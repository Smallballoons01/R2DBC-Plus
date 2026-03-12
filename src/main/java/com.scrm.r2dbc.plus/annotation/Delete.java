package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * Delete 删除注解
 * 用于在 Mapper 接口方法上定义 DELETE SQL 语句
 * 类似于 MyBatis 的 @Delete 注解
 *
 * @author dason
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Delete {
    
    /**
     * SQL 删除语句
     * 支持使用 #{paramName} 或 ${paramName} 进行参数绑定
     * 
     * @return SQL 删除语句
     */
    String value();
}
