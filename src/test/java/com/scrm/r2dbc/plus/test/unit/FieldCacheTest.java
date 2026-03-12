package com.scrm.r2dbc.plus.test.unit;

import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段缓存功能测试
 * 
 * @author dason
 */
public class FieldCacheTest {

    @Test
    public void testFieldCacheBasic() {
        // 测试字段缓存基本功能
        Map<String, Field> fieldCache = EntityUtils.getFieldCache(TestAccount.class);
        
        assertNotNull(fieldCache, "字段缓存不应该为空");
        assertTrue(fieldCache.size() > 0, "字段缓存应该包含字段");
        
        // 验证包含预期的字段
        assertTrue(fieldCache.containsKey("id"), "应该包含id字段");
        assertTrue(fieldCache.containsKey("account"), "应该包含account字段");
        assertTrue(fieldCache.containsKey("password"), "应该包含password字段");
        
        System.out.println("字段缓存包含 " + fieldCache.size() + " 个字段");
        fieldCache.keySet().forEach(fieldName -> 
            System.out.println("- " + fieldName));
    }

    @Test
    public void testGetFieldWithCache() throws NoSuchFieldException {
        // 测试通过缓存获取字段
        Field idField = EntityUtils.getField(TestAccount.class, "id");
        assertNotNull(idField, "应该能获取到id字段");
        assertEquals("id", idField.getName(), "字段名应该正确");
        assertTrue(idField.isAccessible(), "字段应该已设置为可访问");
        
        Field accountField = EntityUtils.getField(TestAccount.class, "account");
        assertNotNull(accountField, "应该能获取到account字段");
        assertEquals("account", accountField.getName(), "字段名应该正确");
        assertTrue(accountField.isAccessible(), "字段应该已设置为可访问");
    }

    @Test
    public void testGetFieldNotFound() {
        // 测试获取不存在的字段
        assertThrows(NoSuchFieldException.class, () -> {
            EntityUtils.getField(TestAccount.class, "nonExistentField");
        }, "获取不存在的字段应该抛出异常");
    }

    @Test
    public void testFieldValueOperations() {
        // 测试字段值操作
        TestAccount account = new TestAccount();
        account.setAccount("testUser");
        account.setPassword("testPassword");
        
        // 测试获取字段值
        Object accountValue = EntityUtils.getFieldValue(account, "account");
        assertEquals("testUser", accountValue, "应该能正确获取字段值");
        
        Object passwordValue = EntityUtils.getFieldValue(account, "password");
        assertEquals("testPassword", passwordValue, "应该能正确获取字段值");
        
        // 测试设置字段值
        EntityUtils.setFieldValue(account, "account", "newUser");
        assertEquals("newUser", account.getAccount(), "应该能正确设置字段值");
        
        EntityUtils.setFieldValue(account, "password", "newPassword");
        assertEquals("newPassword", account.getPassword(), "应该能正确设置字段值");
    }

    @Test
    public void testCacheConsistency() {
        // 测试缓存一致性
        Map<String, Field> cache1 = EntityUtils.getFieldCache(TestAccount.class);
        Map<String, Field> cache2 = EntityUtils.getFieldCache(TestAccount.class);
        
        assertSame(cache1, cache2, "多次获取应该返回同一个缓存实例");
        
        Field field1 = cache1.get("id");
        Field field2 = cache2.get("id");
        
        assertSame(field1, field2, "同一字段应该返回同一个Field实例");
    }

    @Test
    public void testInheritedFields() {
        // 测试继承字段的缓存
        Map<String, Field> fieldCache = EntityUtils.getFieldCache(TestAccount.class);
        
        // TestAccount 继承自 TestBaseEntity，应该包含父类字段
        assertTrue(fieldCache.containsKey("isDel"), "应该包含继承的isDel字段");
        assertTrue(fieldCache.containsKey("gmtCreate"), "应该包含继承的gmtCreate字段");
        assertTrue(fieldCache.containsKey("gmtModified"), "应该包含继承的gmtModified字段");
        
        System.out.println("=== 继承字段测试 ===");
        System.out.println("总字段数: " + fieldCache.size());
        fieldCache.entrySet().stream()
            .filter(entry -> {
                try {
                    return !TestAccount.class.getDeclaredField(entry.getKey()).getDeclaringClass().equals(TestAccount.class);
                } catch (NoSuchFieldException e) {
                    return true; // 说明是继承的字段
                }
            })
            .forEach(entry -> System.out.println("继承字段: " + entry.getKey()));
    }
}
