package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * Select 查询注解
 * 用于在 Mapper 接口方法上定义 SELECT SQL 语句
 * 类似于 MyBatis 的 @Select 注解
 *
 * @author dason
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Select {
    
    /**
     * SQL 查询语句
     * 支持使用 #{paramName} 或 ${paramName} 进行参数绑定
     * 
     * @return SQL 查询语句
     */
    String value();
}
