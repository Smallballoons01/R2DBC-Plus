package com.scrm.r2dbc.plus.generator;

import com.github.f4b6a3.uuid.UuidCreator;

import java.io.Serializable;
import java.util.UUID;

/**
 * ID 生成器接口
 * 
 * @author dason
 */
public interface IdGenerator {
    
    /**
     * 生成 ID
     * 
     * @param entity 实体对象
     * @return 生成的 ID
     */
    Serializable nextId(Object entity);

    /**
     * 生成UUID
     *
     * @param entity 实体对象
     * @return 生成的 UUID
     */
    default Serializable nextUUID(Object entity) {
        return UUID.randomUUID().toString().replace("-", "");
    };

    /**
     * 生成有序 UUID
     *
     * @param entity 实体对象
     * @return 生成的 UUID
     */
    default Serializable nextOrderedUUID(Object entity) {
        return UuidCreator.getTimeOrderedEpoch().toString().replace("-", "");
    };
}
