package com.scrm.r2dbc.plus.test.functional.wrapper;

import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * apply 方法功能测试
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class ApplyMethodTest {

    @Test
    @DisplayName("测试基础 apply 方法 - 使用占位符")
    public void testApplyWithPlaceholders() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .apply("age > {0} AND name LIKE {1}", 18, "%test%");

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("SQL 片段: " + sqlSegment);
        System.out.println("参数映射: " + params);

        // 验证 SQL 片段包含正确的条件
        assertTrue(sqlSegment.contains("status = :param"));
        assertTrue(sqlSegment.contains("age > :param"));
        assertTrue(sqlSegment.contains("name LIKE :param"));
        
        // 验证参数正确存储
        assertTrue(params.containsValue(1));
        assertTrue(params.containsValue(18));
        assertTrue(params.containsValue("%test%"));
    }

    @Test
    @DisplayName("测试 apply 方法 - FIELD 函数场景（直接字符串拼接）")
    public void testApplyWithFieldFunction() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        // 模拟用户场景：排序用的字符串列表
        List<String> statisticsSort = Arrays.asList("VIP客户", "普通客户", "潜在客户");
        String nameOrder = String.join("','", statisticsSort);
        
        queryWrapper.eq("status", 1)
                   .apply("FIELD(name, '" + nameOrder + "')");

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("FIELD 函数 SQL 片段: " + sqlSegment);
        System.out.println("FIELD 函数参数映射: " + params);

        // 验证 SQL 片段包含 FIELD 函数
        assertTrue(sqlSegment.contains("FIELD(name, 'VIP客户','普通客户','潜在客户')"));
        assertTrue(sqlSegment.contains("status = :param"));
        
        // 验证 status 参数正确存储
        assertTrue(params.containsValue(1));
    }

    @Test
    @DisplayName("测试 apply 方法 - 混合使用占位符和直接拼接")
    public void testApplyMixedUsage() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        // 先使用占位符
        queryWrapper.apply("age > {0}", 18);
        
        // 再使用直接拼接（模拟复杂场景）
        String customCondition = "DATE_FORMAT(create_time, '%Y-%m-%d') = '2024-01-01'";
        queryWrapper.apply(customCondition);

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("混合使用 SQL 片段: " + sqlSegment);
        System.out.println("混合使用参数映射: " + params);

        // 验证两个条件都被正确添加
        assertTrue(sqlSegment.contains("age > :param"));
        assertTrue(sqlSegment.contains("DATE_FORMAT(create_time, '%Y-%m-%d') = '2024-01-01'"));
        assertTrue(params.containsValue(18));
    }

    @Test
    @DisplayName("测试 apply 方法 - 带条件判断")
    public void testApplyWithCondition() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        boolean hasAgeFilter = true;
        boolean hasNameFilter = false;
        
        queryWrapper.eq("status", 1)
                   .apply(hasAgeFilter, "age > {0}", 25)
                   .apply(hasNameFilter, "name LIKE {0}", "%admin%");

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("条件判断 SQL 片段: " + sqlSegment);
        System.out.println("条件判断参数映射: " + params);

        // 验证只有条件为 true 的 apply 生效
        assertTrue(sqlSegment.contains("status = :param"));
        assertTrue(sqlSegment.contains("age > :param"));
        assertFalse(sqlSegment.contains("name LIKE"));
        
        assertTrue(params.containsValue(1));
        assertTrue(params.containsValue(25));
        assertFalse(params.containsValue("%admin%"));
    }

    @Test
    @DisplayName("测试 LambdaQueryWrapper 的 apply 方法")
    public void testLambdaQueryWrapperApply() {
        LambdaQueryWrapper<TestAccount> lambdaQuery = new LambdaQueryWrapper<>();
        
        lambdaQuery.eq(TestAccount::getStatus, 1)
                  .apply("YEAR(create_time) = {0}", 2024);

        String sqlSegment = lambdaQuery.getSqlSegment();
        Map<String, Object> params = lambdaQuery.getParamNameValuePairs();

        System.out.println("Lambda apply SQL 片段: " + sqlSegment);
        System.out.println("Lambda apply 参数映射: " + params);

        // 验证 Lambda 版本也能正确继承 apply 方法
        assertTrue(sqlSegment.contains("status = :param"));
        assertTrue(sqlSegment.contains("YEAR(create_time) = :param"));
        assertTrue(params.containsValue(1));
        assertTrue(params.containsValue(2024));
    }

    @Test
    @DisplayName("测试 apply 方法 - 空值和边界情况")
    public void testApplyEdgeCases() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        // 测试空字符串
        queryWrapper.eq("status", 1)
                   .apply("")  // 空字符串应该被忽略
                   .apply(null)  // null 应该被忽略
                   .apply("   ")  // 只有空格的字符串应该被忽略
                   .apply("name IS NOT NULL");  // 正常条件

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("边界情况 SQL 片段: " + sqlSegment);
        System.out.println("边界情况参数映射: " + params);

        // 验证只有有效的条件被添加
        assertTrue(sqlSegment.contains("status = :param"));
        assertTrue(sqlSegment.contains("name IS NOT NULL"));
        assertTrue(params.containsValue(1));
        
        // 验证不包含多余的 AND
        assertFalse(sqlSegment.contains("AND AND"));
    }

    @Test
    @DisplayName("测试 apply 方法 - 复杂 SQL 函数")
    public void testApplyComplexSqlFunctions() {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        // 测试复杂的 SQL 函数和子查询
        queryWrapper.apply("account_id IN (SELECT id FROM accounts WHERE type = {0})", "VIP")
                   .apply("DATEDIFF(NOW(), create_time) <= {0}", 30)
                   .apply("JSON_EXTRACT(metadata, '$.level') = {0}", "premium");

        String sqlSegment = queryWrapper.getSqlSegment();
        Map<String, Object> params = queryWrapper.getParamNameValuePairs();

        System.out.println("复杂函数 SQL 片段: " + sqlSegment);
        System.out.println("复杂函数参数映射: " + params);

        // 验证复杂 SQL 被正确处理
        assertTrue(sqlSegment.contains("account_id IN (SELECT id FROM accounts WHERE type = :param"));
        assertTrue(sqlSegment.contains("DATEDIFF(NOW(), create_time) <= :param"));
        assertTrue(sqlSegment.contains("JSON_EXTRACT(metadata, '$.level') = :param"));
        
        assertTrue(params.containsValue("VIP"));
        assertTrue(params.containsValue(30));
        assertTrue(params.containsValue("premium"));
    }
} 