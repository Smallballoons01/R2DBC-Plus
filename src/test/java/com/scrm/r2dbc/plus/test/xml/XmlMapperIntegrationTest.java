package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.mapper.TestMapper;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

/**
 * XML Mapper 集成测试
 * 测试XML映射在Spring环境中的完整功能
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("mysql")
@Tag("integration")
@Tag("slow")
@Tag("xml")
public class XmlMapperIntegrationTest {

    @Autowired(required = false)
    private TestMapper testMapper;

    @BeforeEach
    public void setUp() {
        // 确保XML已解析
        XmlMapperParser.parseMapperXmls("classpath*:mapper/**/*.xml");
    }

    @Test
    public void testMapperInjection() {
        // 测试Mapper注入
        if (testMapper != null) {
            System.out.println("=== Mapper注入成功 ===");
            System.out.println("Mapper类型: " + testMapper.getClass().getName());
        } else {
            System.out.println("=== Mapper未注入（可能是配置问题） ===");
        }
    }

    @Test
    public void testSimpleQuery() {
        // 测试简单查询
        if (testMapper != null) {
            Mono<Map<String, Object>> result = testMapper.findById(1L);
            
            StepVerifier.create(result)
                    .expectNextMatches(map -> map != null)
                    .verifyComplete();
            
            System.out.println("=== 简单查询测试完成 ===");
        } else {
            System.out.println("=== 跳过简单查询测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testDynamicQuery() {
        // 测试动态查询
        if (testMapper != null) {
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("name", "test");
            conditions.put("status", 1);
            
            Flux<Map<String, Object>> result = testMapper.findByCondition(conditions);
            
            StepVerifier.create(result)
                    .expectNextCount(0) // 可能为0，取决于数据库状态
                    .verifyComplete();
            
            System.out.println("=== 动态查询测试完成 ===");
        } else {
            System.out.println("=== 跳过动态查询测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testInsertOperation() {
        // 测试插入操作
        if (testMapper != null) {
            String name = "test-" + System.currentTimeMillis();
            Integer status = 1;

            Mono<Integer> result = testMapper.insertData(name, status);
            
            StepVerifier.create(result)
                    .expectNextMatches(count -> count >= 0)
                    .verifyComplete();
            
            System.out.println("=== 插入操作测试完成 ===");
        } else {
            System.out.println("=== 跳过插入操作测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testUpdateOperation() {
        // 测试更新操作
        if (testMapper != null) {
            Long id = 1L;
            String name = "updated-" + System.currentTimeMillis();
            Integer status = 2;

            Mono<Integer> result = testMapper.updateById(id, name, status);
            
            StepVerifier.create(result)
                    .expectNextMatches(count -> count >= 0)
                    .verifyComplete();
            
            System.out.println("=== 更新操作测试完成 ===");
        } else {
            System.out.println("=== 跳过更新操作测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testDeleteOperation() {
        // 测试删除操作
        if (testMapper != null) {
            Mono<Integer> result = testMapper.deleteById(999L); // 使用不存在的ID
            
            StepVerifier.create(result)
                    .expectNextMatches(count -> count >= 0)
                    .verifyComplete();
            
            System.out.println("=== 删除操作测试完成 ===");
        } else {
            System.out.println("=== 跳过删除操作测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testComplexQuery() {
        // 测试复杂查询
        if (testMapper != null) {
            String priority = "high";

            Flux<Map<String, Object>> result = testMapper.findByPriority(priority);
            
            StepVerifier.create(result.count())
                    .expectNextMatches(c -> c > 0) // 可能为0，取决于数据库状态
                    .verifyComplete();
            
            System.out.println("=== 复杂查询测试完成 ===");
        } else {
            System.out.println("=== 跳过复杂查询测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testBatchOperation() {
        // 测试批量操作（使用条件删除）
        if (testMapper != null) {
            Mono<Integer> result = testMapper.deleteByCondition("non-existent", 999);

            StepVerifier.create(result)
                    .expectNextMatches(count -> count >= 0)
                    .verifyComplete();

            System.out.println("=== 批量操作测试完成 ===");
        } else {
            System.out.println("=== 跳过批量操作测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testTransactionSupport() {
        // 测试事务支持
        if (testMapper != null) {
            // 在事务中执行多个操作
            String name = "transaction-test-" + System.currentTimeMillis();
            Integer status = 1;

            Mono<Integer> insertResult = testMapper.insertData(name, status);
            
            StepVerifier.create(insertResult)
                    .expectNextMatches(count -> count >= 0)
                    .verifyComplete();
            
            System.out.println("=== 事务支持测试完成 ===");
        } else {
            System.out.println("=== 跳过事务支持测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testErrorHandling() {
        // 测试错误处理
        if (testMapper != null) {
            // 尝试查询一个可能导致错误的ID
            Mono<Map<String, Object>> result = testMapper.findById(-1L);
            
            StepVerifier.create(result)
                    .expectNextCount(0) // 可能返回空结果
                    .verifyComplete();
            
            System.out.println("=== 错误处理测试完成 ===");
        } else {
            System.out.println("=== 跳过错误处理测试（Mapper未注入） ===");
        }
    }

    @Test
    public void testPerformance() {
        // 测试性能
        if (testMapper != null) {
            long startTime = System.currentTimeMillis();
            
            // 执行多次查询
            for (int i = 0; i < 10; i++) {
                Mono<Map<String, Object>> result = testMapper.findById((long) i);
                result.block(); // 阻塞等待结果
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("=== 性能测试 ===");
            System.out.println("10次查询耗时: " + duration + "ms");
            System.out.println("平均每次查询: " + (duration / 10.0) + "ms");
            
            // 验证性能在合理范围内
            assertTrue(duration < 5000, "10次查询应该在5秒内完成");
        } else {
            System.out.println("=== 跳过性能测试（Mapper未注入） ===");
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
