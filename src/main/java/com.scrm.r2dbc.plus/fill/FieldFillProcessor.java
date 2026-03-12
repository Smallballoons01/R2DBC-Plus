package com.scrm.r2dbc.plus.fill;

import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.enums.FieldFill;
import com.scrm.r2dbc.plus.enums.IdType;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.generator.IdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.util.EntityUtils;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 字段填充处理器
 *
 * @author dason
 */
public class FieldFillProcessor {

    @Autowired
    private IdGenerator idGenerator;

    @Autowired(required = false)
    private List<MetaObjectHandler> metaObjectHandlers;

    @Autowired(required = false)
    private LogicDeleteProcessor logicDeleteProcessor;

    @Autowired(required = false)
    private VersionProcessor versionProcessor;

    /**
     * 插入前处理
     */
    public void processInsert(Object entity) {
        if (entity == null) {
            return;
        }

        // 处理主键生成
        processIdGeneration(entity);

        // 处理逻辑删除字段初始化
        if (logicDeleteProcessor != null) {
            logicDeleteProcessor.processInsert(entity);
        }

        // 处理版本号初始化
        if (versionProcessor != null) {
            versionProcessor.processInsert(entity);
        }

        // 处理字段自动填充
        processAutoFill(entity, FieldFill.INSERT);
        processAutoFill(entity, FieldFill.INSERT_UPDATE);

        // 处理元对象填充
        processMetaObjectFill(entity, true);
    }

    /**
     * 更新前处理
     */
    public void processUpdate(Object entity) {
        if (entity == null) {
            return;
        }

        // 处理版本号递增
        if (versionProcessor != null) {
            versionProcessor.processUpdate(entity);
        }

        // 处理字段自动填充
        processAutoFill(entity, FieldFill.UPDATE);
        processAutoFill(entity, FieldFill.INSERT_UPDATE);

        // 处理元对象填充
        processMetaObjectFill(entity, false);
    }

    /**
     * 处理主键生成
     */
    private void processIdGeneration(Object entity) {
        try {
            Object currentValue = EntityUtils.getIdValue(entity);
            if (currentValue == null) {
                EntityUtils.IdFieldInfo idFieldInfo = EntityUtils.getIdFieldInfo(entity.getClass());
                Serializable id = generateId(idFieldInfo.getIdType(), entity);
                if (id != null) {
                    EntityUtils.setIdValue(entity, id);
                }
            }
        } catch (Exception e) {
            // 如果没有ID字段，忽略错误
        }
    }

    /**
     * 生成 ID
     */
    private Serializable generateId(IdType idType, Object entity) {
        return switch (idType) {
            case AUTO ->
                // 数据库自增，不需要生成
                    null;
            case ASSIGN_ID -> idGenerator.nextId(entity);
            case ASSIGN_UUID -> idGenerator.nextUUID(entity);
            case ASSIGN_ORDER_UUID -> idGenerator.nextOrderedUUID(entity);
            default -> null;
        };
    }

    /**
     * 处理字段自动填充
     * 只处理带有 @TableField(fill = xxx) 注解的字段
     */
    private void processAutoFill(Object entity, FieldFill fillType) {
        Class<?> entityClass = entity.getClass();
        Field[] fields = EntityUtils.getAllFields(entityClass);

        for (Field field : fields) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && tableField.fill() == fillType) {
                // 字段填充逻辑通过 MetaObjectHandler 来实现
                // 这里不做具体的填充，只是标记需要填充的字段
            }
        }
    }

    /**
     * 处理元对象填充
     */
    private void processMetaObjectFill(Object entity, boolean isInsert) {
        if (metaObjectHandlers == null || metaObjectHandlers.isEmpty()) {
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

}
