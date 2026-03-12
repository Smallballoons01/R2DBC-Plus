package com.scrm.r2dbc.plus.test.unit;


import com.scrm.r2dbc.plus.test.entity.CleanExampleEntity;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.util.EntityUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段过滤测试 - 单元测试版本
 * 验证 serialVersionUID 等字段被正确过滤
 *
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class FieldFilterTest {

    @Test
    public void testSerialVersionUIDFiltered() {
        // 测试 TestAccount 实体类
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // 验证 serialVersionUID 字段被过滤掉
        assertFalse(fieldMapping.containsKey("serialVersionUID"), 
            "serialVersionUID 字段应该被过滤掉");
        
        // 验证正常字段存在
        assertTrue(fieldMapping.containsKey("uuid"), "uuid 字段应该存在");
        assertTrue(fieldMapping.containsKey("account"), "account 字段应该存在");
        assertTrue(fieldMapping.containsKey("nickname"), "nickname 字段应该存在");
        
        // 验证继承的字段存在
        assertTrue(fieldMapping.containsKey("isDel"), "继承的 isDel 字段应该存在");
        assertTrue(fieldMapping.containsKey("gmtCreate"), "继承的 gmtCreate 字段应该存在");
        
        System.out.println("=== TestAccount 字段映射 ===");
        fieldMapping.forEach((field, column) -> 
            System.out.println(field + " -> " + column));
    }

    @Test
    public void testCleanExampleEntityFiltered() {
        // 测试 CleanExampleEntity 实体类
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(CleanExampleEntity.class);
        
        // 验证 serialVersionUID 字段被过滤掉
        assertFalse(fieldMapping.containsKey("serialVersionUID"), 
            "serialVersionUID 字段应该被过滤掉");
        
        // 验证 @TableField(exist = false) 字段被过滤掉
        assertFalse(fieldMapping.containsKey("extraInfo"), 
            "extraInfo 字段应该被过滤掉（exist = false）");
        
        // 验证正常字段存在
        assertTrue(fieldMapping.containsKey("name"), "name 字段应该存在");
        assertTrue(fieldMapping.containsKey("description"), "description 字段应该存在");
        assertTrue(fieldMapping.containsKey("status"), "status 字段应该存在");
        
        System.out.println("=== CleanExampleEntity 字段映射 ===");
        fieldMapping.forEach((field, column) -> 
            System.out.println(field + " -> " + column));
    }

    @Test
    public void testStaticFinalFieldsFiltered() {
        // 创建一个测试实体类来验证静态和final字段过滤
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestEntityWithStaticFields.class);
        
        // 验证静态字段被过滤
        assertFalse(fieldMapping.containsKey("STATIC_FIELD"), 
            "静态字段应该被过滤掉");
        
        // 验证final字段被过滤
        assertFalse(fieldMapping.containsKey("FINAL_FIELD"), 
            "final字段应该被过滤掉");
        
        // 验证serialVersionUID被过滤
        assertFalse(fieldMapping.containsKey("serialVersionUID"), 
            "serialVersionUID应该被过滤掉");
        
        // 验证正常字段存在
        assertTrue(fieldMapping.containsKey("normalField"), 
            "正常字段应该存在");
        
        System.out.println("=== TestEntityWithStaticFields 字段映射 ===");
        fieldMapping.forEach((field, column) -> 
            System.out.println(field + " -> " + column));
    }

    @Test
    public void testFieldColumnNaming() {
        // 测试字段名到列名的转换
        Map<String, String> fieldMapping = EntityUtils.getFieldColumnMapping(TestAccount.class);
        
        // 验证驼峰命名转下划线命名
        assertEquals("group_id", fieldMapping.get("groupId"));
        assertEquals("is_open", fieldMapping.get("isOpen"));
        assertEquals("gmt_create", fieldMapping.get("gmtCreate"));
        assertEquals("gmt_modified", fieldMapping.get("gmtModified"));
        assertEquals("sort_num", fieldMapping.get("sortNum"));
        
        System.out.println("=== 字段命名转换测试 ===");
        System.out.println("groupId -> " + fieldMapping.get("groupId"));
        System.out.println("isOpen -> " + fieldMapping.get("isOpen"));
        System.out.println("gmtCreate -> " + fieldMapping.get("gmtCreate"));
        System.out.println("gmtModified -> " + fieldMapping.get("gmtModified"));
        System.out.println("sortNum -> " + fieldMapping.get("sortNum"));
    }

    /**
     * 测试用的实体类，包含各种类型的字段
     */
    public static class TestEntityWithStaticFields {
        private static final long serialVersionUID = 1L;
        public static final String STATIC_FIELD = "static";
        public final String FINAL_FIELD = "final";
        private String normalField;
        
        public String getNormalField() {
            return normalField;
        }
        
        public void setNormalField(String normalField) {
            this.normalField = normalField;
        }
    }
}
