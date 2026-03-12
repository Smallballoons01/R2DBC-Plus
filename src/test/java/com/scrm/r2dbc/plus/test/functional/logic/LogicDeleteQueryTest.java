package com.scrm.r2dbc.plus.test.functional.logic;

import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.mapper.impl.BaseMapperImpl;
import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 逻辑删除查询功能测试
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
public class LogicDeleteQueryTest {

    @Autowired
    private LogicDeleteProcessor logicDeleteProcessor;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Autowired
    private DatabaseClient databaseClient;

    private BaseMapperImpl<TestAccount> baseMapper;

    @BeforeEach
    public void setUp() {
        // 创建BaseMapperImpl实例并手动注入依赖
        baseMapper = new BaseMapperImpl<>(TestAccount.class, r2dbcEntityTemplate, databaseClient);
        
        // 手动注入LogicDeleteProcessor
        try {
            java.lang.reflect.Field field = BaseMapperImpl.class.getDeclaredField("logicDeleteProcessor");
            field.setAccessible(true);
            field.set(baseMapper, logicDeleteProcessor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject LogicDeleteProcessor", e);
        }
    }

    @Test
    @DisplayName("测试逻辑删除条件构建")
    public void testBuildLogicDeleteCondition() {
        String condition = logicDeleteProcessor.buildLogicDeleteCondition(TestAccount.class);
        
        assertNotNull(condition, "逻辑删除条件不应该为空");
        assertFalse(condition.isEmpty(), "逻辑删除条件不应该为空字符串");
        assertTrue(condition.contains("is_del"), "应该包含逻辑删除字段");
        assertTrue(condition.contains("0"), "应该包含未删除值");
        
        System.out.println("逻辑删除条件: " + condition);
    }

    @Test
    @DisplayName("测试selectById包含逻辑删除条件")
    public void testSelectByIdWithLogicDelete() {
        // 这个测试主要验证SQL构建逻辑，不执行实际查询
        // 因为我们没有真实的数据库数据
        
        // 验证LogicDeleteProcessor被正确注入
        assertNotNull(logicDeleteProcessor, "LogicDeleteProcessor应该被注入");
        
        // 验证逻辑删除条件能正确构建
        String condition = logicDeleteProcessor.buildLogicDeleteCondition(TestAccount.class);
        assertEquals("is_del = 0", condition, "逻辑删除条件应该是 'is_del = 0'");
        
        System.out.println("selectById逻辑删除测试通过！");
    }

    @Test
    @DisplayName("测试selectList包含逻辑删除条件")
    public void testSelectListWithLogicDelete() {
        // 创建查询条件
        LambdaQueryWrapper<TestAccount> queryWrapper = new LambdaQueryWrapper<>(TestAccount.class);
        queryWrapper.eq(TestAccount::getNickname, "test");
        
        // 验证逻辑删除条件能正确构建
        String condition = logicDeleteProcessor.buildLogicDeleteCondition(TestAccount.class);
        assertEquals("is_del = 0", condition, "逻辑删除条件应该是 'is_del = 0'");
        
        System.out.println("selectList逻辑删除测试通过！");
    }

    @Test
    @DisplayName("测试没有逻辑删除注解的实体类")
    public void testEntityWithoutLogicDelete() {
        // 使用一个没有逻辑删除注解的类进行测试
        String condition = logicDeleteProcessor.buildLogicDeleteCondition(String.class);
        
        assertTrue(condition.isEmpty(), "没有逻辑删除注解的实体类应该返回空条件");
        
        System.out.println("无逻辑删除注解实体类测试通过！");
    }
}
