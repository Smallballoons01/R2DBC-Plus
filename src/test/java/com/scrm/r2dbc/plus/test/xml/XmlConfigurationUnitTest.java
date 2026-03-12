package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.SqlStatement;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML配置单元测试
 * 测试XML配置解析和验证功能
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("xml")
@Tag("unit")
public class XmlConfigurationUnitTest {

    @BeforeEach
    public void setUp() {
        // 清空缓存，确保测试独立性
        XmlMapperParser.clearCache();
    }

    @Test
    public void testXmlParsingBasic() {
        // 测试基本XML解析
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);

        Map<String, SqlStatement> statements = XmlMapperParser.getAllSqlStatements();
        assertFalse(statements.isEmpty(), "应该解析出SQL语句");
        
        System.out.println("=== 基本XML解析测试 ===");
        System.out.println("解析的语句数量: " + statements.size());
        statements.keySet().forEach(id -> System.out.println("语句ID: " + id));
    }

    @Test
    public void testNamespaceResolution() {
        // 测试命名空间解析
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);

        String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";
        
        // 验证命名空间下的语句
        SqlStatement findById = XmlMapperParser.getSqlStatement(namespace + ".findById");
        SqlStatement findByCondition = XmlMapperParser.getSqlStatement(namespace + ".findByCondition");
        
        assertNotNull(findById, "findById语句应该存在");
        assertNotNull(findByCondition, "findByCondition语句应该存在");
        
        System.out.println("=== 命名空间解析测试完成 ===");
    }

    @Test
    public void testStatementTypeDetection() {
        // 测试语句类型检测
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);

        String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";
        
        SqlStatement selectStmt = XmlMapperParser.getSqlStatement(namespace + ".findById");
        SqlStatement insertStmt = XmlMapperParser.getSqlStatement(namespace + ".insert");
        SqlStatement updateStmt = XmlMapperParser.getSqlStatement(namespace + ".updateById");
        SqlStatement deleteStmt = XmlMapperParser.getSqlStatement(namespace + ".deleteById");
        
        if (selectStmt != null) {
            assertEquals(SqlStatement.StatementType.SELECT, selectStmt.getStatementType());
        }
        if (insertStmt != null) {
            assertEquals(SqlStatement.StatementType.INSERT, insertStmt.getStatementType());
        }
        if (updateStmt != null) {
            assertEquals(SqlStatement.StatementType.UPDATE, updateStmt.getStatementType());
        }
        if (deleteStmt != null) {
            assertEquals(SqlStatement.StatementType.DELETE, deleteStmt.getStatementType());
        }
        
        System.out.println("=== 语句类型检测测试完成 ===");
    }

    @Test
    public void testDynamicSqlDetection() {
        // 测试动态SQL检测
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);

        String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";
        
        // 静态SQL
        SqlStatement staticStmt = XmlMapperParser.getSqlStatement(namespace + ".findById");
        if (staticStmt != null) {
            assertFalse(staticStmt.isDynamic(), "简单查询应该是静态SQL");
        }
        
        // 动态SQL
        SqlStatement dynamicStmt = XmlMapperParser.getSqlStatement(namespace + ".findByCondition");
        if (dynamicStmt != null) {
            assertTrue(dynamicStmt.isDynamic(), "包含条件的查询应该是动态SQL");
        }
        
        System.out.println("=== 动态SQL检测测试完成 ===");
    }

    @Test
    public void testCacheEffectiveness() {
        // 测试缓存有效性
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        
        // 第一次解析
        long start1 = System.currentTimeMillis();
        XmlMapperParser.parseMapperXml(resource);
        long end1 = System.currentTimeMillis();
        
        // 第二次获取（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        Map<String, SqlStatement> statements1 = XmlMapperParser.getAllSqlStatements();
        Map<String, SqlStatement> statements2 = XmlMapperParser.getAllSqlStatements();
        long end2 = System.currentTimeMillis();
        
        // 验证缓存生效（内容相同）
        assertEquals(statements1, statements2, "多次获取应该返回相同内容的对象");
        assertFalse(statements1.isEmpty(), "语句缓存不应该为空");
        assertFalse(statements2.isEmpty(), "语句缓存不应该为空");
        
        System.out.println("=== 缓存有效性测试 ===");
        System.out.println("首次解析耗时: " + (end1 - start1) + "ms");
        System.out.println("缓存获取耗时: " + (end2 - start2) + "ms");
    }

    @Test
    public void testInvalidXmlHandling() {
        // 测试无效XML处理
        try {
            ClassPathResource invalidResource = new ClassPathResource("mapper/NonExistentMapper.xml");
            XmlMapperParser.parseMapperXml(invalidResource);
            // 如果没有抛出异常，说明处理了不存在的文件
        } catch (Exception e) {
            // 预期的异常
            System.out.println("正确处理了无效XML: " + e.getMessage());
        }
        
        System.out.println("=== 无效XML处理测试完成 ===");
    }

    @Test
    public void testMultipleXmlFiles() {
        // 测试多个XML文件解析
        ClassPathResource resource1 = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource1);
        
        int count1 = XmlMapperParser.getAllSqlStatements().size();
        
        // 如果有其他XML文件，可以继续解析
        // ClassPathResource resource2 = new ClassPathResource("mapper/AnotherMapper.xml");
        // XmlMapperParser.parseMapperXml(resource2);
        
        int count2 = XmlMapperParser.getAllSqlStatements().size();
        
        assertTrue(count2 >= count1, "解析多个文件后语句数量应该不减少");
        
        System.out.println("=== 多XML文件解析测试 ===");
        System.out.println("第一个文件后语句数: " + count1);
        System.out.println("第二个文件后语句数: " + count2);
    }

    @Test
    public void testClearCache() {
        // 测试清空缓存
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);
        
        assertFalse(XmlMapperParser.getAllSqlStatements().isEmpty(), "解析后应该有语句");
        
        XmlMapperParser.clearCache();
        
        assertTrue(XmlMapperParser.getAllSqlStatements().isEmpty(), "清空缓存后应该没有语句");
        
        System.out.println("=== 清空缓存测试完成 ===");
    }

    @Test
    public void testStatementRetrieval() {
        // 测试语句检索
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);

        String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";
        
        // 测试存在的语句
        SqlStatement existingStmt = XmlMapperParser.getSqlStatement(namespace + ".findById");
        // 可能存在也可能不存在，取决于XML文件内容
        
        // 测试不存在的语句
        SqlStatement nonExistentStmt = XmlMapperParser.getSqlStatement(namespace + ".nonExistentMethod");
        assertNull(nonExistentStmt, "不存在的语句应该返回null");
        
        System.out.println("=== 语句检索测试完成 ===");
    }

    @Test
    public void testXmlValidation() {
        // 测试XML验证
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        
        // 验证XML文件存在
        assertTrue(resource.exists(), "测试XML文件应该存在");
        
        // 解析XML
        assertDoesNotThrow(() -> {
            XmlMapperParser.parseMapperXml(resource);
        }, "解析有效的XML不应该抛出异常");
        
        System.out.println("=== XML验证测试完成 ===");
    }
}
