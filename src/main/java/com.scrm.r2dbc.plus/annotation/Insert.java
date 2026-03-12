package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * Insert 插入注解
 * 用于在 Mapper 接口方法上定义 INSERT SQL 语句
 * 类似于 MyBatis 的 @Insert 注解
 *
 * @author dason
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Insert {
    
    /**
     * SQL 插入语句
     * 支持使用 #{paramName} 或 ${paramName} 进行参数绑定
     * 
     * @return SQL 插入语句
     */
    String value();
}
