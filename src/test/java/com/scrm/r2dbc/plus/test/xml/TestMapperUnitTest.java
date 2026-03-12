package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.test.mapper.TestMapper;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestMapper 单元测试
 * 使用简单的测试Mapper验证XML映射功能
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("xml")
@Tag("unit")
public class TestMapperUnitTest {

    @Mock
    private XmlSqlExecutor xmlSqlExecutor;
    
    private MapperProxyFactory mapperProxyFactory;
    private TestMapper testMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 解析测试XML文件
        XmlMapperParser.clearCache();
        ClassPathResource resource = new ClassPathResource("mapper/TestMapper.xml");
        XmlMapperParser.parseMapperXml(resource);
        
        // 创建MapperProxyFactory并注入mock的XmlSqlExecutor
        mapperProxyFactory = new MapperProxyFactory();
        injectMockExecutor();
        
        // 创建Mapper代理
        testMapper = mapperProxyFactory.createMapperProxy(TestMapper.class);
    }

    private void injectMockExecutor() {
        try {
            java.lang.reflect.Field field = MapperProxyFactory.class.getDeclaredField("xmlSqlExecutor");
            field.setAccessible(true);
            field.set(mapperProxyFactory, xmlSqlExecutor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSimpleSelect() {
        // 测试简单查询
        when(xmlSqlExecutor.selectOne(anyString(), any(), any()))
                .thenReturn(Mono.just(new HashMap<>()));
        
        Mono<Map<String, Object>> result = testMapper.findById(1L);
        assertNotNull(result, "查询结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNextMatches(map -> map != null)
                .verifyComplete();

        verify(xmlSqlExecutor).selectOne(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.findById"),
            any(Map.class),
            eq(Map.class)
        );
        
        System.out.println("=== 简单查询测试完成 ===");
    }

    @Test
    public void testDynamicSelect() {
        // 测试动态查询
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenReturn(Flux.empty());
        
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("name", "test");
        conditions.put("status", 1);
        
        Flux<Map<String, Object>> result = testMapper.findByCondition(conditions);
        assertNotNull(result, "查询结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(xmlSqlExecutor).selectList(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.findByCondition"),
            eq(conditions),
            eq(Map.class)
        );
        
        System.out.println("=== 动态查询测试完成 ===");
    }

    @Test
    public void testInsert() {
        // 测试插入
        when(xmlSqlExecutor.insert(anyString(), any()))
                .thenReturn(Mono.just(1));

        String name = "test";
        Integer status = 1;

        Mono<Integer> result = testMapper.insertData(name, status);
        assertNotNull(result, "插入结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();

        verify(xmlSqlExecutor).insert(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.insertData"),
            any(Map.class)
        );

        System.out.println("=== 插入测试完成 ===");
    }

    @Test
    public void testUpdate() {
        // 测试更新
        when(xmlSqlExecutor.update(anyString(), any()))
                .thenReturn(Mono.just(1));

        Long id = 1L;
        String name = "updated";
        Integer status = 2;

        Mono<Integer> result = testMapper.updateById(id, name, status);
        assertNotNull(result, "更新结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();

        verify(xmlSqlExecutor).update(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.updateById"),
            any(Map.class)
        );

        System.out.println("=== 更新测试完成 ===");
    }

    @Test
    public void testDelete() {
        // 测试删除
        when(xmlSqlExecutor.delete(anyString(), any()))
                .thenReturn(Mono.just(1));
        
        Mono<Integer> result = testMapper.deleteById(1L);
        assertNotNull(result, "删除结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();

        verify(xmlSqlExecutor).delete(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.deleteById"),
            any(Map.class)
        );
        
        System.out.println("=== 删除测试完成 ===");
    }

    @Test
    public void testComplexQuery() {
        // 测试复杂查询
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenReturn(Flux.empty());

        String priority = "high";

        Flux<Map<String, Object>> result = testMapper.findByPriority(priority);
        assertNotNull(result, "查询结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(xmlSqlExecutor).selectList(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.findByPriority"),
            any(Map.class),
            eq(Map.class)
        );

        System.out.println("=== 复杂查询测试完成 ===");
    }

    @Test
    public void testBatchOperation() {
        // 测试批量操作
        when(xmlSqlExecutor.update(anyString(), any()))
                .thenReturn(Mono.just(3));

        java.util.List<Long> ids = java.util.Arrays.asList(1L, 2L, 3L);
        Integer status = 2;

        Mono<Integer> result = testMapper.batchUpdate(ids, status);
        assertNotNull(result, "批量操作结果不应该为空");

        // 订阅以触发执行
        StepVerifier.create(result)
                .expectNext(3)
                .verifyComplete();

        verify(xmlSqlExecutor).update(
            eq("com.scrm.r2dbc.plus.test.mapper.TestMapper.batchUpdate"),
            any(Map.class)
        );

        System.out.println("=== 批量操作测试完成 ===");
    }

    @Test
    public void testParameterMapping() {
        // 测试参数映射
        when(xmlSqlExecutor.selectOne(anyString(), any(), any()))
                .thenReturn(Mono.just(new HashMap<>()));
        
        testMapper.findById(123L);
        
        // 验证参数被正确映射
        verify(xmlSqlExecutor).selectOne(anyString(), argThat(params -> {
            Map<String, Object> paramMap = (Map<String, Object>) params;
            return Long.valueOf(123L).equals(paramMap.get("param1"));
        }), any());
        
        System.out.println("=== 参数映射测试完成 ===");
    }
}
