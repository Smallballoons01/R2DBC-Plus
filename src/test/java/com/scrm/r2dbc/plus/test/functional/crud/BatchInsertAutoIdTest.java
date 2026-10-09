package com.scrm.r2dbc.plus.test.functional.crud;

import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestUser;
import com.scrm.r2dbc.plus.test.mapper.TestUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 批量插入自增ID回填测试
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
@Tag("functional")
@Tag("crud")
@DisplayName("批量插入自增ID回填测试")
public class BatchInsertAutoIdTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private TestUserMapper userMapper;

    @BeforeEach
    void setUp() throws Exception {
        // 清理测试数据
        databaseClient.sql("DELETE FROM test_user WHERE username LIKE 'batch_test_%'")
                .fetch()
                .rowsUpdated()
                .block();
        databaseClient.sql("DELETE FROM test_user WHERE username LIKE 'single_test_%'")
                .fetch()
                .rowsUpdated()
                .block();
        databaseClient.sql("DELETE FROM test_user WHERE username LIKE '_batch_user%'")
                .fetch()
                .rowsUpdated()
                .block();

    }


    @Test
    @DisplayName("测试批量插入自增ID回填功能")
    void testBatchInsertWithAutoIdFillback() {
        // 准备测试数据
        List<TestUser> users = Arrays.asList(
            new TestUser("batch_test_user1", "user1@test.com", 25),
            new TestUser("batch_test_user2", "user2@test.com", 30),
            new TestUser("batch_test_user3", "user3@test.com", 35)
        );

        // 确保ID为null（模拟新插入的数据）
        users.forEach(user -> {
            user.setId(null);
            assertNull(user.getId(), "插入前ID应该为null");
        });

        System.out.println("=== 开始批量插入测试 ===");
        System.out.println("插入前用户ID状态:");
        for (int i = 0; i < users.size(); i++) {
            System.out.println("  " + users.get(i).getUsername() + ": ID = " + users.get(i).getId());
        }

        // 执行批量插入
        StepVerifier.create(userMapper.insertBatch(users))
                .expectNext(3) // 期望插入3条记录
                .verifyComplete();

        System.out.println("插入后用户ID状态:");
        for (int i = 0; i < users.size(); i++) {
            System.out.println("  " + users.get(i).getUsername() + ": ID = " + users.get(i).getId());
        }

        // 验证ID已经被回填
        for (int i = 0; i < users.size(); i++) {
            TestUser user = users.get(i);
            assertNotNull(user.getId(), "插入后ID应该不为null，用户: " + user.getUsername());
            assertTrue(user.getId() > 0, "ID应该大于0，用户: " + user.getUsername());
        }

        // 验证ID是唯一的
        long distinctIdCount = users.stream()
                .mapToLong(TestUser::getId)
                .distinct()
                .count();
        assertEquals(users.size(), distinctIdCount, "所有用户的ID应该是唯一的");

        // 验证数据确实插入到数据库中
        for (TestUser user : users) {
            StepVerifier.create(userMapper.selectById(user.getId()))
                    .expectNextMatches(dbUser ->
                        dbUser != null &&
                        dbUser.getUsername().equals(user.getUsername()) &&
                        dbUser.getEmail().equals(user.getEmail()) &&
                        dbUser.getAge().equals(user.getAge())
                    )
                    .verifyComplete();
        }

        System.out.println("=== 批量插入测试完成 ===");
    }

    @Test
    @DisplayName("测试单个插入自增ID回填功能（对比测试）")
    void testSingleInsertWithAutoIdFillback() {
        // 准备测试数据
        TestUser user = new TestUser("single_test_user", "single@test.com", 28);
        user.setId(null);
        assertNull(user.getId(), "插入前ID应该为null");

        // 执行单个插入
        StepVerifier.create(userMapper.insert(user))
                .expectNext(1) // 期望插入1条记录
                .verifyComplete();

        // 验证ID已经被回填
        assertNotNull(user.getId(), "插入后ID应该不为null");
        assertTrue(user.getId() > 0, "ID应该大于0");
        System.out.println("单个插入用户ID: " + user.getId());

        // 验证数据确实插入到数据库中
        StepVerifier.create(userMapper.selectById(user.getId()))
                .expectNextMatches(dbUser -> 
                    dbUser != null &&
                    dbUser.getUsername().equals(user.getUsername()) &&
                    dbUser.getEmail().equals(user.getEmail()) &&
                    dbUser.getAge().equals(user.getAge())
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("测试大批量插入自增ID回填功能")
    void testLargeBatchInsertWithAutoIdFillback() {
        // 准备大量测试数据
        List<TestUser> users = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            TestUser user = new TestUser(
                "large_batch_user" + i, 
                "user" + i + "@large.test", 
                20 + (i % 30)
            );
            user.setId(null);
            users.add(user);
        }

        // 执行批量插入
        StepVerifier.create(userMapper.insertBatch(users))
                .expectNext(50) // 期望插入50条记录
                .verifyComplete();

        // 验证所有ID都被回填
        for (TestUser user : users) {
            assertNotNull(user.getId(), "插入后ID应该不为null，用户: " + user.getUsername());
            assertTrue(user.getId() > 0, "ID应该大于0，用户: " + user.getUsername());
        }

        // 验证ID是唯一的
        long distinctIdCount = users.stream()
                .mapToLong(TestUser::getId)
                .distinct()
                .count();
        assertEquals(users.size(), distinctIdCount, "所有用户的ID应该是唯一的");

        System.out.println("大批量插入完成，ID范围: " +
            users.stream().mapToLong(TestUser::getId).min().orElse(0) +
            " - " +
            users.stream().mapToLong(TestUser::getId).max().orElse(0));
    }

    @Test
    @DisplayName("测试批量插入性能对比 - 自增ID vs 逐个插入")
    void testBatchInsertPerformanceComparison() {
        System.out.println("=== 批量插入性能对比测试 ===");

        // 测试数据量
        int testSize = 10;

        // 准备测试数据
        List<TestUser> users = new ArrayList<>();
        for (int i = 1; i <= testSize; i++) {
            TestUser user = new TestUser(
                "perf_test_user" + i,
                "perf" + i + "@test.com",
                20 + (i % 30)
            );
            user.setId(null);
            users.add(user);
        }

        // 测试批量插入（自增ID，实际上是逐个插入）
        long startTime = System.currentTimeMillis();
        StepVerifier.create(userMapper.insertBatch(users))
                .expectNext(testSize)
                .verifyComplete();
        long endTime = System.currentTimeMillis();

        System.out.println("批量插入" + testSize + "条记录（自增ID）耗时: " + (endTime - startTime) + "ms");

        // 验证所有ID都被正确回填
        for (TestUser user : users) {
            assertNotNull(user.getId(), "ID应该被回填，用户: " + user.getUsername());
            assertTrue(user.getId() > 0, "ID应该大于0，用户: " + user.getUsername());
        }

        // 验证ID唯一性
        long distinctIdCount = users.stream()
                .mapToLong(TestUser::getId)
                .distinct()
                .count();
        assertEquals(users.size(), distinctIdCount, "所有用户的ID应该是唯一的");

        System.out.println("所有ID回填验证通过");
        System.out.println("=== 性能对比测试完成 ===");
    }
}
