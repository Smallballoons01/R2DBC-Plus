package com.scrm.r2dbc.plus.page;

import java.util.List;

/**
 * 分页结果封装类
 * 
 * @param <T> 数据类型
 * @author dason
 */
public class Page<T> {
    
    private long current = 1;
    private long size = 10;
    private long total = 0;
    private List<T> records;
    
    public Page() {}
    
    public Page(long current, long size) {
        this.current = current;
        this.size = size;
    }
    
    public Page(long current, long size, long total) {
        this.current = current;
        this.size = size;
        this.total = total;
    }
    
    public Page(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
    }
    
    /**
     * 获取总页数
     */
    public long getPages() {
        if (size == 0) {
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
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
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
