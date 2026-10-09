package com.scrm.r2dbc.plus.test.unit;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.test.entity.CleanExampleEntity;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * 缓存性能测试 - 单元测试版本
 * 验证字段信息缓存的效果，不依赖Spring Boot上下文
 *
 * @author dason
 */
public class CachePerformanceTest {

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
    public void testCachePerformance() {
        int iterations = 10000;
        
        // 预热
        for (int i = 0; i < 100; i++) {
            CleanExampleEntity entity = new CleanExampleEntity();
            entity.setName("Warmup " + i);
            fieldFillProcessor.processInsert(entity);
        }
        
        // 测试字段填充性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            CleanExampleEntity entity = new CleanExampleEntity();
            entity.setName("Performance Test " + i);
            fieldFillProcessor.processInsert(entity);
        }
        long endTime = System.currentTimeMillis();
        
        System.out.println("=== 缓存性能测试结果 ===");
        System.out.println("测试次数: " + iterations);
        System.out.println("总耗时: " + (endTime - startTime) + " ms");
        System.out.println("平均耗时: " + ((double)(endTime - startTime) / iterations) + " ms/次");
        
        // 验证缓存命中
        testCacheHit();
    }
    
    @Test
    public void testCacheHit() {
        // 测试逻辑删除缓存
        long start1 = System.nanoTime();
        var logicDeleteInfo1 = logicDeleteProcessor.getLogicDeleteInfo(CleanExampleEntity.class);
        long end1 = System.nanoTime();
        
        long start2 = System.nanoTime();
        var logicDeleteInfo2 = logicDeleteProcessor.getLogicDeleteInfo(CleanExampleEntity.class);
        long end2 = System.nanoTime();
        
        // 测试版本缓存
        long start3 = System.nanoTime();
        var versionInfo1 = versionProcessor.getVersionInfo(CleanExampleEntity.class);
        long end3 = System.nanoTime();
        
        long start4 = System.nanoTime();
        var versionInfo2 = versionProcessor.getVersionInfo(CleanExampleEntity.class);
        long end4 = System.nanoTime();
        
        System.out.println("=== 缓存命中测试结果 ===");
        System.out.println("逻辑删除字段信息:");
        System.out.println("  首次获取: " + (end1 - start1) + " ns");
        System.out.println("  缓存命中: " + (end2 - start2) + " ns");
        System.out.println("  性能提升: " + ((double)(end1 - start1) / (end2 - start2)) + " 倍");
        
        System.out.println("版本字段信息:");
        System.out.println("  首次获取: " + (end3 - start3) + " ns");
        System.out.println("  缓存命中: " + (end4 - start4) + " ns");
        System.out.println("  性能提升: " + ((double)(end3 - start3) / (end4 - start4)) + " 倍");
        
        // 验证返回的是同一个对象引用（缓存生效）
        assert logicDeleteInfo1 == logicDeleteInfo2 : "逻辑删除信息应该来自缓存";
        assert versionInfo1 == versionInfo2 : "版本信息应该来自缓存";
    }
    
    @Test
    public void testMultipleEntityTypes() {
        // 测试多种实体类型的缓存
        CleanExampleEntity entity1 = new CleanExampleEntity();
        entity1.setName("Entity 1");
        
        // 创建另一个实体类型进行测试
        TestEntity entity2 = new TestEntity();
        entity2.setName("Entity 2");
        
        // 处理不同类型的实体
        fieldFillProcessor.processInsert(entity1);
        fieldFillProcessor.processInsert(entity2);
        
        // 验证缓存为不同类型分别存储
        var logicInfo1 = logicDeleteProcessor.getLogicDeleteInfo(CleanExampleEntity.class);
        var logicInfo2 = logicDeleteProcessor.getLogicDeleteInfo(TestEntity.class);
        
        System.out.println("=== 多实体类型缓存测试结果 ===");
        System.out.println("CleanExampleEntity 逻辑删除字段: " + 
            (logicInfo1 != null ? logicInfo1.getFieldName() : "无"));
        System.out.println("TestEntity 逻辑删除字段: " + 
            (logicInfo2 != null ? logicInfo2.getFieldName() : "无"));
    }
    
    /**
     * 测试用的简单实体类
     */
    public static class TestEntity {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
