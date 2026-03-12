package com.scrm.r2dbc.plus.test.functional.logic;

import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.test.mapper.TestAccountMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 逻辑删除处理器注入测试
 * 验证LogicDeleteProcessor是否正确注入到Mapper中
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
public class LogicDeleteInjectionTest {

    @Autowired
    private LogicDeleteProcessor logicDeleteProcessor;

    @Autowired
    private TestAccountMapper testAccountMapper;

    @Test
    @DisplayName("测试LogicDeleteProcessor Bean是否正确创建")
    public void testLogicDeleteProcessorBean() {
        assertNotNull(logicDeleteProcessor, "LogicDeleteProcessor应该被正确注入");
        
        // 测试逻辑删除条件构建
        String condition = logicDeleteProcessor.buildLogicDeleteCondition(TestAccount.class);
        assertNotNull(condition, "逻辑删除条件不应该为空");
        assertEquals("is_del = 0", condition, "逻辑删除条件应该是 'is_del = 0'");
        
        System.out.println("LogicDeleteProcessor Bean测试通过！");
        System.out.println("逻辑删除条件: " + condition);
    }

    @Test
    @DisplayName("测试Mapper中LogicDeleteProcessor是否正确注入")
    public void testMapperLogicDeleteProcessorInjection() {
        assertNotNull(testAccountMapper, "TestAccountMapper应该被正确注入");
        
        // 通过反射检查BaseMapperImpl中的logicDeleteProcessor是否被注入
        try {
            // 获取代理对象背后的实际对象
            java.lang.reflect.InvocationHandler handler = java.lang.reflect.Proxy.getInvocationHandler(testAccountMapper);
            
            // 获取MapperInvocationHandler中的baseMapperImpl
            java.lang.reflect.Field baseMapperField = handler.getClass().getDeclaredField("baseMapperImpl");
            baseMapperField.setAccessible(true);
            Object baseMapperImpl = baseMapperField.get(handler);
            
            // 获取BaseMapperImpl中的logicDeleteProcessor字段
            java.lang.reflect.Field logicDeleteProcessorField = baseMapperImpl.getClass().getDeclaredField("logicDeleteProcessor");
            logicDeleteProcessorField.setAccessible(true);
            LogicDeleteProcessor injectedProcessor = (LogicDeleteProcessor) logicDeleteProcessorField.get(baseMapperImpl);
            
            assertNotNull(injectedProcessor, "BaseMapperImpl中的LogicDeleteProcessor应该被正确注入");
            assertSame(logicDeleteProcessor, injectedProcessor, "注入的LogicDeleteProcessor应该是同一个实例");
            
            System.out.println("Mapper中LogicDeleteProcessor注入测试通过！");
            
        } catch (Exception e) {
            fail("检查LogicDeleteProcessor注入时发生异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试逻辑删除信息获取")
    public void testLogicDeleteInfo() {
        LogicDeleteProcessor.LogicDeleteInfo info = logicDeleteProcessor.getLogicDeleteInfo(TestAccount.class);
        
        assertNotNull(info, "逻辑删除信息不应该为空");
        assertEquals("isDel", info.getFieldName(), "字段名应该是 isDel");
        assertEquals("0", info.getNotDeletedValue(), "未删除值应该是 0");
        assertEquals("1", info.getDeletedValue(), "已删除值应该是 1");
        
        System.out.println("逻辑删除信息测试通过！");
        System.out.println("字段名: " + info.getFieldName());
        System.out.println("未删除值: " + info.getNotDeletedValue());
        System.out.println("已删除值: " + info.getDeletedValue());
    }
}
