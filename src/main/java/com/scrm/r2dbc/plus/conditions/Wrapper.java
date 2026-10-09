package com.scrm.r2dbc.plus.conditions;

import java.util.Map;

/**
 * 条件构造器基础接口
 * 类似于 MyBatis-Plus 的 Wrapper
 *
 * @param <T> 实体类型
 * @author dason
 */
public interface Wrapper<T> extends ISqlSegment {

    /**
     * 获取参数映射
     *
     * @return 参数映射
     */
    Map<String, Object> getParamNameValuePairs();

    /**
     * 获取实体类型
     *
     * @return 实体类型
     */
    Class<T> getEntityClass();

    /**
     * 获取表名
     *
     * @return 表名
     */
    String getTableName();
}
