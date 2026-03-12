package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.SqlStatement;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML功能测试套件
 * 综合测试XML解析、Mapper代理、SQL处理等功能
 *
 * @author dason
 */
@DisplayName("XML功能测试套件")
@Tag("core")
@Tag("fast")
@Tag("xml")
public class XmlTestSuite {

    @Nested
    @DisplayName("XML解析测试")
    class XmlParsingTests {

        @Test
        @DisplayName("解析测试XML文件")
        void testParseTestXml() {
            // 清空缓存
            XmlMapperParser.clearCache();
            
            // 解析测试XML文件
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);

            // 验证解析结果
            Map<String, SqlStatement> statements = XmlMapperParser.getAllSqlStatements();
            assertFalse(statements.isEmpty(), "应该解析出SQL语句");

            String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";
            
            // 验证各种类型的语句都被正确解析
            assertNotNull(XmlMapperParser.getSqlStatement(namespace + ".findById"));
            assertNotNull(XmlMapperParser.getSqlStatement(namespace + ".findByCondition"));
            assertNotNull(XmlMapperParser.getSqlStatement(namespace + ".insert"));
            assertNotNull(XmlMapperParser.getSqlStatement(namespace + ".updateById"));
            assertNotNull(XmlMapperParser.getSqlStatement(namespace + ".deleteById"));

            System.out.println("解析的语句数量: " + statements.size());
        }

        @Test
        @DisplayName("验证动态SQL检测")
        void testDynamicSqlDetection() {
            XmlMapperParser.clearCache();
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);

            String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";

            // 静态SQL
            SqlStatement staticStmt = XmlMapperParser.getSqlStatement(namespace + ".findById");
            assertFalse(staticStmt.isDynamic(), "简单查询应该是静态SQL");

            // 动态SQL
            SqlStatement dynamicStmt = XmlMapperParser.getSqlStatement(namespace + ".findByCondition");
            assertTrue(dynamicStmt.isDynamic(), "包含if标签的查询应该是动态SQL");

            SqlStatement chooseStmt = XmlMapperParser.getSqlStatement(namespace + ".findByPriority");
            assertTrue(chooseStmt.isDynamic(), "包含choose标签的查询应该是动态SQL");
        }
    }

    @Nested
    @DisplayName("SQL参数处理测试")
    class SqlParameterTests {

        @Test
        @DisplayName("简单参数替换")
        void testSimpleParameterReplacement() {
            String sql = "SELECT * FROM users WHERE id = #{id} AND name = #{name}";
            SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

            Map<String, Object> params = new HashMap<>();
            params.put("id", 1);
            params.put("name", "test");

            String result = statement.processParameters(params);
            
            assertTrue(result.contains(":id"));
            assertTrue(result.contains(":name"));
            assertFalse(result.contains("#{id}"));
            assertFalse(result.contains("#{name}"));
        }

        @Test
        @DisplayName("复杂参数处理")
        void testComplexParameters() {
            String sql = "INSERT INTO users (id, name, email, age) VALUES (#{user.id}, #{user.name}, #{user.email}, #{user.age})";
            SqlStatement statement = new SqlStatement("insert", sql, SqlStatement.StatementType.INSERT);

            Map<String, Object> params = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("id", 1);
            user.put("name", "张三");
            user.put("email", "zhangsan@test.com");
            user.put("age", 25);
            params.put("user", user);

            String result = statement.processParameters(params);
            // 嵌套参数应该被转换为扁平化的参数名
            assertTrue(result.contains(":user_id"));
            assertTrue(result.contains(":user_name"));
            assertTrue(result.contains(":user_email"));
            assertTrue(result.contains(":user_age"));
        }

        @Test
        @DisplayName("空参数处理")
        void testNullParameters() {
            String sql = "SELECT * FROM users";
            SqlStatement statement = new SqlStatement("select", sql, SqlStatement.StatementType.SELECT);

            String result1 = statement.processParameters(null);
            String result2 = statement.processParameters(new HashMap<>());

            assertEquals(sql, result1);
            assertEquals(sql, result2);
        }
    }

    @Nested
    @DisplayName("语句类型测试")
    class StatementTypeTests {

        @Test
        @DisplayName("验证所有语句类型")
        void testAllStatementTypes() {
            XmlMapperParser.clearCache();
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);

            String namespace = "com.scrm.r2dbc.plus.test.mapper.TestMapper";

            // SELECT语句
            SqlStatement selectStmt = XmlMapperParser.getSqlStatement(namespace + ".findById");
            assertEquals(SqlStatement.StatementType.SELECT, selectStmt.getStatementType());

            // INSERT语句
            SqlStatement insertStmt = XmlMapperParser.getSqlStatement(namespace + ".insert");
            assertEquals(SqlStatement.StatementType.INSERT, insertStmt.getStatementType());

            // UPDATE语句
            SqlStatement updateStmt = XmlMapperParser.getSqlStatement(namespace + ".updateById");
            assertEquals(SqlStatement.StatementType.UPDATE, updateStmt.getStatementType());

            // DELETE语句
            SqlStatement deleteStmt = XmlMapperParser.getSqlStatement(namespace + ".deleteById");
            assertEquals(SqlStatement.StatementType.DELETE, deleteStmt.getStatementType());
        }

        @Test
        @DisplayName("统计语句类型分布")
        void testStatementTypeDistribution() {
            XmlMapperParser.clearCache();
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);

            Map<String, SqlStatement> statements = XmlMapperParser.getAllSqlStatements();
            
            Map<SqlStatement.StatementType, Long> typeCounts = new EnumMap<>(SqlStatement.StatementType.class);
            statements.values().forEach(stmt -> 
                typeCounts.merge(stmt.getStatementType(), 1L, Long::sum));

            System.out.println("=== 语句类型分布 ===");
            typeCounts.forEach((type, count) -> 
                System.out.println(type + ": " + count + "个"));

            // 验证每种类型都有
            assertTrue(typeCounts.get(SqlStatement.StatementType.SELECT) > 0);
            assertTrue(typeCounts.get(SqlStatement.StatementType.INSERT) > 0);
            assertTrue(typeCounts.get(SqlStatement.StatementType.UPDATE) > 0);
            assertTrue(typeCounts.get(SqlStatement.StatementType.DELETE) > 0);
        }
    }

    @Nested
    @DisplayName("缓存机制测试")
    class CacheTests {

        @Test
        @DisplayName("验证缓存生效")
        void testCacheEffectiveness() {
            XmlMapperParser.clearCache();
            
            // 第一次解析
            long start1 = System.currentTimeMillis();
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);
            long end1 = System.currentTimeMillis();

            // 获取语句（应该从缓存获取）
            long start2 = System.currentTimeMillis();
            String namespace = "com.scrm.r2dbc.plus.test.TestMapper";
            SqlStatement stmt1 = XmlMapperParser.getSqlStatement(namespace + ".findById");
            SqlStatement stmt2 = XmlMapperParser.getSqlStatement(namespace + ".findById");
            long end2 = System.currentTimeMillis();

            // 验证返回同一个对象
            assertSame(stmt1, stmt2, "多次获取应该返回缓存的同一个对象");

            System.out.println("解析耗时: " + (end1 - start1) + "ms");
            System.out.println("缓存获取耗时: " + (end2 - start2) + "ms");
        }

        @Test
        @DisplayName("清空缓存功能")
        void testCacheClear() {
            // 先解析一些数据
            ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
            XmlMapperParser.parseMapperXml(resource);
            
            assertFalse(XmlMapperParser.getAllSqlStatements().isEmpty());

            // 清空缓存
            XmlMapperParser.clearCache();
            
            assertTrue(XmlMapperParser.getAllSqlStatements().isEmpty());
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("不存在的语句ID")
        void testNonExistentStatementId() {
            SqlStatement stmt = XmlMapperParser.getSqlStatement("non.existent.statement");
            assertNull(stmt, "不存在的语句ID应该返回null");
        }
    }

    @Nested
    @DisplayName("性能测试")
    @Tag("slow")
    class PerformanceTests {

        @Test
        @DisplayName("大量语句解析性能")
        void testLargeScaleParsing() {
            XmlMapperParser.clearCache();
            
            long startTime = System.currentTimeMillis();
            
            // 解析所有XML文件
            XmlMapperParser.parseMapperXmls("classpath*:mapper/**/*.xml");
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, SqlStatement> statements = XmlMapperParser.getAllSqlStatements();
            
            System.out.println("=== 性能测试结果 ===");
            System.out.println("解析耗时: " + duration + "ms");
            System.out.println("语句数量: " + statements.size());
            System.out.println("平均每个语句: " + (statements.isEmpty() ? 0 : duration / statements.size()) + "ms");
            
            // 验证性能在合理范围内
            assertTrue(duration < 10000, "解析时间应该在10秒以内");
        }
    }
}
