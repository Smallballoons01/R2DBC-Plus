package com.scrm.r2dbc.plus.factory;

import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.mapper.impl.BaseMapperImpl;
import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.service.IService;
import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;


/**
 * R2dbc-Plus 工厂类，用于创建 Mapper 和 Service 实例
 * 
 * @author dason
 */
public class R2dbcPlusFactory {
    
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final DatabaseClient databaseClient;
    private final MapperProxyFactory mapperProxyFactory;
    private final FieldFillProcessor fieldFillProcessor;
    private final LogicDeleteProcessor logicDeleteProcessor;

    public R2dbcPlusFactory(R2dbcEntityTemplate r2dbcEntityTemplate,
                            DatabaseClient databaseClient,
                            MapperProxyFactory mapperProxyFactory,
                            FieldFillProcessor fieldFillProcessor,
                            LogicDeleteProcessor logicDeleteProcessor) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.databaseClient = databaseClient;
        this.mapperProxyFactory = mapperProxyFactory;
        this.fieldFillProcessor = fieldFillProcessor;
        this.logicDeleteProcessor = logicDeleteProcessor;
    }
    
    /**
     * 创建 BaseMapper 实例（纯基础功能）
     */
    public <T> BaseMapper<T> createMapper(Class<T> entityClass) {
        BaseMapperImpl<T> mapper = new BaseMapperImpl<>(entityClass, r2dbcEntityTemplate, databaseClient);
        // 手动注入 fieldFillProcessor
        injectFieldFillProcessor(mapper);
        return mapper;
    }

    /**
     * 创建 Mapper 代理实例（支持 XML 自定义方法）
     */
    public <T> T createMapperProxy(Class<T> mapperInterface) {
        return mapperProxyFactory.createMapperProxy(mapperInterface);
    }
    
    /**
     * 创建 IService 实例
     */
    public <T> IService<T> createService(Class<T> entityClass) {
        BaseMapper<T> mapper = createMapper(entityClass);
        return new ServiceImpl<>(mapper, entityClass);
    }
    
    /**
     * 创建自定义 Service 实例
     */
    public <M extends BaseMapper<T>, T> ServiceImpl<M, T> createService(M mapper, Class<T> entityClass) {
        return new ServiceImpl<>(mapper, entityClass);
    }
    
    /**
     * 手动注入依赖到 BaseMapperImpl 实例
     */
    private void injectFieldFillProcessor(BaseMapperImpl<?> mapper) {
        mapper.withProcessors(fieldFillProcessor, logicDeleteProcessor);
    }
}
