package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.fill.MetaObjectHandler;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.generator.IdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.List;


/**
 * R2dbc-Plus 自动配置类
 * 
 * @author dason
 */
@Slf4j
@Configuration
public class R2dbcPlusAutoConfiguration {
    
    @Value("${r2dbc-plus.mapper-locations:classpath*:mapper/**/*.xml}")
    private String[] mapperLocations;
    
    @Bean
    @ConditionalOnMissingBean
    public XmlSqlExecutor xmlSqlExecutor(DatabaseClient databaseClient, R2dbcEntityTemplate r2dbcEntityTemplate) {
        return new XmlSqlExecutor(databaseClient, r2dbcEntityTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public R2dbcDataSourceRegistry r2dbcDataSourceRegistry() {
        return new R2dbcDataSourceRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator() {
        return new DefaultIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public FieldFillProcessor fieldFillProcessor(IdGenerator idGenerator,
                                                 ObjectProvider<List<MetaObjectHandler>> metaObjectHandlersProvider,
                                                 ObjectProvider<LogicDeleteProcessor> logicDeleteProcessorProvider,
                                                 ObjectProvider<VersionProcessor> versionProcessorProvider) {
        FieldFillProcessor processor = new FieldFillProcessor();
        // 通过反射设置依赖，因为没有公共setter方法
        try {
            setField(processor, "idGenerator", idGenerator);
            setField(processor, "metaObjectHandlers", metaObjectHandlersProvider.getIfAvailable());
            setField(processor, "logicDeleteProcessor", logicDeleteProcessorProvider.getIfAvailable());
            setField(processor, "versionProcessor", versionProcessorProvider.getIfAvailable());
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies into FieldFillProcessor", e);
        }
        return processor;
    }
    
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultMetaObjectHandler defaultMetaObjectHandler() {
        return new DefaultMetaObjectHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogicDeleteProcessor logicDeleteProcessor() {
        return new LogicDeleteProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public VersionProcessor versionProcessor() {
        return new VersionProcessor();
    }
    
    @PostConstruct
    public void initMappers() {
        // 解析 XML 映射文件
        for (String location : mapperLocations) {
            log.info("解析XML映射文件: {}", location);
            XmlMapperParser.parseMapperXmls(location);
        }
    }
}
