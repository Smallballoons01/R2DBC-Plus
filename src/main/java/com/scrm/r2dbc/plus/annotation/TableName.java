package com.scrm.r2dbc.plus.annotation;

import java.lang.annotation.*;

/**
 * 表名注解
 * 用于指定实体类对应的数据库表名
 *
 * @author dason
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableName {
    
    /**
     * 表名
     */
    String value();
    
    /**
     * 数据库架构名
     */
    String schema() default "";
    
    /**
     * 是否保持使用全局的 tablePrefix 的值
     * 只生效于 既设置了全局的 tablePrefix 也设置了上面 value 的值
     * 1. 当全局 tablePrefix 为 `t_` 且 value 为 `user` 时
     * 2. 如果此值为 true，则最终表名为 `t_user`
     * 3. 如果此值为 false，则最终表名为 `user`
     */
    boolean keepGlobalPrefix() default false;
    
    /**
     * 实体映射结果集
     */
    String resultMap() default "";
    
    /**
     * 是否自动构建 resultMap 并使用
     * 只生效于 resultMap 为空时
     */
    boolean autoResultMap() default false;
    
    /**
     * 需要排除的属性名
     */
    String[] excludeProperty() default {};
}
