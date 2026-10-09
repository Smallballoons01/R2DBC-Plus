package com.scrm.r2dbc.plus.test.functional.crud;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.util.EntityUtils;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 更新操作功能测试
 * 原 TestAccountUpdateTest.java
 *
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class UpdateOperationTest {

    private FieldFillProcessor fieldFillProcessor;

    @BeforeEach
    public void setUp() throws Exception {
        // 直接创建实例，不依赖Spring容器
        LogicDeleteProcessor logicDeleteProcessor = new LogicDeleteProcessor();
        VersionProcessor versionProcessor = new VersionProcessor();
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
    public void testUpdateFieldFill() {
        // 测试更新时的字段填充
        TestAccount account = new TestAccount();
        account.setId(1L);
        account.setUuid("test-uuid");
        account.setAccount("test-account");
        account.setNickname("原始昵称");
        
        // 模拟更新操作
        account.setNickname("更新后昵称");
        
        // 处理更新前的字段填充
        fieldFillProcessor.processUpdate(account);
        
        // 验证更新时间被填充
        assertNotNull(account.getGmtModified(), "更新时间应该被自动填充");
        
        System.out.println("=== 更新字段填充测试 ===");
        System.out.println("更新时间: " + account.getGmtModified());
        System.out.println("昵称: " + account.getNickname());
    }

    @Test
    public void testLambdaUpdateWrapper() {
        // 测试 Lambda 更新包装器
        LambdaUpdateWrapper<TestAccount> updateWrapper = new LambdaUpdateWrapper<>();
        
        // 设置更新条件
        updateWrapper.eq(TestAccount::getUuid, "test-uuid")
                    .eq(TestAccount::getStatus, 1);
        
        // 设置更新字段
        updateWrapper.set(TestAccount::getNickname, "新昵称")
                    .set(TestAccount::getStatus, 2);
        
        // 验证 SQL 片段生成
        String whereSegment = updateWrapper.getSqlSegment();
        String setSegment = updateWrapper.getSetSegment();
        
        assertNotNull(whereSegment, "WHERE 条件不应该为空");
        assertNotNull(setSegment, "SET 语句不应该为空");
        
        assertTrue(whereSegment.contains("uuid"), "WHERE 条件应该包含 uuid");
        assertTrue(whereSegment.contains("status"), "WHERE 条件应该包含 status");
        assertTrue(setSegment.contains("nickname"), "SET 语句应该包含 nickname");
        assertTrue(setSegment.contains("status"), "SET 语句应该包含 status");
        
        System.out.println("=== Lambda 更新包装器测试 ===");
        System.out.println("WHERE 条件: " + whereSegment);
        System.out.println("SET 语句: " + setSegment);
        
        // 验证参数映射
        Map<String, Object> params = updateWrapper.getParamNameValuePairs();
        assertNotNull(params, "参数映射不应该为空");
        assertTrue(params.size() > 0, "应该有参数");
        
        System.out.println("参数映射: " + params);
    }

    @Test
    public void testUpdateWrapperChaining() {
        // 测试更新包装器的链式调用
        LambdaUpdateWrapper<TestAccount> updateWrapper = new LambdaUpdateWrapper<>();
        
        // 链式调用
        updateWrapper.eq(TestAccount::getUuid, "test-uuid")
                    .ne(TestAccount::getStatus, 0)
                    .set(TestAccount::getNickname, "链式更新昵称")
                    .set(TestAccount::getStatus, 3)
                    .setSql(TestAccount::getGmtModified, "NOW()");
        
        String whereSegment = updateWrapper.getSqlSegment();
        String setSegment = updateWrapper.getSetSegment();
        
        // 验证链式调用结果
        assertTrue(whereSegment.contains("uuid"), "应该包含 uuid 条件");
        assertTrue(whereSegment.contains("status"), "应该包含 status 条件");
        assertTrue(setSegment.contains("nickname"), "应该包含 nickname 设置");
        assertTrue(setSegment.contains("gmt_modified = NOW()"), "应该包含自定义 SQL");
        
        System.out.println("=== 链式调用测试 ===");
        System.out.println("WHERE: " + whereSegment);
        System.out.println("SET: " + setSegment);
    }

    @Test
    public void testUpdateWrapperConditions() {
        // 测试各种更新条件
        LambdaUpdateWrapper<TestAccount> updateWrapper = new LambdaUpdateWrapper<>();
        
        updateWrapper.eq(TestAccount::getUuid, "test-uuid")
                    .gt(TestAccount::getId, 0L)
                    .le(TestAccount::getStatus, 5)
                    .isNotNull(TestAccount::getAccount)
                    .like(TestAccount::getNickname, "测试%");
        
        String whereSegment = updateWrapper.getSqlSegment();
        
        // 验证各种条件
        assertTrue(whereSegment.contains("="), "应该包含等于条件");
        assertTrue(whereSegment.contains(">"), "应该包含大于条件");
        assertTrue(whereSegment.contains("<="), "应该包含小于等于条件");
        assertTrue(whereSegment.contains("IS NOT NULL"), "应该包含非空条件");
        assertTrue(whereSegment.contains("LIKE"), "应该包含模糊查询条件");
        
        System.out.println("=== 更新条件测试 ===");
        System.out.println("WHERE: " + whereSegment);
    }

    @Test
    public void testFieldMapping() {
        // 测试字段映射在更新中的应用
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // 验证关键字段映射
        assertEquals("uuid", fieldMapping.get("uuid"));
        assertEquals("nickname", fieldMapping.get("nickname"));
        assertEquals("status", fieldMapping.get("status"));
        assertEquals("is_del", fieldMapping.get("isDel"));
        assertEquals("gmt_modified", fieldMapping.get("gmtModified"));
        
        System.out.println("=== 字段映射测试 ===");
        fieldMapping.forEach((field, column) -> 
            System.out.println(field + " -> " + column));
    }

    @Test
    public void testUpdateWithVersion() {
        // 测试带版本号的更新（乐观锁）
        TestAccount account = new TestAccount();
        account.setId(1L);
        account.setUuid("test-uuid");
        account.setAccount("test-account");
        account.setVersion(1); // 设置版本号
        
        // 模拟更新操作
        account.setNickname("版本更新昵称");
        
        // 处理更新前的字段填充（包括版本号处理）
        fieldFillProcessor.processUpdate(account);
        
        // 验证版本号被递增
        assertEquals(2, account.getVersion(), "版本号应该被递增");
        
        System.out.println("=== 版本号更新测试 ===");
        System.out.println("更新后版本号: " + account.getVersion());
    }

    @Test
    public void testUpdateSqlGeneration() {
        // 测试更新 SQL 生成
        LambdaUpdateWrapper<TestAccount> updateWrapper = new LambdaUpdateWrapper<>();
        
        updateWrapper.eq(TestAccount::getId, 1L)
                    .set(TestAccount::getNickname, "SQL生成测试")
                    .set(TestAccount::getStatus, 2);
        
        String tableName = EntityUtils.getTableName(TestAccount.class);
        String setSegment = updateWrapper.getSetSegment();
        String whereSegment = updateWrapper.getSqlSegment();
        
        // 模拟生成完整的更新 SQL
        String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, setSegment, whereSegment);
        
        assertTrue(sql.contains("UPDATE test_account"), "应该包含表名");
        assertTrue(sql.contains("SET"), "应该包含 SET 关键字");
        assertTrue(sql.contains("WHERE"), "应该包含 WHERE 关键字");
        
        System.out.println("=== SQL 生成测试 ===");
        System.out.println("生成的 SQL: " + sql);
    }
}
