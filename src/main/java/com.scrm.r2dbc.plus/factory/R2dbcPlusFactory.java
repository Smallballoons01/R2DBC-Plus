package com.scrm.r2dbc.plus.factory;

import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.mapper.impl.BaseMapperImpl;
import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.service.IService;
import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * R2dbc-Plus 工厂类，用于创建 Mapper 和 Service 实例
 * 
 * @author dason
 */
@Component
public class R2dbcPlusFactory {
    
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private MapperProxyFactory mapperProxyFactory;
    
    @Autowired(required = false)
    private FieldFillProcessor fieldFillProcessor;

    @Autowired(required = false)
    private LogicDeleteProcessor logicDeleteProcessor;
    
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
        // 注入 FieldFillProcessor
        if (fieldFillProcessor != null) {
            try {
                Field field = BaseMapperImpl.class.getDeclaredField("fieldFillProcessor");
                field.setAccessible(true);
                field.set(mapper, fieldFillProcessor);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject FieldFillProcessor into BaseMapperImpl", e);
            }
        }

        // 注入 LogicDeleteProcessor
        if (logicDeleteProcessor != null) {
            try {
                Field field = BaseMapperImpl.class.getDeclaredField("logicDeleteProcessor");
                field.setAccessible(true);
                field.set(mapper, logicDeleteProcessor);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject LogicDeleteProcessor into BaseMapperImpl", e);
            }
        }
    }
}
