package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * r2dbc-plus Mapper 注解
 * 用于标识 Mapper 接口
 *
 * @author dason
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapper {
    
    /**
     * Mapper 名称
     */
    String value() default "";
    
    /**
     * XML 映射文件路径
     */
    String xmlLocation() default "";
}
