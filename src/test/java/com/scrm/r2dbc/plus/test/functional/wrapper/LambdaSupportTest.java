package com.scrm.r2dbc.plus.test.functional.wrapper;

import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import com.scrm.r2dbc.plus.conditions.update.UpdateWrapper;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Lambda 支持测试
 * 验证 AbstractWrapper 中的 Lambda 方法是否正常工作
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class LambdaSupportTest {

    @Test
    public void testQueryWrapperLambdaSupport() {
        // 测试 QueryWrapper 的 Lambda 支持
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        // 这些调用现在应该使用 AbstractWrapper 中的 Lambda 方法
        queryWrapper.lambda()
                .eq(TestAccount::getNickname, "test")
                .ne(TestAccount::getStatus, 0)
                .gt(TestAccount::getId, 1L)
                .in(TestAccount::getGroupId, Arrays.asList(1L, 2L, 3L))
                .isNotNull(TestAccount::getGmtCreate)
                .like(TestAccount::getNickname, "test")
                .between(TestAccount::getId, 1L, 100L);

        System.out.println("QueryWrapper Lambda 测试通过");
        System.out.println("SQL: " + queryWrapper.getSqlSegment());
    }

    @Test
    public void testUpdateWrapperLambdaSupport() {
        // 测试 UpdateWrapper 的 Lambda 支持
        UpdateWrapper<TestAccount> updateWrapper = new UpdateWrapper<>();
        
        // 这些调用现在应该使用新添加的 Lambda 方法
        updateWrapper.lambda()
                .set(TestAccount::getNickname, "newName")
                .set(TestAccount::getStatus, 1)
                .eq(TestAccount::getId, 1L)
                .in(TestAccount::getGroupId, Arrays.asList(1L, 2L))
                .isNotNull(TestAccount::getGmtCreate);

        System.out.println("UpdateWrapper Lambda 测试通过");
        System.out.println("SET SQL: " + updateWrapper.getSetSegment());
        System.out.println("WHERE SQL: " + updateWrapper.getSqlSegment());
    }

    @Test
    public void testConditionalLambdaSupport() {
        // 测试条件 Lambda 支持
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        
        String name = "test";
        Long groupId = null;
        
        queryWrapper.lambda()
                .eq(name != null, TestAccount::getNickname, name)
                .eq(groupId != null, TestAccount::getGroupId, groupId)
                .isNotNull(TestAccount::getGmtCreate);

        System.out.println("条件 Lambda 测试通过");
        System.out.println("SQL: " + queryWrapper.getSqlSegment());
    }
}
