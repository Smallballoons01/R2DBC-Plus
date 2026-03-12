package com.scrm.r2dbc.plus.test.annotation;

import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.test.mapper.AnnotationTestMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

/**
 * 注解功能测试类
 * 测试新添加的 @Select、@Insert、@Update、@Delete 注解功能
 * 
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("mysql")
@Tag("core")
@Tag("fast")
@Tag("annotation")
public class AnnotationTest {

    @Autowired
    private AnnotationTestMapper annotationTestMapper;

    @Test
    public void testSelectAnnotation() {
        // 测试 @Select 注解查询功能
        StepVerifier.create(annotationTestMapper.countAccountsByUuid("test-uuid"))
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }

    @Test
    public void testBooleanConversion() {
        // 测试 Boolean 类型转换
        String nonExistentAccount = "non-existent-account-" + System.currentTimeMillis();
        
        StepVerifier.create(annotationTestMapper.existsByAccount(nonExistentAccount))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void testInsertAnnotation() {
        // 测试 @Insert 注解插入功能
        String testUuid = "test-annotation-" + System.currentTimeMillis();
        String testAccount = "test-account-" + System.currentTimeMillis();
        
        StepVerifier.create(
                annotationTestMapper.insertAccount(
                    testUuid, 
                    testAccount, 
                    "password123", 
                    1, 
                    1, 
                    100L
                )
        )
        .expectNext(1)
        .verifyComplete();
        
        // 验证插入成功
        StepVerifier.create(annotationTestMapper.countAccountsByUuid(testUuid))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void testUpdateAnnotation() {
        // 先插入一条测试数据
        String testUuid = "test-update-" + System.currentTimeMillis();
        String testAccount = "test-update-account-" + System.currentTimeMillis();
        
        StepVerifier.create(
                annotationTestMapper.insertAccount(testUuid, testAccount, "password123", 1, 1, 100L)
                .then(annotationTestMapper.updateAccountStatusByUuid(testUuid, 2))
        )
        .expectNext(1)
        .verifyComplete();
        
        // 验证更新成功
        StepVerifier.create(annotationTestMapper.selectAccountsByUuidAndStatus(testUuid, 2))
                .expectNextMatches(account -> account.getStatus().equals(2))
                .verifyComplete();
    }

    @Test
    public void testDeleteAnnotation() {
        // 先插入一条测试数据
        String testUuid = "test-delete-" + System.currentTimeMillis();
        String testAccount = "test-delete-account-" + System.currentTimeMillis();
        
        StepVerifier.create(
                annotationTestMapper.insertAccount(testUuid, testAccount, "password123", 1, 1, 100L)
                .then(annotationTestMapper.deleteAccountsByUuidAndStatus(testUuid, 1))
        )
        .expectNext(1)
        .verifyComplete();
        
        // 验证删除成功
        StepVerifier.create(annotationTestMapper.countAccountsByUuid(testUuid))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    public void testComplexSelectAnnotation() {
        // 测试复杂查询注解
        String testUuid = "test-complex-" + System.currentTimeMillis();
        
        // 插入多条测试数据
        StepVerifier.create(
                annotationTestMapper.insertAccount(testUuid, "account1", "password123", 1, 1, 100L)
                .then(annotationTestMapper.insertAccount(testUuid, "account2", "password123", 1, 1, 200L))
                .then(annotationTestMapper.selectAccountsWithRelatedCount(testUuid).collectList())
        )
        .expectNextMatches(accounts -> accounts.size() >= 2)
        .verifyComplete();
    }

    @Test
    public void testExistsAnnotation() {
        // 测试存在性查询
        String testAccount = "test-exists-" + System.currentTimeMillis();
        String testUuid = "test-exists-uuid-" + System.currentTimeMillis();
        
        // 先验证不存在
        StepVerifier.create(annotationTestMapper.existsByAccount(testAccount))
                .expectNext(false)
                .verifyComplete();
        
        // 插入数据后验证存在
        StepVerifier.create(
                annotationTestMapper.insertAccount(testUuid, testAccount, "password123", 1, 1, 100L)
                .then(annotationTestMapper.existsByAccount(testAccount))
        )
        .expectNext(true)
        .verifyComplete();
    }
}
