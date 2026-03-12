package com.scrm.r2dbc.plus.test.performance;

import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestUser;
import com.scrm.r2dbc.plus.test.mapper.TestUserMapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 性能测试
 * 测试R2DBC Plus框架在各种场景下的性能表现
 */
@ActiveProfiles("mysql")
@SpringBootTest(classes = TestConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("performance")
@Tag("slow")
@Tag("integration")
public class PerformanceTest {

    @Autowired
    private TestUserMapper userMapper;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private TransactionalOperator transactionalOperator;

    private static final int LARGE_DATA_SIZE = 100;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        databaseClient.sql("DELETE FROM test_user").fetch().rowsUpdated().block();
    }


    /**
     * 测试批量插入性能
     */
    @Test
    @Order(1)
    void testBatchInsertPerformance() {
        List<TestUser> users = createTestUsers(LARGE_DATA_SIZE);

        long startTime = System.currentTimeMillis();

        // 使用批量插入方法
        userMapper.insertBatch(users, 100).block();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("批量插入 %d 条记录耗时: %d ms, 平均每条: %.2f ms%n",
                LARGE_DATA_SIZE, duration, (double) duration / LARGE_DATA_SIZE);

        // 验证插入结果
        StepVerifier.create(
                        userMapper.selectCount(new QueryWrapper<>()).doOnNext(count -> {
                            System.out.println("插入记录数: " + count);
                        })
                )
                .expectNext((long) LARGE_DATA_SIZE)
                .verifyComplete();

        // 性能断言（根据实际环境调整）
        Assertions.assertTrue(duration < 30000, "批量插入性能不达标，耗时: " + duration + "ms");
    }


    /**
     * 创建测试用户数据
     */
    private List<TestUser> createTestUsers(int count) {
        return createTestUsers(count, "");
    }

    /**
     * 创建测试用户数据（带前缀）
     */
    private List<TestUser> createTestUsers(int count, String prefix) {
        List<TestUser> users = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            TestUser user = new TestUser(
                    prefix + "user" + String.format("%04d", i),
                    prefix + "user" + i + "@example.com",
                    20 + (i % 40) // 年龄在20-59之间
            )
                    .setEnabled(i % 2 == 0) // 交替设置启用状态
                    .setCreatedTime(baseTime.plusSeconds(i))
                    .setUpdatedTime(baseTime.plusSeconds(i));

            users.add(user);
        }

        return users;
    }

    /**
     * 测试事务提交性能
     */
    @Test
    @Order(2)
    void testTransactionCommitPerformance() {
        List<TestUser> users = createTestUsers(100, "tx_commit_");

        long startTime = System.currentTimeMillis();

        // 使用事务操作符进行事务管理
        Mono<Integer> result = userMapper.insertBatch(users, 50)
                .as(transactionalOperator::transactional);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("事务提交批量插入 %d 条记录耗时: %d ms%n", users.size(), duration);

        // 验证事务提交结果
        StepVerifier.create(result)
                .expectNext(users.size())
                .verifyComplete();

        // 验证数据确实插入了
        StepVerifier.create(
                        userMapper.selectCount(new QueryWrapper<TestUser>(TestUser.class)
                                .like("username", "tx_commit_"))
                )
                .expectNext((long) users.size())
                .verifyComplete();
    }

    /**
     * 测试事务回滚性能
     */
    @Test
    @Order(3)
    void testTransactionRollbackPerformance() {
        List<TestUser> users = createTestUsers(100, "tx_rollback_");

        long startTime = System.currentTimeMillis();

        // 模拟事务回滚场景
        Mono<Object> result = userMapper.insertBatch(users, 50)
                .flatMap(count -> {
                    // 模拟业务异常，触发回滚
                    return Mono.error(new RuntimeException("模拟业务异常，触发事务回滚"));
                })
                .as(transactionalOperator::transactional)
                .onErrorReturn(-1); // 捕获异常，返回错误标识

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("事务回滚批量插入 %d 条记录耗时: %d ms%n", users.size(), duration);

        // 验证事务回滚结果
        StepVerifier.create(result)
                .expectNext(-1) // 期望返回错误标识
                .verifyComplete();

        // 验证数据没有插入（事务已回滚）
        StepVerifier.create(
                        userMapper.selectCount(new QueryWrapper<TestUser>(TestUser.class)
                                .like("username", "tx_rollback_"))
                )
                .expectNext(0L) // 期望没有数据
                .verifyComplete();
    }

    /**
     * 测试嵌套事务性能
     */
    @Test
    @Order(5)
    void testNestedTransactionPerformance() {
        List<TestUser> batch1 = createTestUsers(50, "nested_tx_1_");
        List<TestUser> batch2 = createTestUsers(50, "nested_tx_2_");

        long startTime = System.currentTimeMillis();

        // 外层事务
        Mono<String> result = userMapper.insertBatch(batch1, 25)
                .flatMap(count1 -> {
                    System.out.println("第一批插入完成: " + count1);

                    // 内层事务
                    return userMapper.insertBatch(batch2, 25)
                            .flatMap(count2 -> {
                                System.out.println("第二批插入完成: " + count2);

                                // 模拟内层事务成功
                                if (count2 == batch2.size()) {
                                    return Mono.just("嵌套事务成功");
                                } else {
                                    return Mono.error(new RuntimeException("内层事务失败"));
                                }
                            });
                })
                .as(transactionalOperator::transactional);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("嵌套事务批量插入 %d 条记录耗时: %d ms%n",
                batch1.size() + batch2.size(), duration);

        // 验证嵌套事务结果
        StepVerifier.create(result)
                .expectNext("嵌套事务成功")
                .verifyComplete();

        // 验证所有数据都插入了
        StepVerifier.create(
                        userMapper.selectCount(new QueryWrapper<TestUser>(TestUser.class)
                                .like("username", "nested_tx_"))
                )
                .expectNext((long) (batch1.size() + batch2.size()))
                .verifyComplete();
    }

    /**
     * 测试并发事务性能
     */
    @Test
    @Order(6)
    void testConcurrentTransactionPerformance() {
        int concurrentCount = 5;
        int batchSize = 20;

        long startTime = System.currentTimeMillis();

        // 创建多个并发事务
        List<Mono<Integer>> concurrentTransactions = new ArrayList<>();
        for (int i = 0; i < concurrentCount; i++) {
            List<TestUser> users = createTestUsers(batchSize, "concurrent_tx_" + i + "_");

            Mono<Integer> transaction = userMapper.insertBatch(users, 10)
                    .as(transactionalOperator::transactional);

            concurrentTransactions.add(transaction);
        }

        // 并发执行所有事务
        Mono<List<Integer>> allResults = Mono.zip(concurrentTransactions,
                results -> {
                    List<Integer> resultList = new ArrayList<>();
                    for (Object result : results) {
                        resultList.add((Integer) result);
                    }
                    return resultList;
                });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("并发事务 %d 个事务，每个插入 %d 条记录，总耗时: %d ms%n",
                concurrentCount, batchSize, duration);

        // 验证并发事务结果
        StepVerifier.create(allResults)
                .expectNextMatches(results -> {
                    return results.size() == concurrentCount &&
                           results.stream().allMatch(count -> count == batchSize);
                })
                .verifyComplete();

        // 验证总数据量
        StepVerifier.create(
                        userMapper.selectCount(new QueryWrapper<TestUser>(TestUser.class)
                                .like("username", "concurrent_tx_"))
                )
                .expectNext((long) (concurrentCount * batchSize))
                .verifyComplete();
    }
}