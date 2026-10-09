package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * 乐观锁版本号注解
 * 用于标识乐观锁版本字段
 * 
 * @author dason
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Version {
}
