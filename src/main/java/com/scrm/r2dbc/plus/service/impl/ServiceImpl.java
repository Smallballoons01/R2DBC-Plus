package com.scrm.r2dbc.plus.service.impl;

import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.service.IService;
import com.scrm.r2dbc.plus.util.EntityUtils;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryChainWrapper;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateChainWrapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import com.scrm.r2dbc.plus.conditions.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * IService 默认实现
 * 
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @author dason
 */
public class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {

    @Autowired(required = false)
    protected M baseMapper;
    protected Class<T> entityClass;

    public ServiceImpl() {
        // Spring 会自动注入 baseMapper（通过 @Autowired），entityClass 会通过泛型反射自动获取
        // 子类如需自定义，可通过 setEntityClass() 方法设置
    }

    public ServiceImpl(M baseMapper, Class<T> entityClass) {
        this.baseMapper = baseMapper;
        this.entityClass = entityClass;
    }

    @Override
    public Mono<Boolean> save(T entity) {
        return baseMapper.insert(entity).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, 1000); // 默认批量大小为1000
    }

    @Override
    public Mono<Boolean> saveBatch(Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("saveBatch Entity list cannot be empty");
        }

        // 分批处理
        return Flux.fromIterable(entityList)
                .buffer(batchSize)
                .concatMap(batch -> baseMapper.insertBatch(batch))
                .reduce(0, Integer::sum)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> removeById(Serializable id) {
        return baseMapper.deleteById(id).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> removeByIds(Collection<? extends Serializable> idList) {
        return baseMapper.deleteBatchIds(idList).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> remove(Wrapper<T> queryWrapper) {
        return baseMapper.delete(queryWrapper).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> updateById(T entity) {
        return baseMapper.updateById(entity).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> updateBatchById(Collection<T> entityList) {
        return updateBatchById(entityList, 1000);
    }

    @Override
    public Mono<Boolean> updateBatchById(Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("updateBatchById Entity list cannot be empty");
        }

        return Flux.fromIterable(entityList)
                .buffer(batchSize)
                .concatMap(batch -> baseMapper.updateBatchById(batch))
                .reduce(0, Integer::sum)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> update(T entity, Wrapper<T> updateWrapper) {
        return baseMapper.update(entity, updateWrapper).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> update(Wrapper<T> updateWrapper) {
        return baseMapper.update(null, updateWrapper).map(count -> count > 0);
    }


    @Override
    public Mono<T> getById(Serializable id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Flux<T> listByIds(Collection<? extends Serializable> idList) {
        return baseMapper.selectBatchIds(idList);
    }

    @Override
    public Flux<T> list() {
        return baseMapper.selectList();
    }

    @Override
    public Flux<T> list(Wrapper<T> queryWrapper) {
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Mono<T> getOne(Wrapper<T> queryWrapper) {
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Mono<Long> count() {
        return baseMapper.selectCount(new QueryWrapper<>());
    }

    @Override
    public Mono<Long> count(Wrapper<T> queryWrapper) {
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public Mono<Boolean> exists(Wrapper<T> queryWrapper) {
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(baseMapper, getEntityClass());
    }

    @Override
    public LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(baseMapper, getEntityClass());
    }

    @Override
    public Mono<Boolean> saveOrUpdate(T entity) {
        Object idValue = EntityUtils.getIdValue(entity);
        if (idValue == null) {
            // 没有ID，执行插入
            return save(entity);
        } else {
            // 有ID，先查询是否存在
            return getById((Serializable) idValue)
                    .hasElement()
                    .flatMap(exists -> {
                        if (exists) {
                            return updateById(entity);
                        } else {
                            return save(entity);
                        }
                    });
        }
    }

    @Override
    public Mono<Boolean> saveOrUpdateBatch(Collection<T> entityList) {
        return saveOrUpdateBatch(entityList, 1000); // 默认批量大小为1000
    }

    @Override
    public Mono<Boolean> saveOrUpdateBatch(Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("saveOrUpdateBatch Entity list cannot be empty");
        }

        return Flux.fromIterable(entityList)
                .buffer(batchSize)
                .concatMap(this::processSaveOrUpdateBatch)
                .reduce(0, Integer::sum)
                .map(count -> count > 0);
    }

    /**
     * 处理单批次的保存或更新操作
     * 将实体分为插入和更新两组，分别进行批量操作
     */
    private Mono<Integer> processSaveOrUpdateBatch(List<T> batch) {
        List<T> insertList = new ArrayList<>();
        List<T> updateList = new ArrayList<>();

        // 分离插入和更新的实体
        for (T entity : batch) {
            Object idValue = EntityUtils.getIdValue(entity);
            if (idValue == null) {
                // 没有ID，需要插入
                insertList.add(entity);
            } else {
                // 有ID，需要检查是否存在，然后决定插入还是更新
                updateList.add(entity);
            }
        }

        Mono<Integer> insertResult = insertList.isEmpty() ?
                Mono.just(0) : baseMapper.insertBatch(insertList);

        Mono<Integer> updateResult = updateList.isEmpty() ?
                Mono.just(0) : baseMapper.updateBatchById(updateList);

        return Mono.zip(insertResult, updateResult)
                .map(tuple -> tuple.getT1() + tuple.getT2());
    }

    /**
     * 获取实体类型
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        if (entityClass != null) {
            return entityClass;
        }
        
        // 如果没有设置，尝试从泛型中获取
        try {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) superClass;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length >= 2) {
                    // 第二个泛型参数是实体类型 T
                    Type entityType = typeArguments[1];
                    if (entityType instanceof Class) {
                        entityClass = (Class<T>) entityType;
                        return entityClass;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常，继续抛出原始错误
        }
        
        throw new RuntimeException("Entity class not set. Please set entityClass in constructor or override getEntityClass method.");
    }

    /**
     * 设置 BaseMapper
     */
    public void setBaseMapper(M baseMapper) {
        this.baseMapper = baseMapper;
    }

    /**
     * 设置实体类型
     */
    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 获取 BaseMapper
     */
    public M getBaseMapper() {
        return baseMapper;
    }

    @Override
    public Mono<Page<T>> page(Page<T> page, Wrapper<T> queryWrapper) {
        // 验证分页大小，限制最大分页数量为2000
        if (page != null && page.getSize() > 2000) {
            return Mono.error(new RuntimeException("page query: size limit of 2000"));
        }
        
        // 如果 queryWrapper 为 null，创建一个默认的 QueryWrapper 用于分页
        final Wrapper<T> dataWrapper;
        if (queryWrapper == null) {
            dataWrapper = new QueryWrapper<T>(getEntityClass())
                    .page(page.getCurrent(), page.getSize());
        } else {
            dataWrapper = queryWrapper;
        }

        // 根据 Wrapper 类型创建用于计数的查询包装器
        Wrapper<T> countWrapper;
        if (queryWrapper instanceof QueryWrapper) {
            countWrapper = ((QueryWrapper<T>) queryWrapper).forCount();
        } else if (queryWrapper instanceof LambdaQueryWrapper) {
            countWrapper = ((LambdaQueryWrapper<T>) queryWrapper).forCount();
        } else {
            // 对于其他类型的 Wrapper 或 null，直接使用原包装器进行计数
            countWrapper = queryWrapper;
        }

        // 先查询总数
        return baseMapper.selectCount(countWrapper)
                .flatMap(total -> {
                    page.setTotal(total);
                    if (total == 0) {
                        page.setRecords(java.util.Collections.emptyList());
                        return Mono.just(page);
                    }

                    // 设置分页参数（如果原始 queryWrapper 不为 null）
                    if (queryWrapper != null) {
                        if (dataWrapper instanceof QueryWrapper) {
                            ((QueryWrapper<T>) dataWrapper).page(page.getCurrent(), page.getSize());
                        } else if (dataWrapper instanceof LambdaQueryWrapper) {
                            ((LambdaQueryWrapper<T>) dataWrapper).page(page.getCurrent(), page.getSize());
                        }
                    }
                    // 如果 queryWrapper 为 null，dataWrapper 已经在创建时设置了分页参数

                    // 查询数据
                    return baseMapper.selectList(dataWrapper)
                            .collectList()
                            .map(records -> {
                                page.setRecords(records);
                                return page;
                            });
                });
    }

    @Override
    public Mono<Page<T>> page(Page<T> page) {
        return page(page, new QueryWrapper<>(getEntityClass()));
    }
}
