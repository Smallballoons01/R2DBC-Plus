package com.scrm.r2dbc.plus.generator;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 默认 ID 生成器实现
 * 
 * @author dason
 */
public class DefaultIdGenerator implements IdGenerator {
    
    /**
     * 雪花算法相关常量
     */
    private static final long EPOCH = 1640995200000L; // 2022-01-01 00:00:00
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public DefaultIdGenerator() {
        // 简单的工作节点ID生成策略
        this.workerId = ThreadLocalRandom.current().nextLong(0, MAX_WORKER_ID + 1);
        this.datacenterId = ThreadLocalRandom.current().nextLong(0, MAX_DATACENTER_ID + 1);
    }
    
    public DefaultIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_ID + " or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    
    @Override
    public synchronized Serializable nextId(Object entity) {
        return nextLong();
    }
    
    /**
     * 生成雪花算法 ID
     */
    public synchronized long nextLong() {
        long timestamp = timeGen();
        
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }
        
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
               (datacenterId << DATACENTER_ID_SHIFT) |
               (workerId << WORKER_ID_SHIFT) |
               sequence;
    }
    

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
