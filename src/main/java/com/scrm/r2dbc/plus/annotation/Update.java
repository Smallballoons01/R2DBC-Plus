package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * Update 更新注解
 * 用于在 Mapper 接口方法上定义 UPDATE SQL 语句
 * 类似于 MyBatis 的 @Update 注解
 *
 * @author dason
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Update {
    
    /**
     * SQL 更新语句
     * 支持使用 #{paramName} 或 ${paramName} 进行参数绑定
     * 
     * @return SQL 更新语句
     */
    String value();
}
