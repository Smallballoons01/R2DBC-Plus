package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.SqlStatement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlStatement 单元测试
 * 测试SQL语句处理功能
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("xml")
@Tag("unit")
public class SqlStatementUnitTest {

    @Test
    public void testStaticSqlProcessing() {
        // 测试静态SQL处理
        String sql = "SELECT * FROM users WHERE id = #{id} AND name = #{name}";
        SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

        // 准备参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 1);
        parameters.put("name", "test");

        // 处理参数
        String result = statement.processParameters(parameters);
        
        // 验证参数替换
        assertTrue(result.contains(":id"), "应该包含 :id 参数");
        assertTrue(result.contains(":name"), "应该包含 :name 参数");
        assertFalse(result.contains("#{id}"), "不应该包含 #{id}");
        assertFalse(result.contains("#{name}"), "不应该包含 #{name}");
        
        System.out.println("=== 静态SQL处理测试 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("处理后: " + result);
    }

    @Test
    public void testComplexParameterProcessing() {
        // 测试复杂参数处理
        String sql = "INSERT INTO users (id, name, email, age) VALUES (#{user.id}, #{user.name}, #{user.email}, #{user.age})";
        SqlStatement statement = new SqlStatement("insert", sql, SqlStatement.StatementType.INSERT);

        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("name", "张三");
        user.put("email", "zhangsan@test.com");
        user.put("age", 25);
        parameters.put("user", user);

        String result = statement.processParameters(parameters);
        
        // 验证嵌套参数处理
        assertTrue(result.contains(":user_id"), "应该包含扁平化的参数名");
        assertTrue(result.contains(":user_name"), "应该包含扁平化的参数名");
        assertTrue(result.contains(":user_email"), "应该包含扁平化的参数名");
        assertTrue(result.contains(":user_age"), "应该包含扁平化的参数名");
        
        System.out.println("=== 复杂参数处理测试 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("处理后: " + result);
    }

    @Test
    public void testStatementTypes() {
        // 测试语句类型
        SqlStatement selectStmt = new SqlStatement("select", "SELECT * FROM users", SqlStatement.StatementType.SELECT);
        SqlStatement insertStmt = new SqlStatement("insert", "INSERT INTO users VALUES (1)", SqlStatement.StatementType.INSERT);
        SqlStatement updateStmt = new SqlStatement("update", "UPDATE users SET name = 'test'", SqlStatement.StatementType.UPDATE);
        SqlStatement deleteStmt = new SqlStatement("delete", "DELETE FROM users", SqlStatement.StatementType.DELETE);

        assertEquals(SqlStatement.StatementType.SELECT, selectStmt.getStatementType());
        assertEquals(SqlStatement.StatementType.INSERT, insertStmt.getStatementType());
        assertEquals(SqlStatement.StatementType.UPDATE, updateStmt.getStatementType());
        assertEquals(SqlStatement.StatementType.DELETE, deleteStmt.getStatementType());
        
        System.out.println("=== 语句类型测试完成 ===");
    }

    @Test
    public void testDynamicSqlDetection() {
        // 测试动态SQL检测
        String staticSql = "SELECT * FROM users WHERE id = #{id}";
        String dynamicSql = "SELECT * FROM users <where><if test='id != null'>id = #{id}</if></where>";

        SqlStatement staticStmt = new SqlStatement("static", staticSql, SqlStatement.StatementType.SELECT);
        SqlStatement dynamicStmt = new SqlStatement("dynamic", dynamicSql, SqlStatement.StatementType.SELECT);

        assertFalse(staticStmt.isDynamic(), "静态SQL应该被正确识别");
        assertTrue(dynamicStmt.isDynamic(), "动态SQL应该被正确识别");
        
        System.out.println("=== 动态SQL检测测试完成 ===");
    }

    @Test
    public void testEmptyParameters() {
        // 测试空参数处理
        String sql = "SELECT * FROM users";
        SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

        String result1 = statement.processParameters(null);
        String result2 = statement.processParameters(new HashMap<>());

        assertEquals(sql, result1, "空参数应该返回原始SQL");
        assertEquals(sql, result2, "空参数应该返回原始SQL");
        
        System.out.println("=== 空参数处理测试完成 ===");
    }

    @Test
    public void testParameterWithSpecialCharacters() {
        // 测试包含特殊字符的参数
        String sql = "SELECT * FROM users WHERE name = #{name} AND email = #{email}";
        SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "张三's account");
        parameters.put("email", "test@example.com");

        String result = statement.processParameters(parameters);
        
        assertTrue(result.contains(":name"), "应该正确处理特殊字符参数");
        assertTrue(result.contains(":email"), "应该正确处理邮箱参数");
        
        System.out.println("=== 特殊字符参数测试完成 ===");
    }

    @Test
    public void testMultipleParameterOccurrences() {
        // 测试同一参数多次出现
        String sql = "SELECT * FROM users WHERE (name = #{name} OR nickname = #{name}) AND status = #{status}";
        SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "test");
        parameters.put("status", 1);

        String result = statement.processParameters(parameters);
        
        // 验证同一参数的多次出现都被正确替换
        int nameCount = result.split(":name", -1).length - 1;
        assertEquals(2, nameCount, "参数name应该出现2次");
        
        System.out.println("=== 多次参数出现测试完成 ===");
        System.out.println("处理后SQL: " + result);
    }

    @Test
    public void testSqlStatementEquality() {
        // 测试SqlStatement相等性
        SqlStatement stmt1 = new SqlStatement("test", "SELECT * FROM users", SqlStatement.StatementType.SELECT);
        SqlStatement stmt2 = new SqlStatement("test", "SELECT * FROM users", SqlStatement.StatementType.SELECT);
        SqlStatement stmt3 = new SqlStatement("test2", "SELECT * FROM users", SqlStatement.StatementType.SELECT);

        assertEquals(stmt1.getId(), stmt2.getId(), "相同ID的语句应该相等");
        assertNotEquals(stmt1.getId(), stmt3.getId(), "不同ID的语句应该不相等");
        
        System.out.println("=== 语句相等性测试完成 ===");
    }

    @Test
    public void testSqlStatementToString() {
        // 测试toString方法
        SqlStatement statement = new SqlStatement("testId", "SELECT * FROM users WHERE id = #{id}", SqlStatement.StatementType.SELECT);
        
        String toString = statement.toString();
        assertTrue(toString.contains("testId"), "toString应该包含ID");
        assertTrue(toString.contains("SELECT"), "toString应该包含语句类型");
        
        System.out.println("=== toString测试 ===");
        System.out.println("toString结果: " + toString);
    }

    @Test
    public void testParameterValidation() {
        // 测试参数验证
        String sql = "SELECT * FROM users WHERE id = #{id} AND name = #{name}";
        SqlStatement statement = new SqlStatement("test", sql, SqlStatement.StatementType.SELECT);

        // 测试缺少参数的情况
        Map<String, Object> incompleteParams = new HashMap<>();
        incompleteParams.put("id", 1);
        // 缺少name参数

        String result = statement.processParameters(incompleteParams);
        
        // 应该只替换存在的参数
        assertTrue(result.contains(":id"), "存在的参数应该被替换");
        assertTrue(result.contains("#{name}"), "不存在的参数应该保持原样");
        
        System.out.println("=== 参数验证测试完成 ===");
        System.out.println("处理结果: " + result);
    }
}
