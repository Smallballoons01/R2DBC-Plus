package com.scrm.r2dbc.plus.enums;

/**
 * 字段验证策略枚举
 */
public enum FieldStrategy {
    /**
     * 忽略判断
     */
    IGNORED,
    /**
     * 非NULL判断
     */
    NOT_NULL,
    /**
     * 非空判断(只对字符串类型字段,其他类型字段依然为非NULL判断)
     */
    NOT_EMPTY,
    /**
     * 默认的,一般只用于注解里
     * 1. 在全局里代表 NOT_NULL
     * 2. 在注解里代表 跟随全局
     */
    DEFAULT,
    /**
     * 不加入 SQL
     */
    NEVER
}