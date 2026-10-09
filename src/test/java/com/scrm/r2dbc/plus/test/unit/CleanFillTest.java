package com.scrm.r2dbc.plus.test.unit;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.test.entity.CleanExampleEntity;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简洁的字段填充功能测试 - 单元测试版本
 *
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class CleanFillTest {

    private FieldFillProcessor fieldFillProcessor;
    private LogicDeleteProcessor logicDeleteProcessor;
    private VersionProcessor versionProcessor;

    @BeforeEach
    public void setUp() throws Exception {
        // 直接创建实例，不依赖Spring容器
        this.logicDeleteProcessor = new LogicDeleteProcessor();
        this.versionProcessor = new VersionProcessor();
        this.fieldFillProcessor = new FieldFillProcessor(new com.scrm.r2dbc.plus.generator.DefaultIdGenerator(), java.util.List.of(), null, null);

        // 创建字段填充处理器
        DefaultMetaObjectHandler metaObjectHandler = new DefaultMetaObjectHandler();

        // 通过反射注入依赖
        injectDependency(fieldFillProcessor, "idGenerator", new DefaultIdGenerator());
        injectDependency(fieldFillProcessor, "logicDeleteProcessor", logicDeleteProcessor);
        injectDependency(fieldFillProcessor, "versionProcessor", versionProcessor);
        injectDependency(fieldFillProcessor, "metaObjectHandlers", Collections.singletonList(metaObjectHandler));
    }

    /**
     * 通过反射注入依赖
     */
    private void injectDependency(Object target, String fieldName, Object dependency) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, dependency);
    }

    @Test
    public void testInsertFill() {
        // 创建测试实体
        CleanExampleEntity entity = new CleanExampleEntity();
        entity.setName("Test Entity");
        entity.setDescription("Test Description");
        entity.setStatus(1);
        entity.setExtraInfo("Extra Info");

        // 插入前验证
        assertNull(entity.getId());
        assertNull(entity.getGmtCreate());
        assertNull(entity.getGmtModified());
        assertNull(entity.getIsDel());
        assertNull(entity.getVersion());

        // 执行插入填充
        fieldFillProcessor.processInsert(entity);

        // 验证结果
        assertNotNull(entity.getId()); // ID 自动生成
        assertNotNull(entity.getGmtCreate()); // 创建时间填充
        assertNotNull(entity.getGmtModified()); // 修改时间填充
        assertEquals(0, entity.getIsDel()); // 逻辑删除字段初始化
        assertEquals(1, entity.getVersion()); // 版本号初始化
        assertEquals("Extra Info", entity.getExtraInfo()); // 扩展字段不受影响

        System.out.println("=== 插入填充测试结果 ===");
        System.out.println("ID: " + entity.getId());
        System.out.println("创建时间: " + entity.getGmtCreate());
        System.out.println("修改时间: " + entity.getGmtModified());
        System.out.println("逻辑删除: " + entity.getIsDel());
        System.out.println("版本号: " + entity.getVersion());
        System.out.println("扩展信息: " + entity.getExtraInfo());
    }

    @Test
    public void testUpdateFill() {
        // 先创建并插入填充一个实体
        CleanExampleEntity entity = new CleanExampleEntity();
        entity.setName("Test Entity");
        fieldFillProcessor.processInsert(entity);

        // 记录插入后的状态
        LocalDateTime originalCreateTime = entity.getGmtCreate();
        LocalDateTime originalModifiedTime = entity.getGmtModified();
        Integer originalVersion = entity.getVersion();

        // 等待一毫秒确保时间不同
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 修改实体数据
        entity.setName("Updated Entity");

        // 执行更新填充
        fieldFillProcessor.processUpdate(entity);

        // 验证结果
        assertEquals(originalCreateTime, entity.getGmtCreate()); // 创建时间不变
        assertTrue(entity.getGmtModified().isAfter(originalModifiedTime)); // 修改时间更新
        assertEquals(originalVersion + 1, entity.getVersion()); // 版本号递增

        System.out.println("=== 更新填充测试结果 ===");
        System.out.println("创建时间（不变）: " + originalCreateTime + " -> " + entity.getGmtCreate());
        System.out.println("修改时间（更新）: " + originalModifiedTime + " -> " + entity.getGmtModified());
        System.out.println("版本号（递增）: " + originalVersion + " -> " + entity.getVersion());
    }

    @Test
    public void testLogicDeleteProcessor() {
        // 测试逻辑删除处理器
        CleanExampleEntity entity = new CleanExampleEntity();
        entity.setName("Logic Delete Test");

        // 处理前
        assertNull(entity.getIsDel());

        // 执行逻辑删除处理
        logicDeleteProcessor.processInsert(entity);

        // 验证结果
        assertEquals(0, entity.getIsDel()); // 初始化为未删除

        // 获取逻辑删除信息
        var logicDeleteInfo = logicDeleteProcessor.getLogicDeleteInfo(CleanExampleEntity.class);
        assertNotNull(logicDeleteInfo);
        assertEquals("isDel", logicDeleteInfo.getFieldName());
        assertEquals("0", logicDeleteInfo.getNotDeletedValue());
        assertEquals("1", logicDeleteInfo.getDeletedValue());

        System.out.println("=== 逻辑删除处理器测试结果 ===");
        System.out.println("字段名: " + logicDeleteInfo.getFieldName());
        System.out.println("未删除值: " + logicDeleteInfo.getNotDeletedValue());
        System.out.println("已删除值: " + logicDeleteInfo.getDeletedValue());
        System.out.println("当前值: " + entity.getIsDel());
    }

    @Test
    public void testVersionProcessor() {
        // 测试版本处理器
        CleanExampleEntity entity = new CleanExampleEntity();
        entity.setName("Version Test");

        // 处理前
        assertNull(entity.getVersion());

        // 执行版本初始化
        versionProcessor.processInsert(entity);

        // 验证结果
        assertEquals(1, entity.getVersion()); // 初始版本号为1

        // 执行版本递增
        versionProcessor.processUpdate(entity);

        // 验证结果
        assertEquals(2, entity.getVersion()); // 版本号递增为2

        // 获取版本信息
        var versionInfo = versionProcessor.getVersionInfo(CleanExampleEntity.class);
        assertNotNull(versionInfo);
        assertEquals("version", versionInfo.getFieldName());
        assertEquals(Integer.class, versionInfo.getFieldType());

        // 获取当前版本
        Object currentVersion = versionProcessor.getCurrentVersion(entity);
        assertEquals(2, currentVersion);

        System.out.println("=== 版本处理器测试结果 ===");
        System.out.println("字段名: " + versionInfo.getFieldName());
        System.out.println("字段类型: " + versionInfo.getFieldType().getSimpleName());
        System.out.println("当前版本: " + currentVersion);
    }
}
