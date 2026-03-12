package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注解，用于指定方法参数在 SQL 中的名称
 * 类似于 MyBatis 的 @Param 注解
 * 
 * @author dason
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    
    /**
     * 参数名称
     * 
     * @return 参数在 SQL 中使用的名称
     */
    String value();
}
