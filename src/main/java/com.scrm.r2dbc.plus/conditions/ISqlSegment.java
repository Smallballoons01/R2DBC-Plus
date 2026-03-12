package com.scrm.r2dbc.plus.conditions;

/**
 * SQL 片段接口
 * 类似于 MyBatis-Plus 的 ISqlSegment
 * 
 * @author dason
 */
public interface ISqlSegment {

    /**
     * 获取 SQL 片段
     *
     * @return SQL 片段
     */
    String getSqlSegment();
}
