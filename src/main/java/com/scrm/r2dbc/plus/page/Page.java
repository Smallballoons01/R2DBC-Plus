package com.scrm.r2dbc.plus.page;

import java.util.List;

/**
 * 分页结果封装类
 *
 * <p><b>注意</b>：本类对分页参数做了防御性规范化，任何入口（BaseMapper、
 * IService、XML 自定义分页）拿到的都是已修正的合法值，避免
 * {@code size <= 0} 或超大size 拼出非法 SQL 拖垮数据库。
 *
 * @param <T> 数据类型
 * @author dason
 */
public class Page<T> {

    /**
     * 单页最大条数上限，默认 500。可通过 {@code r2dbc-plus.max-page-size} 调整。
     */
    private static final long DEFAULT_MAX_PAGE_SIZE = 500L;

    /**
     * 默认单页条数
     */
    private static final long DEFAULT_SIZE = 10L;

    private long current = 1;
    private long size = DEFAULT_SIZE;
    private long total = 0;
    private List<T> records;

    /**
     * 单页条数上限。设为 {@code <= 0} 表示不限制。
     */
    private long maxPageSize = DEFAULT_MAX_PAGE_SIZE;

    public Page() {
    }

    public Page(long current, long size) {
        this.current = current;
        this.size = size;
        normalize();
    }

    public Page(long current, long size, long total) {
        this.current = current;
        this.size = size;
        this.total = total;
        normalize();
    }

    public Page(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        normalize();
    }

    /**
     * 规范化分页参数
     *
     * <p>规则：
     * <ul>
     *   <li>{@code current < 1} → 纠正为 1</li>
     *   <li>{@code size <= 0} → 回退为默认 10（而不是抛异常，避免调用方频繁踩坑）</li>
     *   <li>{@code size > maxPageSize} → 截断为 maxPageSize</li>
     *   <li>{@code total < 0} → 纠正为 0</li>
     * </ul>
     */
    public Page<T> normalize() {
        if (current < 1) {
            current = 1;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (maxPageSize > 0 && size > maxPageSize) {
            size = maxPageSize;
        }
        if (total < 0) {
            total = 0;
        }
        return this;
    }

    /**
     * 获取总页数
     */
    public long getPages() {
        if (size <= 0) {
            return 0L;
        }
        long pages = total / size;
        if (total % size != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return current < getPages();
    }

    /**
     * 获取偏移量
     */
    public long getOffset() {
        return (current - 1) * size;
    }

    // Getters and Setters

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
        normalize();
    }

    public long getSize() {
        return size;
    }

    /**
     * 设置单页条数。传入非法值（<= 0）会被规范化而非直接生效。
     */
    public void setSize(long size) {
        this.size = size;
        normalize();
    }

    public long getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * 设置单页条数上限，传 {@code <= 0} 表示不限制。
     */
    public void setMaxPageSize(long maxPageSize) {
        this.maxPageSize = maxPageSize;
        normalize();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", size=" + size +
                ", total=" + total +
                ", pages=" + getPages() +
                ", records=" + (records != null ? records.size() : 0) +
                '}';
    }
}