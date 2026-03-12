package com.scrm.r2dbc.plus.test.functional.wrapper;

import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import com.scrm.r2dbc.plus.conditions.Wrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 查询包装器兼容性测试
 * 原 WrapperCompatibilityTest.java
 */
@Tag("core")
@Tag("fast")
@Tag("unit")
public class WrapperTest {

    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private Integer age;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    // 测试 Service 实现
    public static class TestService extends ServiceImpl<BaseMapper<TestEntity>, TestEntity> {
        public TestService(BaseMapper<TestEntity> baseMapper) {
            super(baseMapper, TestEntity.class);
        }
    }

    @Test
    public void testQueryWrapperCompatibility() {
        // 创建 mock BaseMapper
        BaseMapper<TestEntity> mockMapper = Mockito.mock(BaseMapper.class);
        
        // 设置 mock 行为
        when(mockMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(Flux.empty());
        when(mockMapper.selectCount(any(QueryWrapper.class)))
            .thenReturn(Mono.just(0L));
        when(mockMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
            .thenReturn(Mono.just(new Page<TestEntity>(1, 10)));
        when(mockMapper.delete(any(QueryWrapper.class)))
            .thenReturn(Mono.just(1));

        // 创建测试 Service
        TestService testService = new TestService(mockMapper);

        // 测试 QueryWrapper
        QueryWrapper<TestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "test");

        // 测试各种方法
        StepVerifier.create(testService.list(queryWrapper))
                .verifyComplete();

        StepVerifier.create(testService.count(queryWrapper))
                .expectNext(0L)
                .verifyComplete();

        Page<TestEntity> page = new Page<>(1, 10);
        StepVerifier.create(testService.page(page, queryWrapper))
                .expectNextMatches(p -> p.getSize() == 10)
                .verifyComplete();

        StepVerifier.create(testService.remove(queryWrapper))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testLambdaQueryWrapperCompatibility() {
        // 创建 mock BaseMapper
        BaseMapper<TestEntity> mockMapper = Mockito.mock(BaseMapper.class);
        
        // 设置 mock 行为
        when(mockMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Flux.empty());
        when(mockMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(Mono.just(0L));
        when(mockMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(Mono.just(new Page<TestEntity>(1, 10)));
        when(mockMapper.delete(any(LambdaQueryWrapper.class)))
            .thenReturn(Mono.just(1));

        // 创建测试 Service
        TestService testService = new TestService(mockMapper);

        // 测试 LambdaQueryWrapper
        LambdaQueryWrapper<TestEntity> lambdaWrapper = new LambdaQueryWrapper<>();
        lambdaWrapper.eq(TestEntity::getName, "test");

        // 测试各种方法
        StepVerifier.create(testService.list(lambdaWrapper))
                .verifyComplete();

        StepVerifier.create(testService.count(lambdaWrapper))
                .expectNext(0L)
                .verifyComplete();

        Page<TestEntity> page = new Page<>(1, 10);
        StepVerifier.create(testService.page(page, lambdaWrapper))
                .expectNextMatches(p -> p.getSize() == 10)
                .verifyComplete();

        StepVerifier.create(testService.remove(lambdaWrapper))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testWrapperPolymorphism() {
        // 创建 mock BaseMapper
        BaseMapper<TestEntity> mockMapper = Mockito.mock(BaseMapper.class);
        
        // 设置 mock 行为 - 使用 Wrapper 基类
        when(mockMapper.selectList(any(Wrapper.class)))
            .thenReturn(Flux.empty());
        when(mockMapper.selectCount(any(Wrapper.class)))
            .thenReturn(Mono.just(0L));
        when(mockMapper.delete(any(Wrapper.class)))
            .thenReturn(Mono.just(1));

        // 创建测试 Service
        TestService testService = new TestService(mockMapper);

        // 测试多态性 - QueryWrapper 作为 Wrapper
        Wrapper<TestEntity> wrapper1 = new QueryWrapper<TestEntity>().eq("name", "test");
        
        StepVerifier.create(testService.list(wrapper1))
                .verifyComplete();

        StepVerifier.create(testService.count(wrapper1))
                .expectNext(0L)
                .verifyComplete();

        // 测试多态性 - LambdaQueryWrapper 作为 Wrapper
        Wrapper<TestEntity> wrapper2 = new LambdaQueryWrapper<TestEntity>().eq(TestEntity::getName, "test");
        
        StepVerifier.create(testService.list(wrapper2))
                .verifyComplete();

        StepVerifier.create(testService.count(wrapper2))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    public void testWrapperMethodChaining() {
        // 测试 QueryWrapper 方法链
        QueryWrapper<TestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "test")
                   .gt("age", 18)
                   .orderByDesc("id")
                   .last("LIMIT 10");

        String sql = queryWrapper.getSqlSegment();
        System.out.println("QueryWrapper SQL: " + sql);

        // 测试 LambdaQueryWrapper 方法链
        LambdaQueryWrapper<TestEntity> lambdaWrapper = new LambdaQueryWrapper<>();
        lambdaWrapper.eq(TestEntity::getName, "test")
                    .gt(TestEntity::getAge, 18)
                    .orderByDesc(TestEntity::getId)
                    .last("LIMIT 10");

        String lambdaSql = lambdaWrapper.getSqlSegment();
        System.out.println("LambdaQueryWrapper SQL: " + lambdaSql);
    }
}
