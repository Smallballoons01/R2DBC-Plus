package com.scrm.r2dbc.plus.test.functional.crud;

import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestUser;
import com.scrm.r2dbc.plus.test.mapper.TestUserMapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询测试
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
@Tag("functional")
@Tag("crud")
@DisplayName("分页查询测试")
public class PageQueryTest {

    @Autowired
    private TestUserMapper userMapper;

    @Autowired
    private DatabaseClient databaseClient;

    private ServiceImpl<TestUserMapper, TestUser> userService;

    @BeforeEach
    void setUp() {
        userService = new ServiceImpl<>();
        userService.setBaseMapper(userMapper);
        userService.setEntityClass(TestUser.class);
        
        // 清理测试数据
        databaseClient.sql("DELETE FROM test_user WHERE username LIKE 'page_test_%'")
                .fetch()
                .rowsUpdated()
                .block();
        
        // 插入测试数据
        List<TestUser> testUsers = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            TestUser user = new TestUser("page_test_user" + i, "user" + i + "@page.test", 20 + i);
            user.setId(null);
            testUsers.add(user);
        }
        
        // 批量插入测试数据
        userMapper.insertBatch(testUsers).block();
        System.out.println("插入了 " + testUsers.size() + " 条测试数据");
    }

    @Test
    @DisplayName("测试分页查询 - 传入null查询条件")
    void testPageQueryWithNullWrapper() {
        // 第一页，每页10条
        Page<TestUser> page1 = new Page<>(1, 10);
        
        StepVerifier.create(userService.page(page1, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("第一页结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().size() == 10 &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getCurrent() == 1 &&
                           pageResult.getSize() == 10;
                })
                .verifyComplete();

        // 第二页，每页10条
        Page<TestUser> page2 = new Page<>(2, 10);
        
        StepVerifier.create(userService.page(page2, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("第二页结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().size() == 10 &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getCurrent() == 2 &&
                           pageResult.getSize() == 10;
                })
                .verifyComplete();

        // 第三页，每页10条（应该有5条记录）
        Page<TestUser> page3 = new Page<>(3, 10);
        
        StepVerifier.create(userService.page(page3, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("第三页结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().size() >= 5 &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getCurrent() == 3 &&
                           pageResult.getSize() == 10;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("测试分页查询 - 传入QueryWrapper查询条件")
    void testPageQueryWithQueryWrapper() {
        // 创建查询条件：用户名包含 "page_test_user1"
        QueryWrapper<TestUser> queryWrapper = new QueryWrapper<>(TestUser.class)
                .like("username", "page_test_user1");

        Page<TestUser> page = new Page<>(1, 5);
        
        StepVerifier.create(userService.page(page, queryWrapper))
                .expectNextMatches(pageResult -> {
                    System.out.println("条件查询结果: " + pageResult);
                    // 应该匹配 page_test_user1, page_test_user10, page_test_user11, ..., page_test_user19
                    return pageResult != null &&
                           pageResult.getRecords().size() <= 5 &&
                           pageResult.getTotal() >= 10 && // 至少有10个匹配的用户
                           pageResult.getCurrent() == 1 &&
                           pageResult.getSize() == 5;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("测试分页查询 - 空结果")
    void testPageQueryWithEmptyResult() {
        // 创建查询条件：查询不存在的用户
        QueryWrapper<TestUser> queryWrapper = new QueryWrapper<>(TestUser.class)
                .eq("username", "non_existent_user");

        Page<TestUser> page = new Page<>(1, 10);
        
        StepVerifier.create(userService.page(page, queryWrapper))
                .expectNextMatches(pageResult -> {
                    System.out.println("空结果查询: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().isEmpty() &&
                           pageResult.getTotal() == 0 &&
                           pageResult.getCurrent() == 1 &&
                           pageResult.getSize() == 10;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("测试分页查询 - 大页码")
    void testPageQueryWithLargePageNumber() {
        // 查询超出范围的页码
        Page<TestUser> page = new Page<>(100, 10);
        
        StepVerifier.create(userService.page(page, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("大页码查询结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().isEmpty() &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getCurrent() == 100 &&
                           pageResult.getSize() == 10;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("测试分页查询 - 不同页面大小")
    void testPageQueryWithDifferentPageSizes() {
        // 每页5条
        Page<TestUser> page5 = new Page<>(1, 5);
        
        StepVerifier.create(userService.page(page5, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("每页5条结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().size() == 5 &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getPages() >= 5;
                })
                .verifyComplete();

        // 每页20条
        Page<TestUser> page20 = new Page<>(1, 20);
        
        StepVerifier.create(userService.page(page20, null))
                .expectNextMatches(pageResult -> {
                    System.out.println("每页20条结果: " + pageResult);
                    return pageResult != null &&
                           pageResult.getRecords().size() == 20 &&
                           pageResult.getTotal() >= 25 &&
                           pageResult.getPages() >= 2;
                })
                .verifyComplete();
    }
}
