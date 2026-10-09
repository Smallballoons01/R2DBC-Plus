package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.factory.R2dbcPlusFactory;
import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.fill.MetaObjectHandler;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.generator.IdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.proxy.MapperProxyFactory;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * R2DBC-Plus 自动配置
 *
 * <p>通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 注册，用户引入依赖后自动生效，无需手动声明。
 *
 * @author dason
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(R2dbcPlusProperties.class)
@ConditionalOnClass({DatabaseClient.class, R2dbcEntityTemplate.class})
@Import(R2dbcPlusMapperScannerConfigurer.class)
public class R2dbcPlusAutoConfiguration {

    private final R2dbcPlusProperties properties;

    /**
     * @param properties 由 {@link EnableConfigurationProperties} 注入的配置项
     */
    public R2dbcPlusAutoConfiguration(R2dbcPlusProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public XmlSqlExecutor xmlSqlExecutor(DatabaseClient databaseClient,
                                          R2dbcEntityTemplate r2dbcEntityTemplate) {
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

    /**
     * 字段填充处理器
     *
     * <p>使用构造器注入而非反射设置字段：反射方式在 Spring 容器初始化顺序变化时
     * 容易拿到未注入的 null，且无法享受 Spring 的依赖管理。
     */
    @Bean
    @ConditionalOnMissingBean
    public FieldFillProcessor fieldFillProcessor(IdGenerator idGenerator,
                                                 ObjectProvider<MetaObjectHandler> metaObjectHandlers,
                                                 ObjectProvider<LogicDeleteProcessor> logicDeleteProcessor,
                                                 ObjectProvider<VersionProcessor> versionProcessor) {
        return new FieldFillProcessor(
                idGenerator,
                metaObjectHandlers.orderedStream().toList(),
                logicDeleteProcessor.getIfAvailable(),
                versionProcessor.getIfAvailable()
        );
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

    /**
     * Mapper 代理工厂：负责为 {@code @Mapper} 接口生成响应式代理
     */
    @Bean
    @ConditionalOnMissingBean
    public MapperProxyFactory mapperProxyFactory(XmlSqlExecutor xmlSqlExecutor,
                                                 R2dbcEntityTemplate r2dbcEntityTemplate,
                                                 DatabaseClient databaseClient,
                                                 ObjectProvider<FieldFillProcessor> fieldFillProcessor,
                                                 ObjectProvider<LogicDeleteProcessor> logicDeleteProcessor) {
        return new MapperProxyFactory(r2dbcEntityTemplate, databaseClient,
                fieldFillProcessor.getIfAvailable(),
                xmlSqlExecutor,
                logicDeleteProcessor.getIfAvailable());
    }

    /**
     * 运行时工厂：用于以编程方式创建 Mapper / Service
     */
    @Bean
    @ConditionalOnMissingBean
    public R2dbcPlusFactory r2dbcPlusFactory(R2dbcEntityTemplate r2dbcEntityTemplate,
                                             DatabaseClient databaseClient,
                                             MapperProxyFactory mapperProxyFactory,
                                             ObjectProvider<FieldFillProcessor> fieldFillProcessor,
                                             ObjectProvider<LogicDeleteProcessor> logicDeleteProcessor) {
        return new R2dbcPlusFactory(r2dbcEntityTemplate, databaseClient, mapperProxyFactory,
                fieldFillProcessor.getIfAvailable(),
                logicDeleteProcessor.getIfAvailable());
    }

    /**
     * 启动时解析 XML 映射文件。
     *
     * <p>设置 {@code r2dbc-plus.xml-enabled=false} 可跳过，纯注解方式使用时无需解析。
     */
    @PostConstruct
    public void initMappers() {
        if (!properties.isXmlEnabled()) {
            log.info("XML 映射已禁用（r2dbc-plus.xml-enabled=false），跳过解析");
            return;
        }
        for (String location : properties.getMapperLocations()) {
            log.info("解析 XML 映射文件: {}", location);
            XmlMapperParser.parseMapperXmls(location);
        }
    }
}