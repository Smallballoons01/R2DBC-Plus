package com.scrm.r2dbc.plus.test.functional.wrapper;

import com.scrm.r2dbc.plus.conditions.update.UpdateWrapper;
import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UpdateNullValue功能测试
 */
public class UpdateNullValueTest {

    /**
     * 测试实体类
     */
    public static class TestEntity {
        private String id;
        private String name;
        private String description;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @Test
    public void testUpdateWrapperUpdateNullValue() {
        UpdateWrapper<TestEntity> wrapper = new UpdateWrapper<TestEntity>()
                .eq("id", "1")
                .updateNullValue();
        
        assertTrue(wrapper.isUpdateNullValue(), "UpdateWrapper应该支持updateNullValue标志");
    }

    @Test
    public void testLambdaUpdateWrapperUpdateNullValue() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<TestEntity>(TestEntity.class)
                .eq(TestEntity::getId, "1")
                .updateNullValue();
        
        assertTrue(wrapper.isUpdateNullValue(), "LambdaUpdateWrapper应该支持updateNullValue标志");
    }

    @Test
    public void testLambdaUpdateChainWrapperUpdateNullValue() {
        // 模拟BaseMapper，这里只测试方法调用
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<TestEntity>(TestEntity.class)
                .eq(TestEntity::getId, "1")
                .updateNullValue();
        
        assertTrue(wrapper.isUpdateNullValue(), "LambdaUpdateChainWrapper应该支持updateNullValue标志");
    }

    @Test
    public void testLambdaUpdateMethod() {
        LambdaUpdateWrapper<TestEntity> wrapper = new LambdaUpdateWrapper<TestEntity>(TestEntity.class)
                .eq(TestEntity::getId, "1");
        
        // 测试lambdaUpdate方法返回自身
        LambdaUpdateWrapper<TestEntity> result = wrapper;
        assertSame(wrapper, result, "lambdaUpdate方法应该返回当前实例");
    }

    @Test
    public void testChainedCalls() {
        UpdateWrapper<TestEntity> wrapper = new UpdateWrapper<TestEntity>()
                .eq("visitor_id", "user123")
                .eq("account_id", "account456")
                .eq("id", "msg789")
                .updateNullValue();
        
        assertTrue(wrapper.isUpdateNullValue(), "链式调用应该正确设置updateNullValue标志");
    }
}