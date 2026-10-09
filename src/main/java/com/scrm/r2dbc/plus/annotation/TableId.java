package com.scrm.r2dbc.plus.annotation;

import com.scrm.r2dbc.plus.enums.IdType;

import java.lang.annotation.*;

/**
 * 表主键注解
 * 用于标识实体类的主键字段
 *
 * @author dason
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableId {

    /**
     * 主键字段名
     */
    String value() default "";

    /**
     * 主键类型
     */
    IdType type() default IdType.NONE;

}

