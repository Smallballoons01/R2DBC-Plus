package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * R2DBC Plus 数据源配置注解
 * 用于标识数据源配置类并指定对应的包路径和数据源组件
 * 
 * @author dason
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface R2dbcPlusDataSource {
    
    /**
     * 数据源名称（唯一标识）
     */
    String value() default "";
    
    /**
     * 数据源名称（别名）
     */
    String name() default "";
    
    /**
     * Mapper 扫描包路径
     */
    String[] basePackages() default {};
    
    /**
     * R2dbcEntityTemplate Bean 名称
     */
    String entityTemplate() default "";
    
    /**
     * DatabaseClient Bean 名称
     */
    String databaseClient() default "";
    
    /**
     * 是否为默认数据源
     */
    boolean primary() default false;
}