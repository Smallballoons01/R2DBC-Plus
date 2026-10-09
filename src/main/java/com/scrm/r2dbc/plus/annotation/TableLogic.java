package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * 逻辑删除注解
 * 用于标识逻辑删除字段
 * 
 * @author dason
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableLogic {
    
    /**
     * 删除值（已删除时的值）
     */
    String delval() default "1";
    
    /**
     * 未删除值（正常时的值）
     */
    String value() default "0";
}
