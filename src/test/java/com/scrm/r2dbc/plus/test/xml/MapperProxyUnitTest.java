package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.test.mapper.TestAccountMapper;
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mapper代理单元测试
 * 测试Mapper接口的代理机制和方法映射
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("xml")
@Tag("unit")
public class MapperProxyUnitTest {

    @Mock
    private XmlSqlExecutor xmlSqlExecutor;
    
    private MapperProxyFactory mapperProxyFactory;
    private TestAccountMapper TestAccountMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 解析测试XML文件
        XmlMapperParser.clearCache();
        ClassPathResource resource = new ClassPathResource("mapper/TestAccountMapper.xml");
        XmlMapperParser.parseMapperXml(resource);
        
        // 创建MapperProxyFactory并注入mock的XmlSqlExecutor
        mapperProxyFactory = new MapperProxyFactory();
        // 通过反射注入mock对象
        try {
            java.lang.reflect.Field field = MapperProxyFactory.class.getDeclaredField("xmlSqlExecutor");
            field.setAccessible(true);
            field.set(mapperProxyFactory, xmlSqlExecutor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // 创建Mapper代理
        TestAccountMapper = mapperProxyFactory.createMapperProxy(TestAccountMapper.class);
    }

    @Test
    public void testMapperProxyCreation() {
        // 测试代理对象创建
        assertNotNull(TestAccountMapper, "Mapper代理应该被创建");
        assertTrue(TestAccountMapper.getClass().getName().contains("Proxy"), 
                  "应该是代理对象");
        
        System.out.println("=== 代理创建测试 ===");
        System.out.println("代理类型: " + TestAccountMapper.getClass().getName());
        System.out.println("接口: " + TestAccountMapper.class.getName());
    }

    @Test
    public void testMethodToXmlMapping() {
        // 测试方法到XML语句的映射
        
        // Mock返回值
        when(xmlSqlExecutor.selectList(anyString(), any(), eq(TestAccount.class)))
                .thenReturn(Flux.empty());
        
        // 调用Mapper方法
        TestAccountMapper.findRecentActiveAccounts("test-uuid-001", 24, 10);
        
        // 验证调用了正确的XML语句
        String expectedStatementId = "com.scrm.r2dbc.plus.test.mapper.TestAccountMapper.findRecentActiveAccounts";
        verify(xmlSqlExecutor).selectList(eq(expectedStatementId), any(Map.class), eq(TestAccount.class));
        
        System.out.println("=== 方法映射测试完成 ===");
    }

    @Test
    public void testParameterMapping() {
        // 测试参数映射
        
        when(xmlSqlExecutor.selectList(anyString(), any(), eq(TestAccount.class)))
                .thenReturn(Flux.empty());
        
        // 调用方法
        String uuid = "test-uuid-001";
        int hours = 24;
        int limit = 10;
        TestAccountMapper.findRecentActiveAccounts(uuid, hours, limit);
        
        // 验证参数映射 - 根据MapperProxyFactory的实际实现，参数是按位置命名的
        verify(xmlSqlExecutor).selectList(anyString(), argThat(params -> {
            Map<String, Object> paramMap = (Map<String, Object>) params;
            return uuid.equals(paramMap.get("param1")) &&
                   hours == (Integer) paramMap.get("param2") &&
                   limit == (Integer) paramMap.get("param3");
        }), eq(TestAccount.class));
        
        System.out.println("=== 参数映射测试完成 ===");
    }

    @Test
    public void testMapParameterMapping() {
        // 测试Map参数映射
        
        when(xmlSqlExecutor.selectList(anyString(), any(), eq(TestAccount.class)))
                .thenReturn(Flux.empty());
        
        // 准备Map参数
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("uuid", "test-uuid-001");
        conditions.put("status", 1);
        conditions.put("account", "test");
        
        // 调用方法
        TestAccountMapper.findAccountsByComplexCondition(conditions);
        
        // 验证Map参数直接传递
        verify(xmlSqlExecutor).selectList(anyString(), eq(conditions), eq(TestAccount.class));
        
        System.out.println("=== Map参数映射测试完成 ===");
    }

    @Test
    public void testReturnTypeMapping() {
        // 测试返回类型映射
        
        // 测试Flux返回类型
        when(xmlSqlExecutor.selectList(anyString(), any(), eq(TestAccount.class)))
                .thenReturn(Flux.just(new TestAccount()));
        
        Flux<TestAccount> fluxResult = TestAccountMapper.findRecentActiveAccounts("test", 24, 10);
        assertNotNull(fluxResult, "应该返回Flux类型");
        
        // 测试Mono返回类型
        when(xmlSqlExecutor.selectOne(anyString(), any(), any()))
                .thenReturn(Mono.just(new HashMap<>()));
        
        Mono<Map<String, Object>> monoResult = TestAccountMapper.getAccountStatistics("test");
        assertNotNull(monoResult, "应该返回Mono类型");
        
        System.out.println("=== 返回类型映射测试完成 ===");
    }

    @Test
    public void testMethodNameResolution() {
        // 测试方法名解析
        
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenReturn(Flux.empty());
        when(xmlSqlExecutor.selectOne(anyString(), any(), any()))
                .thenReturn(Mono.empty());
        
        // 测试不同的方法名
        TestAccountMapper.findRecentActiveAccounts("test", 24, 10);
        TestAccountMapper.getAccountStatistics("test");
        TestAccountMapper.countAccountsByStatus("test");
        
        // 验证生成了正确的语句ID
        String namespace = "com.scrm.r2dbc.plus.test.mapper.TestAccountMapper";
        verify(xmlSqlExecutor).selectList(eq(namespace + ".findRecentActiveAccounts"), any(), any());
        verify(xmlSqlExecutor).selectOne(eq(namespace + ".getAccountStatistics"), any(), any());
        verify(xmlSqlExecutor).selectList(eq(namespace + ".countAccountsByStatus"), any(), any());
        
        System.out.println("=== 方法名解析测试完成 ===");
    }

    @Test
    public void testInterfaceMethodInvocation() {
        // 测试接口方法调用
        
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenReturn(Flux.empty());
        
        // 获取接口方法
        Method[] methods = TestAccountMapper.class.getDeclaredMethods();
        assertTrue(methods.length > 0, "接口应该有方法");
        
        // 验证每个方法都能被正确调用
        for (Method method : methods) {
            if (method.getName().equals("findRecentActiveAccounts")) {
                try {
                    Object result = method.invoke(TestAccountMapper, "test", 24, 10);
                    assertNotNull(result, "方法调用应该返回结果");
                } catch (Exception e) {
                    fail("方法调用不应该抛出异常: " + e.getMessage());
                }
            }
        }
        
        System.out.println("=== 接口方法调用测试完成 ===");
    }

    @Test
    public void testExceptionHandling() {
        // 测试异常处理
        
        // Mock抛出异常
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenThrow(new RuntimeException("SQL执行异常"));
        
        // 调用方法应该传播异常
        assertThrows(RuntimeException.class, () -> {
            TestAccountMapper.findRecentActiveAccounts("test", 24, 10);
        });
        
        System.out.println("=== 异常处理测试完成 ===");
    }

    @Test
    public void testMultipleMapperInstances() {
        // 测试多个Mapper实例
        
        TestAccountMapper mapper1 = mapperProxyFactory.createMapperProxy(TestAccountMapper.class);
        TestAccountMapper mapper2 = mapperProxyFactory.createMapperProxy(TestAccountMapper.class);
        
        assertNotNull(mapper1);
        assertNotNull(mapper2);
        assertSame(mapper1, mapper2, "缓存机制应该返回相同的实例以提高性能");
        
        // 它们应该有相同的行为，因为是同一个实例
        when(xmlSqlExecutor.selectList(anyString(), any(), any()))
                .thenReturn(Flux.empty());

        mapper1.findRecentActiveAccounts("test", 24, 10);
        mapper2.findRecentActiveAccounts("test", 24, 10);

        // 虽然是同一个实例，但方法调用了两次
        verify(xmlSqlExecutor, times(2)).selectList(anyString(), any(), any());

        System.out.println("=== 多实例测试完成 ===");
    }

    @Test
    public void testStatementNotFound() {
        // 测试语句不存在的情况

        // 清空缓存，模拟语句不存在
        XmlMapperParser.clearCache();

        // 配置mock对象在语句不存在时抛出异常
        String expectedStatementId = "com.scrm.r2dbc.plus.test.mapper.TestAccountMapper.findRecentActiveAccounts";
        when(xmlSqlExecutor.selectList(eq(expectedStatementId), any(), any()))
                .thenThrow(new RuntimeException("SQL statement not found: " + expectedStatementId));

        // 调用方法应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            TestAccountMapper.findRecentActiveAccounts("test", 24, 10);
        });

        // 验证异常消息包含预期的内容
        System.out.println("异常信息：" + exception.getMessage());
        assertTrue(exception.getMessage().contains("SQL statement not found"),
                   "异常消息应该包含'SQL statement not found'");

        System.out.println("=== 语句不存在测试完成 ===");
    }
}
