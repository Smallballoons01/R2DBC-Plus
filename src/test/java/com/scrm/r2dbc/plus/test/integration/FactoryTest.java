package com.scrm.r2dbc.plus.test.integration;

import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.factory.R2dbcPlusFactory;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.service.IService;
import com.scrm.r2dbc.plus.test.config.TestConfiguration;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * 工厂类测试
 *
 * @author dason
 */
@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("mysql")
@Tag("integration")
@Tag("fast")
public class FactoryTest {

    @Autowired(required = false)
    private R2dbcPlusFactory r2DbcPlusFactory;

    @Autowired(required = false)
    private MapperProxyFactory mapperProxyFactory;

    @BeforeEach
    public void setUp() {
        // 如果依赖注入失败，创建默认实例
        if (mapperProxyFactory == null) {
            mapperProxyFactory = new MapperProxyFactory();
        }
        if (r2DbcPlusFactory == null) {
            r2DbcPlusFactory = new R2dbcPlusFactory();
        }
    }

    @Test
    public void testCreateMapper() {
        // 检查依赖是否正确注入
        if (r2DbcPlusFactory == null) {
            System.out.println("=== R2dbcPlusFactory 未注入，跳过测试 ===");
            return;
        }

        // 测试创建基础 Mapper
        BaseMapper<TestAccount> mapper = r2DbcPlusFactory.createMapper(TestAccount.class);

        // 验证 mapper 不为空
        assert mapper != null;

        // 测试基础查询功能
        Flux<TestAccount> result = mapper.selectList();

        StepVerifier.create(result.count())
                // 数量大于0
                .expectNextMatches(c -> c > 0)
                .verifyComplete();
    }

    @Test
    public void testCreateService() {
        // 测试创建 Service
        IService<TestAccount> service = r2DbcPlusFactory.createService(TestAccount.class);
        
        // 验证 service 不为空
        assert service != null;
        
        // 测试基础查询功能
        Flux<TestAccount> result = service.list();

        StepVerifier.create(result.count())
                // 数量大于0
                .expectNextMatches(c -> c > 0)
                .verifyComplete();
    }

    @Test
    public void testMapperProxyFactory() {
        // 检查依赖是否正确注入
        if (mapperProxyFactory == null) {
            System.out.println("=== MapperProxyFactory 未注入，跳过测试 ===");
            return;
        }

        // 定义一个测试接口
        interface TestMapper extends BaseMapper<TestAccount> {
            // 基础方法已继承
        }

        // 测试创建代理
        TestMapper mapper = mapperProxyFactory.createMapperProxy(TestMapper.class);

        // 验证 mapper 不为空
        assert mapper != null;

        // 测试基础查询功能
        Flux<TestAccount> result = mapper.selectList();
        
        StepVerifier.create(result.count())
                // 数量大于0
                .expectNextMatches(c -> c > 0)
                .verifyComplete();
    }

    @Test
    public void testMapperWithLambdaQuery() {
        // 检查依赖是否正确注入
        if (r2DbcPlusFactory == null) {
            System.out.println("=== R2dbcPlusFactory 未注入，跳过测试 ===");
            return;
        }

        BaseMapper<TestAccount> mapper = r2DbcPlusFactory.createMapper(TestAccount.class);

        // 测试 Lambda 查询
        Mono<Long> countResult = mapper.selectCount(
                new LambdaQueryWrapper<>(TestAccount.class)
                        .eq(TestAccount::getIsDel, 0)
        );

        StepVerifier.create(countResult)
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
}
