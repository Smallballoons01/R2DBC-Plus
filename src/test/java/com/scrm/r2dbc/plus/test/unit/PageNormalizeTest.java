package com.scrm.r2dbc.plus.test.unit;

import com.scrm.r2dbc.plus.page.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分页参数防御性规范化测试
 *
 * <p>覆盖 {@link Page#normalize()} 的边界行为：非法输入不应导致非法 SQL。
 */
@Tag("unit")
@Tag("fast")
@DisplayName("Page 分页参数规范化")
class PageNormalizeTest {

    @Test
    @DisplayName("current 小于 1 时纠正为 1")
    void shouldCorrectCurrentToOne() {
        Page<String> page = new Page<>(0, 10);
        assertEquals(1L, page.getCurrent());

        Page<String> page2 = new Page<>(-5, 10);
        assertEquals(1L, page2.getCurrent());
    }

    @Test
    @DisplayName("size 小于等于 0 时回退为默认值 10")
    void shouldFallbackToDefaultSize() {
        assertEquals(10L, new Page<>(1, 0).getSize());
        assertEquals(10L, new Page<>(1, -1).getSize());
        assertEquals(10L, new Page<>(1, -9999).getSize());
    }

    @Test
    @DisplayName("size 超过上限时被截断，默认上限 500")
    void shouldTruncateOversizedPage() {
        Page<String> page = new Page<>(1, 100_000);
        assertEquals(500L, page.getSize());
    }

    @Test
    @DisplayName("setMaxPageSize 可自定义上限")
    void shouldRespectCustomMaxPageSize() {
        Page<String> page = new Page<>(1, 300);
        page.setMaxPageSize(50);
        assertEquals(50L, page.getSize());
    }

    @Test
    @DisplayName("maxPageSize 设为 <= 0 表示不限制")
    void shouldAllowUnlimitedPageSize() {
        // 构造时 size=5000 会被默认上限 500 截断，
        // 因此先放开上限再设置超大 size
        Page<String> page = new Page<>(1, 10);
        page.setMaxPageSize(0);
        page.setSize(5000);
        assertEquals(5000L, page.getSize());
    }

    @Test
    @DisplayName("setSize 同样走规范化，不会写入非法值")
    void shouldNormalizeOnSetSize() {
        Page<String> page = new Page<>(1, 10);
        page.setSize(0);
        assertEquals(10L, page.getSize());

        page.setSize(999_999);
        assertEquals(500L, page.getSize());
    }

    @Test
    @DisplayName("total 为负数时纠正为 0，不出现负页数")
    void shouldCorrectNegativeTotal() {
        Page<String> page = new Page<>(1, 10, -100);
        assertEquals(0L, page.getTotal());
        assertEquals(0L, page.getPages());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    @DisplayName("偏移量与总页数计算正确")
    void shouldComputeOffsetAndPages() {
        // total=25, size=10 → 3 页；current=3 为最后一页
        Page<String> page = new Page<>(3, 10, 25);
        assertEquals(20L, page.getOffset());
        assertEquals(3L, page.getPages());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext(), "第 3 页已是末页（共 3 页）");

        // current=2 时仍有下一页
        Page<String> middle = new Page<>(2, 10, 25);
        assertTrue(middle.hasNext());
    }

    @Test
    @DisplayName("toString 不因 records 为 null 而抛异常")
    void shouldNotThrowOnNullRecords() {
        Page<String> page = new Page<>(1, 10, 0, List.of("a", "b"));
        assertTrue(page.toString().contains("records=2"));

        Page<String> empty = new Page<>(1, 10);
        assertTrue(empty.toString().contains("records=0"));
    }
}