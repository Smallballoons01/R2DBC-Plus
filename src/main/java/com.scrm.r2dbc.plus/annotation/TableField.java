package com.scrm.r2dbc.plus.annotation;

import com.scrm.r2dbc.plus.enums.FieldFill;
import com.scrm.r2dbc.plus.enums.FieldStrategy;

import java.lang.annotation.*;

/**
 * 表字段注解
 * 用于标识实体类的字段与数据库列的映射关系
 *
 * @author dason
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableField {
    
    /**
     * 数据库字段名
     */
    String value() default "";
    
    /**
     * 是否为数据库表字段
     * 默认 true 存在，false 不存在
     */
    boolean exist() default true;
    
    /**
     * 字段验证策略之 insert: 当insert操作时，该字段拼接insert语句时的策略
     * IGNORED: 直接忽略
     * NOT_NULL: 非NULL判断,只会在非NULL的情况下,才会进行插入
     * NOT_EMPTY: 非空判断(只对字符串类型字段,其他类型字段依然为非NULL判断)
     * DEFAULT: 追加insert语句
     */
    FieldStrategy insertStrategy() default FieldStrategy.DEFAULT;
    
    /**
     * 字段验证策略之 update: 当更新操作时，该字段拼接set语句时的策略
     */
    FieldStrategy updateStrategy() default FieldStrategy.DEFAULT;
    
    /**
     * 字段验证策略之 where: 表示该字段在拼接where条件时的策略
     */
    FieldStrategy whereStrategy() default FieldStrategy.DEFAULT;
    
    /**
     * 字段填充策略
     */
    FieldFill fill() default FieldFill.DEFAULT;
    
    /**
     * 是否进行 select 查询
     * 大字段可设置为 false 不加入 select 查询范围
     */
    boolean select() default true;
    
    /**
     * 是否保持使用全局的 columnPrefix 的值
     * 只生效于 既设置了全局的 columnPrefix 也设置了上面 value 的值
     */
    boolean keepGlobalPrefix() default false;
    
    /**
     * JDBC类型 (该默认值不代表会按照该值生效)
     */
    String jdbcType() default "";
    
    /**
     * 类型处理器 (该默认值不代表会按照该值生效)
     */
    String typeHandler() default "";
}
