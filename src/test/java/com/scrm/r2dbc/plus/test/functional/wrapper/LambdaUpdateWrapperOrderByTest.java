package com.scrm.r2dbc.plus.test.functional.wrapper;

import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LambdaUpdateWrapper 排序功能测试
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class LambdaUpdateWrapperOrderByTest {

    // 测试实体类
    static class TestEntity {
        private Long id;
        private String name;
        private Integer status;
        
        // getter 方法用于 Lambda 表达式
        public Long getId() { return id; }
        public String getName() { return name; }
        public Integer getStatus() { return status; }
    }

    @Test
    public void testOrderByDesc() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderByDesc(TestEntity::getId);

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals(" ORDER BY id DESC", orderBySegment);
        
        // 验证完整的 SQL 段包含排序
        String fullSql = wrapper.getSqlSegment();
        assertTrue(fullSql.contains("ORDER BY id DESC"));
    }

    @Test
    public void testOrderByAsc() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderByAsc(TestEntity::getId);

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals(" ORDER BY id ASC", orderBySegment);
    }

    @Test
    public void testMultipleOrderBy() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderByDesc(TestEntity::getId)
               .orderByAsc(TestEntity::getName);

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals(" ORDER BY id DESC, name ASC", orderBySegment);
    }

    @Test
    public void testOrderByWithCondition() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderBy(true, false, TestEntity::getId)  // 降序
               .orderBy(false, true, TestEntity::getName); // 条件为false，不执行

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals(" ORDER BY id DESC", orderBySegment);
    }

    @Test
    public void testOrderByGeneric() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderBy(false, TestEntity::getId)  // 降序
               .orderBy(true, TestEntity::getName);   // 升序

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals(" ORDER BY id DESC, name ASC", orderBySegment);
    }

    @Test
    public void testNoOrderBy() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test");

        String orderBySegment = wrapper.getOrderBySegment();
        assertEquals("", orderBySegment);
    }

    @Test
    public void testOrderByWithLast() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<>(TestEntity.class);
        wrapper.set(TestEntity::getStatus, 1)
               .eq(TestEntity::getName, "test")
               .orderByDesc(TestEntity::getId)
               .last("LIMIT 10");

        String fullSql = wrapper.getSqlSegment();
        assertTrue(fullSql.contains("ORDER BY id DESC"));
        assertTrue(fullSql.contains("LIMIT 10"));
        // ORDER BY 应该在 LIMIT 之前
        assertTrue(fullSql.indexOf("ORDER BY") < fullSql.indexOf("LIMIT"));
    }
}
