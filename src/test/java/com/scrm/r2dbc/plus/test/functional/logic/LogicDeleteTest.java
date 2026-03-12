package com.scrm.r2dbc.plus.test.functional.logic;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 逻辑删除功能测试
 * 原 TestAccountTableLogicTest.java
 *
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class LogicDeleteTest {

    private LogicDeleteProcessor logicDeleteProcessor;
    private FieldFillProcessor fieldFillProcessor;

    @BeforeEach
    public void setUp() throws Exception {
        // 直接创建实例，不依赖Spring容器
        this.logicDeleteProcessor = new LogicDeleteProcessor();
        this.fieldFillProcessor = new FieldFillProcessor();

        // 创建字段填充处理器
        DefaultMetaObjectHandler metaObjectHandler = new DefaultMetaObjectHandler();

        // 通过反射注入依赖
        injectDependency(fieldFillProcessor, "idGenerator", new DefaultIdGenerator());
        injectDependency(fieldFillProcessor, "logicDeleteProcessor", logicDeleteProcessor);
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
    public void testLogicDeleteFieldDetection() {
        // 测试逻辑删除字段检测
        LogicDeleteProcessor.LogicDeleteInfo info = logicDeleteProcessor.getLogicDeleteInfo(TestAccount.class);

        assertNotNull(info, "应该检测到逻辑删除字段");
        assertNotNull(info.getFieldName(), "逻辑删除字段名不应该为空");
        assertEquals("isDel", info.getFieldName(), "逻辑删除字段名应该是 isDel");
        assertEquals("0", info.getNotDeletedValue(), "未删除值应该是 0");
        assertEquals("1", info.getDeletedValue(), "已删除值应该是 1");

        System.out.println("=== 逻辑删除字段信息 ===");
        System.out.println("字段名: " + info.getFieldName());
        System.out.println("未删除值: " + info.getNotDeletedValue());
        System.out.println("已删除值: " + info.getDeletedValue());
    }

    @Test
    public void testLogicDeleteInitialization() {
        // 测试逻辑删除字段初始化
        TestAccount account = new TestAccount();
        
        // 确保 isDel 字段为 null
        assertNull(account.getIsDel(), "初始状态 isDel 应该为 null");
        
        // 处理插入前的逻辑删除字段初始化
        logicDeleteProcessor.processInsert(account);
        
        // 验证 isDel 字段被设置为未删除值
        assertNotNull(account.getIsDel(), "处理后 isDel 不应该为 null");
        assertEquals(Integer.valueOf(0), account.getIsDel(), "isDel 应该被设置为未删除值 0");
        
        System.out.println("逻辑删除字段初始化测试通过！");
    }

    @Test
    public void testLogicDeleteNotOverrideExistingValue() {
        // 测试不覆盖已有值
        TestAccount account = new TestAccount();
        account.setIsDel(1); // 手动设置为已删除
        
        // 处理插入前的逻辑删除字段初始化
        logicDeleteProcessor.processInsert(account);
        
        // 验证已有值不被覆盖
        assertEquals(Integer.valueOf(1), account.getIsDel(), "已有值不应该被覆盖");
        
        System.out.println("逻辑删除字段不覆盖已有值测试通过！");
    }

    @Test
    public void testLogicDeleteWithFieldFillProcessor() {
        // 测试与字段填充处理器的集成
        TestAccount account = new TestAccount();
        account.setUuid("test-uuid");
        account.setAccount("test-account");
        
        // 确保 isDel 字段为 null
        assertNull(account.getIsDel(), "初始状态 isDel 应该为 null");
        
        // 处理插入前的字段填充（包括逻辑删除字段）
        fieldFillProcessor.processInsert(account);
        
        // 验证逻辑删除字段被正确初始化
        assertNotNull(account.getIsDel(), "处理后 isDel 不应该为 null");
        assertEquals(Integer.valueOf(0), account.getIsDel(), "isDel 应该被设置为未删除值 0");
        
        // 验证ID生成
        assertNotNull(account.getId(), "ID 应该被自动生成");
        
        // 验证时间字段填充
        assertNotNull(account.getGmtCreate(), "创建时间应该被自动填充");
        assertNotNull(account.getGmtModified(), "修改时间应该被自动填充");
        
        System.out.println("=== 字段填充处理器集成测试 ===");
        System.out.println("ID: " + account.getId());
        System.out.println("isDel: " + account.getIsDel());
        System.out.println("创建时间: " + account.getGmtCreate());
        System.out.println("修改时间: " + account.getGmtModified());
    }

    @Test
    public void testLogicDeleteCaching() {
        // 测试逻辑删除信息缓存
        LogicDeleteProcessor.LogicDeleteInfo info1 = logicDeleteProcessor.getLogicDeleteInfo(TestAccount.class);
        LogicDeleteProcessor.LogicDeleteInfo info2 = logicDeleteProcessor.getLogicDeleteInfo(TestAccount.class);
        
        // 验证返回同一个对象（缓存生效）
        assertSame(info1, info2, "多次获取应该返回缓存的同一个对象");
        
        System.out.println("逻辑删除信息缓存测试通过！");
    }

    @Test
    public void testNullEntityHandling() {
        // 测试空实体处理
        assertDoesNotThrow(() -> {
            logicDeleteProcessor.processInsert(null);
        }, "处理空实体不应该抛出异常");
        
        System.out.println("空实体处理测试通过！");
    }

    @Test
    public void testLogicDeleteFieldInfo() {
        // 测试逻辑删除字段信息的详细内容
        LogicDeleteProcessor.LogicDeleteInfo info = logicDeleteProcessor.getLogicDeleteInfo(TestAccount.class);

        assertNotNull(info, "逻辑删除信息不应该为空");

        // 验证字段信息
        String fieldName = info.getFieldName();
        assertNotNull(fieldName, "逻辑删除字段名不应该为空");
        assertEquals("isDel", fieldName, "字段名应该是 isDel");

        // 验证注解值
        assertEquals("0", info.getNotDeletedValue(), "未删除值应该是 0");
        assertEquals("1", info.getDeletedValue(), "已删除值应该是 1");

        System.out.println("逻辑删除字段信息详细测试通过！");
    }
}
