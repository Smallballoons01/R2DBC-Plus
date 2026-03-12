package com.scrm.r2dbc.plus.enums;

/**
 * 主键ID生成类型枚举
 */
public enum IdType {
    /**
     * 数据库ID自增
     * 该类型请确保数据库设置了 ID自增 否则无效
     */
    AUTO(0),

    /**
     * 该类型为未设置主键类型(注解里等于跟随全局,全局里约等于 INPUT)
     */
    NONE(1),

    /**
     * 用户输入ID
     * 该类型可以通过自己注册自动填充插件进行填充
     */
    INPUT(2),

    /**
     * 分配ID (主键类型为number或string）
     * 使用雪花算法生成ID
     */
    ASSIGN_ID(3),

    /**
     * 分配UUID (主键类型为 string)
     * 使用UUID生成器生成ID
     */
    ASSIGN_UUID(4),

    /**
     * 分配有序UUID (主键类型为 string)
     * 使用有序UUID生成器生成ID
     */
    ASSIGN_ORDER_UUID(5);

    private final int key;

    IdType(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}