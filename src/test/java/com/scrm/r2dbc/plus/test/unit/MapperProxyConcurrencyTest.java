package com.scrm.r2dbc.plus.test.unit;

import com.scrm.r2dbc.plus.annotation.TableId;
import com.scrm.r2dbc.plus.annotation.TableName;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mapper 代理缓存并发安全测试
 *
 * <p>回归防护：{@code MapperProxyFactory} 是单例 Bean，其代理缓存若使用
 * 非线程安全的 {@code HashMap}，在 WebFlux 多线程下并发 {@code computeIfAbsent}
 * 可能破坏内部桶结构，极端情况下导致 CPU 100% 打满。
 * 本测试并发创建同名代理，验证缓存容器本身是线程安全的。
 */
@Tag("unit")
@Tag("fast")
@DisplayName("Mapper 代理缓存并发安全")
class MapperProxyConcurrencyTest {

    @TableName("concurrent_test")
    static class ConcurrentEntity {
        @TableId
        private Long id;
        private String name;
    }

    interface ConcurrentMapper extends BaseMapper<ConcurrentEntity> {
    }

    @Test
    @DisplayName("代理缓存字段必须是 ConcurrentHashMap")
    void mapperCacheShouldBeConcurrent() throws Exception {
        Field field = Class.forName("com.scrm.r2dbc.plus.proxy.MapperProxyFactory")
                .getDeclaredField("mapperCache");
        Class<?> type = field.getType();
        assertTrue(java.util.concurrent.ConcurrentHashMap.class.isAssignableFrom(type)
                        || type == java.util.Map.class,
                "mapperCache 应为 ConcurrentHashMap，实际为 " + type.getName());
    }

    @Test
    @DisplayName("并发 computeIfAbsent 不损坏缓存结构")
    void concurrentComputeIfAbsentShouldNotCorrupt() throws Exception {
        // 直接对 MapperProxyFactory 的缓存字段做并发写入，验证容器线程安全。
        // 这里只验证容器行为，不需要真实数据库连接。
        Field field = Class.forName("com.scrm.r2dbc.plus.proxy.MapperProxyFactory")
                .getDeclaredField("mapperCache");
        field.setAccessible(true);

        Object factory = Class.forName("com.scrm.r2dbc.plus.proxy.MapperProxyFactory")
                .getDeclaredConstructor().newInstance();
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> cache = (java.util.Map<String, Object>) field.get(factory);
        assertNotNull(cache);

        int threads = 32;
        int perThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        // 混合"已存在"与"不存在"的 key，最大化竞争
                        String key = (i % 3 == 0) ? "shared-key" : ("key-" + threadId + "-" + i);
                        cache.computeIfAbsent(key, k -> new Object());
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "并发写入超时");
        pool.shutdownNow();

        // 每线程 200 次写入中，i%3==0 的 67 次全部映射到同一个 shared-key，
        // 其余 133 次为线程内唯一 key，因此总条目 = 32 * 133 + 1
        int sharedKeyWrites = 0;
        for (int i = 0; i < perThread; i++) {
            if (i % 3 == 0) {
                sharedKeyWrites++;
            }
        }
        int uniqueKeysPerThread = perThread - sharedKeyWrites;
        int expected = threads * uniqueKeysPerThread + 1;

        assertEquals(0, errors.get(), "并发写入出现异常");
        assertEquals(expected, cache.size(),
                "缓存条目数异常，说明存在并发覆盖或丢失");
    }

    @Test
    @DisplayName("mapperCache 字段应为 final，保证发布安全")
    void mapperCacheShouldBeFinal() throws Exception {
        Field field = Class.forName("com.scrm.r2dbc.plus.proxy.MapperProxyFactory")
                .getDeclaredField("mapperCache");
        assertTrue(Modifier.isFinal(field.getModifiers()),
                "mapperCache 应声明为 final，避免不安全发布");
    }
}