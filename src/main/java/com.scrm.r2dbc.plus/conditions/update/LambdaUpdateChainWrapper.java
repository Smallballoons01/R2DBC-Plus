package com.scrm.r2dbc.plus.conditions.update;

import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Lambda 更新链式包装器
 * 提供链式调用的更新操作
 * 
 * @param <T> 实体类型
 * @author dason
 */
public class LambdaUpdateChainWrapper<T> {

    private final BaseMapper<T> baseMapper;
    private final LambdaUpdateWrapper<T> updateWrapper;

    public LambdaUpdateChainWrapper(BaseMapper<T> baseMapper, Class<T> entityClass) {
        this.baseMapper = baseMapper;
        this.updateWrapper = new LambdaUpdateWrapper<>(entityClass);
    }

    // SET 方法
    public LambdaUpdateChainWrapper<T> set(SFunction<T, ?> column, Object val) {
        updateWrapper.set(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.set(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> setNull(SFunction<T, ?> column) {
        updateWrapper.setNull(column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> setNull(boolean condition, SFunction<T, ?> column) {
        updateWrapper.setNull(condition, column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> setSql(SFunction<T, ?> column, String sqlSet) {
        updateWrapper.setSql(column, sqlSet);
        return this;
    }

    public LambdaUpdateChainWrapper<T> setSql(boolean condition, SFunction<T, ?> column, String sqlSet) {
        updateWrapper.setSql(condition, column, sqlSet);
        return this;
    }

    // WHERE 条件方法
    public LambdaUpdateChainWrapper<T> eq(SFunction<T, ?> column, Object val) {
        updateWrapper.eq(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.eq(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> ne(SFunction<T, ?> column, Object val) {
        updateWrapper.ne(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.ne(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> gt(SFunction<T, ?> column, Object val) {
        updateWrapper.gt(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.gt(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> ge(SFunction<T, ?> column, Object val) {
        updateWrapper.ge(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.ge(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> lt(SFunction<T, ?> column, Object val) {
        updateWrapper.lt(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.lt(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> le(SFunction<T, ?> column, Object val) {
        updateWrapper.le(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.le(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> like(SFunction<T, ?> column, Object val) {
        updateWrapper.like(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> like(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.like(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notLike(SFunction<T, ?> column, Object val) {
        updateWrapper.notLike(column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notLike(boolean condition, SFunction<T, ?> column, Object val) {
        updateWrapper.notLike(condition, column, val);
        return this;
    }

    public LambdaUpdateChainWrapper<T> in(SFunction<T, ?> column, Collection<?> values) {
        updateWrapper.in(column, values);
        return this;
    }

    public LambdaUpdateChainWrapper<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        updateWrapper.in(condition, column, values);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        updateWrapper.notIn(column, values);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        updateWrapper.notIn(condition, column, values);
        return this;
    }

    public LambdaUpdateChainWrapper<T> isNull(SFunction<T, ?> column) {
        updateWrapper.isNull(column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> isNull(boolean condition, SFunction<T, ?> column) {
        updateWrapper.isNull(condition, column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> isNotNull(SFunction<T, ?> column) {
        updateWrapper.isNotNull(column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        updateWrapper.isNotNull(condition, column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> between(SFunction<T, ?> column, Object val1, Object val2) {
        updateWrapper.between(column, val1, val2);
        return this;
    }

    public LambdaUpdateChainWrapper<T> between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        updateWrapper.between(condition, column, val1, val2);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        updateWrapper.notBetween(column, val1, val2);
        return this;
    }

    public LambdaUpdateChainWrapper<T> notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        updateWrapper.notBetween(condition, column, val1, val2);
        return this;
    }

    // 逻辑连接
    public LambdaUpdateChainWrapper<T> and() {
        updateWrapper.and();
        return this;
    }

    public LambdaUpdateChainWrapper<T> or() {
        updateWrapper.or();
        return this;
    }

    // 排序方法
    public LambdaUpdateChainWrapper<T> orderByAsc(SFunction<T, ?> column) {
        updateWrapper.orderByAsc(column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> orderByDesc(SFunction<T, ?> column) {
        updateWrapper.orderByDesc(column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
        updateWrapper.orderBy(condition, isAsc, column);
        return this;
    }

    public LambdaUpdateChainWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        updateWrapper.orderBy(isAsc, column);
        return this;
    }

    // 最后拼接的SQL
    public LambdaUpdateChainWrapper<T> last(String lastSql) {
        updateWrapper.last(lastSql);
        return this;
    }

    // 执行更新方法
    public Mono<Boolean> update() {
        return baseMapper.update(null, updateWrapper).map(count -> count > 0);
    }

    public Mono<Boolean> update(T entity) {
        return baseMapper.update(entity, updateWrapper).map(count -> count > 0);
    }

    public Mono<Boolean> remove() {
        return baseMapper.delete(updateWrapper.buildQueryWrapper()).map(count -> count > 0);
    }

    // 获取内部的更新包装器
    public LambdaUpdateWrapper<T> getWrapper() {
        return updateWrapper;
    }

    /**
     * 设置更新null值标志
     * 当设置为true时，实体对象中的null字段也会被更新到数据库
     *
     * @return LambdaUpdateChainWrapper 当前实例
     */
    public LambdaUpdateChainWrapper<T> updateNullValue() {
        updateWrapper.updateNullValue();
        return this;
    }

    /**
     * 返回Lambda更新包装器，用于链式调用
     *
     * @return LambdaUpdateWrapper 当前实例的更新包装器
     */
    public LambdaUpdateWrapper<T> lambdaUpdate() {
        return updateWrapper;
    }
}