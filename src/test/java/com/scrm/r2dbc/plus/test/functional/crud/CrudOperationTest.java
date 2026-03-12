package com.scrm.r2dbc.plus.test.functional.crud;

import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.test.service.TestAccountService;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * CRUD 操作功能测试
 *
 * @author dason
 */
@ActiveProfiles("mysql")
@SpringBootTest(classes = TestConfiguration.class)
@Tag("core")
@Tag("fast")
@Tag("functional")
public class CrudOperationTest {

    @Autowired
    private TestAccountService testAccountService;

    @Test
    public void testSave() {
        TestAccount account = new TestAccount();
        account.setUuid("test-uuid-001");
        account.setAccount("test-account");
        account.setNickname("测试账号");
        account.setStatus(1);
        account.setIsOpen(0);
        account.setIsDel(0);
        account.setGmtCreate(LocalDateTime.now());
        account.setGmtModified(LocalDateTime.now());

        Mono<Boolean> result = testAccountService.save(account);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testSaveBatch() {
        TestAccount account1 = new TestAccount();
        account1.setUuid("test-uuid-batch-001");
        account1.setAccount("test-account-batch-1");
        account1.setNickname("批量测试账号1");
        account1.setStatus(1);
        account1.setIsOpen(0);
        account1.setIsDel(0);
        account1.setGmtCreate(LocalDateTime.now());
        account1.setGmtModified(LocalDateTime.now());

        TestAccount account2 = new TestAccount();
        account2.setUuid("test-uuid-batch-002");
        account2.setAccount("test-account-batch-2");
        account2.setNickname("批量测试账号2");
        account2.setStatus(1);
        account2.setIsOpen(0);
        account2.setIsDel(0);
        account2.setGmtCreate(LocalDateTime.now());
        account2.setGmtModified(LocalDateTime.now());

        Mono<Boolean> result = testAccountService.saveBatch(Arrays.asList(account1, account2));

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testList() {
        Flux<TestAccount> result = testAccountService.lambdaQuery().list();
        //Flux<TestAccount> result = testAccountService.list();

        StepVerifier.create(result.count())
                // 数量大于0
                .expectNextMatches(c -> c > 0)
                .verifyComplete();
    }

    @Test
    public void testListWithWrapper() {
        LambdaQueryWrapper<TestAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestAccount::getStatus, 1);

        Flux<TestAccount> result = testAccountService.list(wrapper);

        StepVerifier.create(result.count())
                // 数量大于0
                .expectNextMatches(c -> c > 0)
                .verifyComplete();
    }

    @Test
    public void testCount() {
        Mono<Long> result = testAccountService.count();

        StepVerifier.create(result)
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
    
    @Test
    public void testAutowiring() {
        // 验证 baseMapper 是否被正确注入
        boolean isMapperInjected = testAccountService.isBaseMapperInjected();
        assert isMapperInjected : "baseMapper should be autowired but is null";
        
        // 验证 entityClass 是否被正确设置
        Class<?> entityClass = testAccountService.getEntityClassForTest();
        assert entityClass != null : "entityClass should not be null";
        assert entityClass.equals(TestAccount.class) : "entityClass should be TestAccount.class";
        
        System.out.println("✅ 自动装配验证通过：baseMapper=" + isMapperInjected + ", entityClass=" + entityClass.getSimpleName());
    }

    @Test
    public void testCountWithWrapper() {
        LambdaQueryWrapper<TestAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestAccount::getStatus, 1);

        Mono<Long> result = testAccountService.count(wrapper);

        StepVerifier.create(result)
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }

    @Test
    public void testPage() {
        Page<TestAccount> page = new Page<>(1, 10);
        LambdaQueryWrapper<TestAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestAccount::getStatus, 1);

        Mono<Page<TestAccount>> result = testAccountService.page(page, wrapper);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.getSize() == 10 && p.getCurrent() == 1)
                .verifyComplete();
    }

    @Test
    public void testRemoveById() {
        // 先插入一条记录
        TestAccount account = new TestAccount();
        account.setUuid("test-uuid-remove");
        account.setAccount("test-account-remove");
        account.setNickname("待删除账号");
        account.setStatus(1);
        account.setIsOpen(0);
        account.setIsDel(0);
        account.setGmtCreate(LocalDateTime.now());
        account.setGmtModified(LocalDateTime.now());

        Mono<Boolean> saveResult = testAccountService.save(account);
        
        StepVerifier.create(saveResult)
                .expectNext(true)
                .verifyComplete();

        // 然后删除
        if (account.getId() != null) {
            Mono<Boolean> removeResult = testAccountService.removeById(account.getId());
            
            StepVerifier.create(removeResult)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Test
    public void testUpdateById() {
        // 先插入一条记录
        TestAccount account = new TestAccount();
        account.setUuid("test-uuid-update");
        account.setAccount("test-account-update");
        account.setNickname("待更新账号");
        account.setStatus(1);
        account.setIsOpen(0);
        account.setIsDel(0);
        account.setGmtCreate(LocalDateTime.now());
        account.setGmtModified(LocalDateTime.now());

        Mono<Boolean> saveResult = testAccountService.save(account);
        
        StepVerifier.create(saveResult)
                .expectNext(true)
                .verifyComplete();

        // 然后更新
        if (account.getId() != null) {
            account.setNickname("已更新账号");
            account.setStatus(2);
            
            Mono<Boolean> updateResult = testAccountService.updateById(account);
            
            StepVerifier.create(updateResult)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Test
    public void testGetById() {
        // 先插入一条记录
        TestAccount account = new TestAccount();
        account.setUuid("test-uuid-get");
        account.setAccount("test-account-get");
        account.setNickname("查询账号");
        account.setStatus(1);
        account.setIsOpen(0);
        account.setIsDel(0);
        account.setGmtCreate(LocalDateTime.now());
        account.setGmtModified(LocalDateTime.now());

        Mono<Boolean> saveResult = testAccountService.save(account);
        
        StepVerifier.create(saveResult)
                .expectNext(true)
                .verifyComplete();

        // 然后查询
        if (account.getId() != null) {
            Mono<TestAccount> getResult = testAccountService.getById(account.getId());
            
            StepVerifier.create(getResult)
                    .expectNextMatches(acc -> acc.getAccount().equals("test-account-get"))
                    .verifyComplete();
        }
    }
}
