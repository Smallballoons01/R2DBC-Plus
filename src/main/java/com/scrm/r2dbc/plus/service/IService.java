package com.scrm.r2dbc.plus.service;

import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryChainWrapper;
import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateChainWrapper;
import com.scrm.r2dbc.plus.conditions.Wrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;

/**
 * 响应式服务接口，类似于 MyBatis-Plus 的 IService
 * 
 * @param <T> 实体类型
 * @author dason
 */
public interface IService<T> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return 插入是否成功
     */
    Mono<Boolean> save(T entity);

    /**
     * 批量插入
     *
     * @param entityList 实体对象集合
     * @return 插入是否成功
     */
    Mono<Boolean> saveBatch(Collection<T> entityList);

    /**
     * 批量插入，支持分批处理
     *
     * @param entityList 实体对象集合
     * @param batchSize 每批处理的数量
     * @return 插入是否成功
     */
    Mono<Boolean> saveBatch(Collection<T> entityList, int batchSize);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     * @return 删除是否成功
     */
    Mono<Boolean> removeById(Serializable id);

    /**
     * 根据 ID 批量删除
     *
     * @param idList 主键ID列表
     * @return 删除是否成功
     */
    Mono<Boolean> removeByIds(Collection<? extends Serializable> idList);

    /**
     * 根据条件删除
     *
     * @param queryWrapper 查询条件
     * @return 删除是否成功
     */
    Mono<Boolean> remove(Wrapper<T> queryWrapper);

    /**
     * 根据 ID 更新
     *
     * @param entity 实体对象
     * @return 更新是否成功
     */
    Mono<Boolean> updateById(T entity);


    /**
     * 根据 ID 批量更新
     *
     * @param entityList 实体对象集合
     * @return 更新是否成功
     */
    Mono<Boolean> updateBatchById(Collection<T> entityList);


    /**
     * 根据 ID 批量更新
     *
     * @param entityList 实体对象集合
     * @param batchSize 每批处理的数量
     * @return 更新是否成功
     */
    Mono<Boolean> updateBatchById(Collection<T> entityList, int batchSize);


    /**
     * 根据条件更新
     *
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 更新是否成功
     */
    Mono<Boolean> update(T entity, Wrapper<T> updateWrapper);

    /**
     * 根据条件更新
     *
     * @param updateWrapper 更新条件
     * @return 更新是否成功
     */
    Mono<Boolean> update(Wrapper<T> updateWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return 实体对象
     */
    Mono<T> getById(Serializable id);

    /**
     * 根据 ID 批量查询
     *
     * @param idList 主键ID列表
     * @return 实体对象列表
     */
    Flux<T> listByIds(Collection<? extends Serializable> idList);

    /**
     * 查询所有记录
     *
     * @return 实体对象列表
     */
    Flux<T> list();

    /**
     * 根据条件查询列表
     *
     * @param queryWrapper 查询条件
     * @return 实体对象列表
     */
    Flux<T> list(Wrapper<T> queryWrapper);

    /**
     * 根据条件查询一条记录
     *
     * @param queryWrapper 查询条件
     * @return 实体对象
     */
    Mono<T> getOne(Wrapper<T> queryWrapper);

    /**
     * 查询记录数
     *
     * @return 记录数
     */
    Mono<Long> count();


    /**
     * 根据条件查询记录数
     *
     * @param queryWrapper 查询条件
     * @return 记录数
     */
    Mono<Long> count(Wrapper<T> queryWrapper);

    /**
     * 根据条件查询是否存在记录
     *
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    Mono<Boolean> exists(Wrapper<T> queryWrapper);

    /**
     * 获取 Lambda 查询链式包装器
     * 参考 mybatis-plus 的 lambdaQuery 方法，返回可以直接执行查询的链式包装器
     *
     * @return LambdaQueryChainWrapper
     */
    LambdaQueryChainWrapper<T> lambdaQuery();

    /**
     * 获取 Lambda 更新链式包装器
     *
     * @return LambdaUpdateChainWrapper
     */
    LambdaUpdateChainWrapper<T> lambdaUpdate();

    /**
     * 保存或更新
     *
     * @param entity 实体对象
     * @return 操作是否成功
     */
    Mono<Boolean> saveOrUpdate(T entity);

    /**
     * 批量保存或更新
     *
     * @param entityList 实体对象集合
     * @return 操作是否成功
     */
    Mono<Boolean> saveOrUpdateBatch(Collection<T> entityList);

    /**
     * 批量保存或更新，支持分批处理
     *
     * @param entityList 实体对象集合
     * @param batchSize 每批处理的数量
     * @return 操作是否成功
     */
    Mono<Boolean> saveOrUpdateBatch(Collection<T> entityList, int batchSize);

    /**
     * 分页查询
     *
     * @param page 分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    Mono<Page<T>> page(Page<T> page, Wrapper<T> queryWrapper);

    /**
     * 分页查询
     *
     * @param page 分页对象
     * @return 分页结果
     */
    Mono<Page<T>> page(Page<T> page);
}
