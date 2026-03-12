package com.scrm.r2dbc.plus.conditions.query;

import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.page.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Lambda 查询链式包装器
 * 提供链式调用的查询操作
 * 
 * @param <T> 实体类型
 * @author dason
 */
public class LambdaQueryChainWrapper<T> {

    private final BaseMapper<T> baseMapper;
    private final LambdaQueryWrapper<T> queryWrapper;

    public LambdaQueryChainWrapper(BaseMapper<T> baseMapper, Class<T> entityClass) {
        this.baseMapper = baseMapper;
        this.queryWrapper = new LambdaQueryWrapper<>(entityClass);
    }

    // 查询条件方法
    public LambdaQueryChainWrapper<T> eq(SFunction<T, ?> column, Object val) {
        queryWrapper.eq(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.eq(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> ne(SFunction<T, ?> column, Object val) {
        queryWrapper.ne(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.ne(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> gt(SFunction<T, ?> column, Object val) {
        queryWrapper.gt(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.gt(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> ge(SFunction<T, ?> column, Object val) {
        queryWrapper.ge(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.ge(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> lt(SFunction<T, ?> column, Object val) {
        queryWrapper.lt(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.lt(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> le(SFunction<T, ?> column, Object val) {
        queryWrapper.le(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.le(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> like(SFunction<T, ?> column, Object val) {
        queryWrapper.like(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> like(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.like(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> notLike(SFunction<T, ?> column, Object val) {
        queryWrapper.notLike(column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> notLike(boolean condition, SFunction<T, ?> column, Object val) {
        queryWrapper.notLike(condition, column, val);
        return this;
    }

    public LambdaQueryChainWrapper<T> in(SFunction<T, ?> column, Collection<?> values) {
        queryWrapper.in(column, values);
        return this;
    }

    public LambdaQueryChainWrapper<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        queryWrapper.in(condition, column, values);
        return this;
    }

    public LambdaQueryChainWrapper<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        queryWrapper.notIn(column, values);
        return this;
    }

    public LambdaQueryChainWrapper<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        queryWrapper.notIn(condition, column, values);
        return this;
    }

    public LambdaQueryChainWrapper<T> isNull(SFunction<T, ?> column) {
        queryWrapper.isNull(column);
        return this;
    }

    public LambdaQueryChainWrapper<T> isNull(boolean condition, SFunction<T, ?> column) {
        queryWrapper.isNull(condition, column);
        return this;
    }

    public LambdaQueryChainWrapper<T> isNotNull(SFunction<T, ?> column) {
        queryWrapper.isNotNull(column);
        return this;
    }

    public LambdaQueryChainWrapper<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        queryWrapper.isNotNull(condition, column);
        return this;
    }

    public LambdaQueryChainWrapper<T> between(SFunction<T, ?> column, Object val1, Object val2) {
        queryWrapper.between(column, val1, val2);
        return this;
    }

    public LambdaQueryChainWrapper<T> between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        queryWrapper.between(condition, column, val1, val2);
        return this;
    }

    public LambdaQueryChainWrapper<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        queryWrapper.notBetween(column, val1, val2);
        return this;
    }

    public LambdaQueryChainWrapper<T> notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        queryWrapper.notBetween(condition, column, val1, val2);
        return this;
    }

    // 逻辑连接
    public LambdaQueryChainWrapper<T> and() {
        queryWrapper.and();
        return this;
    }

    public LambdaQueryChainWrapper<T> or() {
        queryWrapper.or();
        return this;
    }

    // 排序方法
    public LambdaQueryChainWrapper<T> orderByAsc(SFunction<T, ?> column) {
        queryWrapper.orderByAsc(column);
        return this;
    }

    public LambdaQueryChainWrapper<T> orderByDesc(SFunction<T, ?> column) {
        queryWrapper.orderByDesc(column);
        return this;
    }

    public LambdaQueryChainWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
        queryWrapper.orderBy(condition, isAsc, column);
        return this;
    }

    public LambdaQueryChainWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        queryWrapper.orderBy(isAsc, column);
        return this;
    }

    // 分页方法
    public LambdaQueryChainWrapper<T> limit(long limit) {
        queryWrapper.limit(limit);
        return this;
    }

    public LambdaQueryChainWrapper<T> offset(long offset) {
        queryWrapper.offset(offset);
        return this;
    }

    public LambdaQueryChainWrapper<T> page(long current, long size) {
        queryWrapper.page(current, size);
        return this;
    }

    // 最后拼接的SQL
    public LambdaQueryChainWrapper<T> last(String lastSql) {
        queryWrapper.last(lastSql);
        return this;
    }

    // 查询字段选择
    @SafeVarargs
    public final LambdaQueryChainWrapper<T> select(SFunction<T, ?>... columns) {
        queryWrapper.select(columns);
        return this;
    }

    // 执行查询方法
    public Flux<T> list() {
        return baseMapper.selectList(queryWrapper);
    }

    public Mono<T> one() {
        return baseMapper.selectOne(queryWrapper);
    }

    public Mono<Long> count() {
        return baseMapper.selectCount(queryWrapper);
    }

    public Mono<Page<T>> page(Page<T> page) {
        return baseMapper.selectPage(page, queryWrapper);
    }

    // 获取内部的查询包装器
    public LambdaQueryWrapper<T> getWrapper() {
        return queryWrapper;
    }
}