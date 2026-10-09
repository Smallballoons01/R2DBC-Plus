package com.scrm.r2dbc.plus.fill;

import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.enums.FieldFill;
import com.scrm.r2dbc.plus.enums.IdType;
import com.scrm.r2dbc.plus.generator.IdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.util.EntityUtils;
import com.scrm.r2dbc.plus.version.VersionProcessor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 字段填充处理器
 *
 * <p>负责插入/更新前的主键生成、逻辑删除字段初始化、版本号处理与元对象自动填充。
 *
 * <p>采用构造器注入，避免字段注入在容器初始化顺序变化时拿到未初始化的 null。
 *
 * @author dason
 */
public class FieldFillProcessor {

    private final IdGenerator idGenerator;
    private final List<MetaObjectHandler> metaObjectHandlers;
    private final LogicDeleteProcessor logicDeleteProcessor;
    private final VersionProcessor versionProcessor;

    public FieldFillProcessor(IdGenerator idGenerator,
                              List<MetaObjectHandler> metaObjectHandlers,
                              LogicDeleteProcessor logicDeleteProcessor,
                              VersionProcessor versionProcessor) {
        this.idGenerator = idGenerator;
        this.metaObjectHandlers = metaObjectHandlers == null ? List.of() : List.copyOf(metaObjectHandlers);
        this.logicDeleteProcessor = logicDeleteProcessor;
        this.versionProcessor = versionProcessor;
    }

    /**
     * 插入前处理
     */
    public void processInsert(Object entity) {
        if (entity == null) {
            return;
        }

        // 1. 主键生成
        processIdGeneration(entity);

        // 2. 逻辑删除字段初始化
        if (logicDeleteProcessor != null) {
            logicDeleteProcessor.processInsert(entity);
        }

        // 3. 版本号初始化
        if (versionProcessor != null) {
            versionProcessor.processInsert(entity);
        }

        // 4. 元对象自动填充（具体填充逻辑由用户实现的 MetaObjectHandler 决定）
        processMetaObjectFill(entity, true);
    }

    /**
     * 更新前处理
     */
    public void processUpdate(Object entity) {
        if (entity == null) {
            return;
        }

        // 1. 版本号递增
        if (versionProcessor != null) {
            versionProcessor.processUpdate(entity);
        }

        // 2. 元对象自动填充
        processMetaObjectFill(entity, false);
    }

    /**
     * 处理主键生成
     */
    private void processIdGeneration(Object entity) {
        try {
            Object currentValue = EntityUtils.getIdValue(entity);
            if (currentValue != null) {
                return;
            }
            EntityUtils.IdFieldInfo idFieldInfo = EntityUtils.getIdFieldInfo(entity.getClass());
            if (idFieldInfo == null) {
                // 实体没有 @TableId 主键，跳过
                return;
            }
            Serializable id = generateId(idFieldInfo.getIdType(), entity);
            if (id != null) {
                EntityUtils.setIdValue(entity, id);
            }
        } catch (Exception e) {
            // 实体缺少主键字段属于正常情况，不视为错误
        }
    }

    /**
     * 生成 ID
     *
     * <p>仅 ASSIGN_* 系列由框架生成；AUTO 交给数据库自增；
     * NONE 与 INPUT 需由用户自行赋值或通过 MetaObjectHandler 填充，此处返回 null 不干预。
     */
    private Serializable generateId(IdType idType, Object entity) {
        if (idType == null) {
            return null;
        }
        return switch (idType) {
            // 数据库自增，由数据库生成
            case AUTO, NONE, INPUT -> null;
            case ASSIGN_ID -> idGenerator.nextId(entity);
            case ASSIGN_UUID -> idGenerator.nextUUID(entity);
            case ASSIGN_ORDER_UUID -> idGenerator.nextOrderedUUID(entity);
        };
    }

    /**
     * 处理元对象填充
     */
    private void processMetaObjectFill(Object entity, boolean isInsert) {
        if (metaObjectHandlers.isEmpty()) {
            return;
        }

        MetaObject metaObject = new MetaObject(entity);
        for (MetaObjectHandler handler : metaObjectHandlers) {
            if (isInsert) {
                handler.insertFill(metaObject);
            } else {
                handler.updateFill(metaObject);
            }
        }
    }

    /**
     * 收集实体中声明了指定填充策略的字段名，可用于排查自动填充未生效的问题
     *
     * @param entity   实体对象
     * @param fillType 填充策略
     * @return 字段名列表
     */
    public List<String> collectFillFields(Object entity, FieldFill fillType) {
        if (entity == null) {
            return List.of();
        }
        Field[] fields = EntityUtils.getAllFields(entity.getClass());
        return Arrays.stream(fields)
                .filter(f -> {
                    TableField tableField = f.getAnnotation(TableField.class);
                    return tableField != null && tableField.fill() == fillType;
                })
                .map(Field::getName)
                .toList();
    }
}