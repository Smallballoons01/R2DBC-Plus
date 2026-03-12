package com.scrm.r2dbc.plus.test.functional.field;

import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.util.EntityUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段映射测试
 * 原 TestAccountFieldTest.java
 *
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class FieldMappingTest {

    @Test
    public void testTestAccountFieldMapping() {
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        System.out.println("=== TestAccount 字段映射 ===");
        fieldMapping.forEach((field, column) -> 
            System.out.println(field + " -> " + column));
        
        // 验证 serialVersionUID 被过滤
        assertFalse(fieldMapping.containsKey("serialVersionUID"), 
            "serialVersionUID 应该被过滤掉");
        
        // 验证 TestAccount 自身字段
        assertTrue(fieldMapping.containsKey("id"), "应该包含 id 字段");
        assertTrue(fieldMapping.containsKey("uuid"), "应该包含 uuid 字段");
        assertTrue(fieldMapping.containsKey("account"), "应该包含 account 字段");
        assertTrue(fieldMapping.containsKey("password"), "应该包含 password 字段");
        assertTrue(fieldMapping.containsKey("nickname"), "应该包含 nickname 字段");
        assertTrue(fieldMapping.containsKey("status"), "应该包含 status 字段");
        assertTrue(fieldMapping.containsKey("isOpen"), "应该包含 isOpen 字段");
        
        // 验证继承的 TestBaseEntity 字段
        assertTrue(fieldMapping.containsKey("isDel"), "应该包含继承的 isDel 字段");
        assertTrue(fieldMapping.containsKey("gmtCreate"), "应该包含继承的 gmtCreate 字段");
        assertTrue(fieldMapping.containsKey("gmtModified"), "应该包含继承的 gmtModified 字段");
        assertTrue(fieldMapping.containsKey("createBy"), "应该包含继承的 createBy 字段");
        assertTrue(fieldMapping.containsKey("updatedBy"), "应该包含继承的 updatedBy 字段");
        assertTrue(fieldMapping.containsKey("sortNum"), "应该包含继承的 sortNum 字段");
        
        // 验证字段映射的正确性
        assertEquals("id", fieldMapping.get("id"), "id 字段映射应该正确");
        assertEquals("uuid", fieldMapping.get("uuid"), "uuid 字段映射应该正确");
        assertEquals("account", fieldMapping.get("account"), "account 字段映射应该正确");
        assertEquals("password", fieldMapping.get("password"), "password 字段映射应该正确");
        assertEquals("nickname", fieldMapping.get("nickname"), "nickname 字段映射应该正确");
        assertEquals("status", fieldMapping.get("status"), "status 字段映射应该正确");
        assertEquals("is_open", fieldMapping.get("isOpen"), "isOpen 字段映射应该正确");
        
        // 验证继承字段的映射
        assertEquals("is_del", fieldMapping.get("isDel"), "isDel 字段映射应该正确");
        assertEquals("gmt_create", fieldMapping.get("gmtCreate"), "gmtCreate 字段映射应该正确");
        assertEquals("gmt_modified", fieldMapping.get("gmtModified"), "gmtModified 字段映射应该正确");
        assertEquals("create_by", fieldMapping.get("createBy"), "createBy 字段映射应该正确");
        assertEquals("updated_by", fieldMapping.get("updatedBy"), "updatedBy 字段映射应该正确");
        assertEquals("sort_num", fieldMapping.get("sortNum"), "sortNum 字段映射应该正确");
        
        System.out.println("字段映射测试通过！");
    }

    @Test
    public void testTableName() {
        String tableName = EntityUtils.getTableName(TestAccount.class);
        assertEquals("test_account", tableName, "表名应该正确");
        System.out.println("表名: " + tableName);
    }

    @Test
    public void testIdColumnName() {
        String idColumnName = EntityUtils.getIdColumnName(TestAccount.class);
        assertEquals("id", idColumnName, "主键列名应该正确");
        System.out.println("主键列名: " + idColumnName);
    }

    @Test
    public void testFieldCount() {
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // TestAccount 自身字段：id, uuid, account, password, nickname, status, isOpen (7个)
        // TestBaseEntity 继承字段：isDel, gmtCreate, gmtModified, createBy, updatedBy, sortNum (6个)
        // 总计：13个字段
        
        assertTrue(fieldMapping.size() >= 13, 
            "字段数量应该至少有13个，实际有: " + fieldMapping.size());
        
        System.out.println("总字段数量: " + fieldMapping.size());
    }

    @Test
    public void testFieldExistence() {
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // 验证所有预期字段都存在
        String[] expectedFields = {
            "id", "uuid", "account", "password", "nickname", "status", "isOpen",
            "isDel", "gmtCreate", "gmtModified", "createBy", "updatedBy", "sortNum"
        };
        
        for (String field : expectedFields) {
            assertTrue(fieldMapping.containsKey(field), 
                "应该包含字段: " + field);
        }
        
        System.out.println("所有预期字段都存在！");
    }

    @Test
    public void testColumnNaming() {
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // 验证驼峰命名转下划线命名
        assertEquals("is_open", fieldMapping.get("isOpen"), 
            "驼峰命名应该转换为下划线命名");
        assertEquals("gmt_create", fieldMapping.get("gmtCreate"), 
            "驼峰命名应该转换为下划线命名");
        assertEquals("gmt_modified", fieldMapping.get("gmtModified"), 
            "驼峰命名应该转换为下划线命名");
        assertEquals("create_by", fieldMapping.get("createBy"), 
            "驼峰命名应该转换为下划线命名");
        assertEquals("updated_by", fieldMapping.get("updatedBy"), 
            "驼峰命名应该转换为下划线命名");
        assertEquals("sort_num", fieldMapping.get("sortNum"), 
            "驼峰命名应该转换为下划线命名");
        
        System.out.println("列名命名规则验证通过！");
    }
}
