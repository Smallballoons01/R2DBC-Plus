package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * R2dbc-Plus Mapper 工厂 Bean
 * 用于创建 Mapper 接口的代理实例
 * 支持多数据源自动选择
 *
 * @author dason
 */
public class R2dbcPlusMapperFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> mapperInterface;

    @Autowired
    private R2dbcDataSourceRegistry dataSourceRegistry;

    @Autowired(required = false)
    private FieldFillProcessor fieldFillProcessor;

    @Autowired(required = false)
    private LogicDeleteProcessor logicDeleteProcessor;

    @Autowired(required = false)
    private XmlSqlExecutor xmlSqlExecutor;

    public R2dbcPlusMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        // 从注册表中获取对应的数据源配置
        R2dbcDataSourceRegistry.DataSourceConfig dataSourceConfig =
                dataSourceRegistry.getDataSourceConfigForPackage(mapperInterface.getPackage().getName());

        // 创建专用的 MapperProxyFactory
        MapperProxyFactory mapperProxyFactory = new MapperProxyFactory(
                dataSourceConfig.getEntityTemplate(),
                dataSourceConfig.getDatabaseClient(),
                fieldFillProcessor,
                xmlSqlExecutor,
                logicDeleteProcessor
        );

        return mapperProxyFactory.createMapperProxy(mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
