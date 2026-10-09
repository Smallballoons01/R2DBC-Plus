package com.scrm.r2dbc.plus.conditions;

import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.Collection;

/**
 * 抽象 Lambda 条件构造器
 * 类似于 MyBatis-Plus 的 AbstractLambdaWrapper
 * 
 * @param <T> 实体类型
 * @param <R> 子类类型，用于链式调用
 * @author dason
 */
public abstract class AbstractLambdaWrapper<T, R extends AbstractLambdaWrapper<T, R>> extends AbstractWrapper<T, R> {

    public AbstractLambdaWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    /**
     * 等于条件
     */
    public R eq(SFunction<T, ?> column, Object val) {
        return super.eq(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 等于条件（支持条件判断）
     */
    public R eq(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? eq(column, val) : typedThis();
    }

    /**
     * 不等于条件
     */
    public R ne(SFunction<T, ?> column, Object val) {
        return super.ne(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 不等于条件（支持条件判断）
     */
    public R ne(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? ne(column, val) : typedThis();
    }

    /**
     * 大于条件
     */
    public R gt(SFunction<T, ?> column, Object val) {
        return super.gt(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 大于条件（支持条件判断）
     */
    public R gt(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? gt(column, val) : typedThis();
    }

    /**
     * 大于等于条件
     */
    public R ge(SFunction<T, ?> column, Object val) {
        return super.ge(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 大于等于条件（支持条件判断）
     */
    public R ge(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? ge(column, val) : typedThis();
    }

    /**
     * 小于条件
     */
    public R lt(SFunction<T, ?> column, Object val) {
        return super.lt(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 小于条件（支持条件判断）
     */
    public R lt(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? lt(column, val) : typedThis();
    }

    /**
     * 小于等于条件
     */
    public R le(SFunction<T, ?> column, Object val) {
        return super.le(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 小于等于条件（支持条件判断）
     */
    public R le(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? le(column, val) : typedThis();
    }

    /**
     * LIKE 条件
     */
    public R like(SFunction<T, ?> column, Object val) {
        return super.like(LambdaUtils.getColumnName(column), val);
    }

    /**
     * LIKE 条件（支持条件判断）
     */
    public R like(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? like(column, val) : typedThis();
    }

    /**
     * NOT LIKE 条件
     */
    public R notLike(SFunction<T, ?> column, Object val) {
        return super.notLike(LambdaUtils.getColumnName(column), val);
    }

    /**
     * NOT LIKE 条件（支持条件判断）
     */
    public R notLike(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? notLike(column, val) : typedThis();
    }

    /**
     * IS NULL 条件
     */
    public R isNull(SFunction<T, ?> column) {
        return super.isNull(LambdaUtils.getColumnName(column));
    }

    /**
     * IS NULL 条件（支持条件判断）
     */
    public R isNull(boolean condition, SFunction<T, ?> column) {
        return condition ? isNull(column) : typedThis();
    }

    /**
     * IS NOT NULL 条件
     */
    public R isNotNull(SFunction<T, ?> column) {
        return super.isNotNull(LambdaUtils.getColumnName(column));
    }

    /**
     * IS NOT NULL 条件（支持条件判断）
     */
    public R isNotNull(boolean condition, SFunction<T, ?> column) {
        return condition ? isNotNull(column) : typedThis();
    }

    /**
     * IN 条件
     */
    public R in(SFunction<T, ?> column, Collection<?> values) {
        return super.in(LambdaUtils.getColumnName(column), values);
    }

    /**
     * IN 条件（支持条件判断）
     */
    public R in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return condition ? in(column, values) : typedThis();
    }

    /**
     * NOT IN 条件
     */
    public R notIn(SFunction<T, ?> column, Collection<?> values) {
        return super.notIn(LambdaUtils.getColumnName(column), values);
    }

    /**
     * NOT IN 条件（支持条件判断）
     */
    public R notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return condition ? notIn(column, values) : typedThis();
    }

    /**
     * BETWEEN 条件
     */
    public R between(SFunction<T, ?> column, Object val1, Object val2) {
        return super.between(LambdaUtils.getColumnName(column), val1, val2);
    }

    /**
     * BETWEEN 条件（支持条件判断）
     */
    public R between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        return condition ? between(column, val1, val2) : typedThis();
    }

    /**
     * NOT BETWEEN 条件
     */
    public R notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        return super.notBetween(LambdaUtils.getColumnName(column), val1, val2);
    }

    /**
     * NOT BETWEEN 条件（支持条件判断）
     */
    public R notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        return condition ? notBetween(column, val1, val2) : typedThis();
    }
}
