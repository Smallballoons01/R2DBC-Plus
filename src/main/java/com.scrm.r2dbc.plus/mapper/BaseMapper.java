package com.scrm.r2dbc.plus.mapper;

import com.scrm.r2dbc.plus.conditions.Wrapper;
import com.scrm.r2dbc.plus.page.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;

/**
 * 响应式 Mapper 基础接口，类似于 MyBatis-Plus 的 BaseMapper
 * 
 * @param <T> 实体类型
 * @author dason
 */
public interface BaseMapper<T> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return 插入成功的记录数
     */
    Mono<Integer> insert(T entity);

    /**
     * 批量插入记录
     *
     * @param entityList 实体对象集合
     * @return 插入成功的记录数
     */
    Mono<Integer> insertBatch(Collection<T> entityList);

    /**
     * 批量插入记录
     *
     * @param entityList 实体对象集合
     * @param batchSize 分批数量
     * @return 插入成功的记录数
     */
    Mono<Integer> insertBatch(Collection<T> entityList, int batchSize);

    /**
     * 批量更新记录（根据ID）
     *
     * @param entityList 实体对象集合
     * @return 更新成功的记录数
     */
    Mono<Integer> updateBatchById(Collection<T> entityList);

    /**
     * 根据 ID 删除一条记录
     *
     * @param id 主键ID
     * @return 删除成功的记录数
     */
    Mono<Integer> deleteById(Serializable id);

    /**
     * 根据 ID 批量删除
     *
     * @param idList 主键ID列表
     * @return 删除成功的记录数
     */
    Mono<Integer> deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据条件删除
     *
     * @param queryWrapper 查询条件
     * @return 删除成功的记录数
     */
    Mono<Integer> delete(Wrapper<T> queryWrapper);

    /**
     * 根据 ID 更新
     *
     * @param entity 实体对象
     * @return 更新成功的记录数
     */
    Mono<Integer> updateById(T entity);

    /**
     * 根据条件更新
     *
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 更新成功的记录数
     */
    Mono<Integer> update(T entity, Wrapper<T> updateWrapper);

    /**
     * 根据 ID 查询一条记录
     *
     * @param id 主键ID
     * @return 实体对象
     */
    Mono<T> selectById(Serializable id);

    /**
     * 根据 ID 批量查询
     *
     * @param idList 主键ID列表
     * @return 实体对象列表
     */
    Flux<T> selectBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据条件查询一条记录
     *
     * @param queryWrapper 查询条件
     * @return 实体对象
     */
    Mono<T> selectOne(Wrapper<T> queryWrapper);

    /**
     * 根据条件查询记录数
     *
     * @param queryWrapper 查询条件
     * @return 记录数
     */
    Mono<Long> selectCount(Wrapper<T> queryWrapper);

    /**
     * 根据条件查询所有记录
     *
     * @param queryWrapper 查询条件
     * @return 实体对象列表
     */
    Flux<T> selectList(Wrapper<T> queryWrapper);

    /**
     * 查询所有记录
     *
     * @return 实体对象列表
     */
    Flux<T> selectList();

    /**
     * 根据条件查询是否存在记录
     *
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    Mono<Boolean> exists(Wrapper<T> queryWrapper);

    /**
     * 分页查询
     *
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    Mono<Page<T>> selectPage(Page<T> page, Wrapper<T> queryWrapper);
}
