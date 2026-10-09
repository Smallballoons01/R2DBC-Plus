package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.annotation.R2dbcPlusDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * R2DBC 数据源注册表
 * 自动发现和管理 @R2dbcDataSource 配置的数据源映射
 *
 * @author dason
 */
@Slf4j
public class R2dbcDataSourceRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 包路径 → 数据源配置 的映射。
     *
     * <p>本类是单例 Bean，查询会在事件循环线程上并发发生，
     * 因此使用 {@link ConcurrentHashMap}；初始化写入阶段通过
     * {@link #initialize()} 一次性完成，之后视为只读。
     */
    private final Map<String, DataSourceConfig> packageToDataSourceMap = new ConcurrentHashMap<>();
    private volatile DataSourceConfig defaultDataSourceConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void initialize() {
        discoverDataSourceConfigurations();
    }

    /**
     * 自动发现所有 @R2dbcDataSource 配置
     */
    private void discoverDataSourceConfigurations() {
        // 获取所有标注了 @R2dbcDataSource 的配置类
        Map<String, Object> configBeans = applicationContext.getBeansWithAnnotation(R2dbcPlusDataSource.class);
        
        for (Map.Entry<String, Object> entry : configBeans.entrySet()) {
            Object configBean = entry.getValue();
            Class<?> configClass = configBean.getClass();
            
            R2dbcPlusDataSource annotation = AnnotationUtils.findAnnotation(configClass, R2dbcPlusDataSource.class);
            if (annotation != null) {
                String[] basePackages = annotation.basePackages();
                String entityTemplateBeanName = annotation.entityTemplate();
                String databaseClientBeanName = annotation.databaseClient();
                boolean isPrimary = annotation.primary();
                
                if (basePackages.length > 0 && !entityTemplateBeanName.isEmpty()) {
                    try {
                        // 获取对应的 R2dbcEntityTemplate
                        R2dbcEntityTemplate entityTemplate = applicationContext.getBean(entityTemplateBeanName, R2dbcEntityTemplate.class);
                        
                        // 获取对应的 DatabaseClient
                        DatabaseClient databaseClient;
                        if (!databaseClientBeanName.isEmpty()) {
                            // 使用指定的 Bean 名称
                            databaseClient = applicationContext.getBean(databaseClientBeanName, DatabaseClient.class);
                        } else {
                            // 自动推断 DatabaseClient Bean 名称
                            databaseClient = inferDatabaseClient(entityTemplateBeanName);
                        }
                        
                        DataSourceConfig config = new DataSourceConfig(entityTemplate, databaseClient);
                        
                        // 注册每个包路径
                        for (String basePackage : basePackages) {
                            packageToDataSourceMap.put(basePackage, config);
                            log.info("Registered R2DBC data source mapping: {}", basePackage + " -> " + entityTemplateBeanName);
                        }
                        
                        // 设置默认数据源
                        if (isPrimary || defaultDataSourceConfig == null) {
                            defaultDataSourceConfig = config;
                            log.info("Set default R2DBC data source: {}", entityTemplateBeanName);
                        }
                        
                    } catch (Exception e) {
                        log.error("Failed to register R2DBC data source config for {}", entityTemplateBeanName + ": " + e.getMessage());
                    }
                }
            }
        }
        
        // 如果没有找到默认配置，使用第一个配置作为默认
        if (defaultDataSourceConfig == null && !packageToDataSourceMap.isEmpty()) {
            defaultDataSourceConfig = packageToDataSourceMap.values().iterator().next();
        }
        
        log.info("R2DBC data source registry initialized with {}", packageToDataSourceMap.size() + " configurations");
    }
    
    /**
     * 自动推断 DatabaseClient Bean 名称
     */
    private DatabaseClient inferDatabaseClient(String entityTemplateBeanName) {
        // 尝试多种命名约定
        String[] possibleNames = {
            entityTemplateBeanName.replace("R2dbcEntityTemplate", "DatabaseClient"),
            entityTemplateBeanName.replace("EntityTemplate", "DatabaseClient"),
            entityTemplateBeanName.replace("R2dbcEntityTemplate", "Client"),
            entityTemplateBeanName.replace("Template", "Client")
        };
        
        for (String possibleName : possibleNames) {
            try {
                return applicationContext.getBean(possibleName, DatabaseClient.class);
            } catch (Exception ignored) {
                // 继续尝试下一个
            }
        }
        
        // 最后使用默认的 DatabaseClient
        try {
            return applicationContext.getBean(DatabaseClient.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not find DatabaseClient for entityTemplate: " + entityTemplateBeanName, e);
        }
    }

    /**
     * 根据包名获取数据源配置
     */
    public DataSourceConfig getDataSourceConfigForPackage(String packageName) {
        // 精确匹配
        for (Map.Entry<String, DataSourceConfig> entry : packageToDataSourceMap.entrySet()) {
            if (packageName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 如果没有找到匹配的，返回默认配置
        if (defaultDataSourceConfig != null) {
            return defaultDataSourceConfig;
        }
        
        // 最后的fallback：使用应用上下文中的默认Bean
        try {
            R2dbcEntityTemplate entityTemplate = applicationContext.getBean(R2dbcEntityTemplate.class);
            DatabaseClient databaseClient = applicationContext.getBean(DatabaseClient.class);
            return new DataSourceConfig(entityTemplate, databaseClient);
        } catch (Exception e) {
            throw new RuntimeException("No data source configuration found for package: " + packageName, e);
        }
    }

    /**
     * 数据源配置类
     */
    public static class DataSourceConfig {
        private final R2dbcEntityTemplate entityTemplate;
        private final DatabaseClient databaseClient;

        public DataSourceConfig(R2dbcEntityTemplate entityTemplate, DatabaseClient databaseClient) {
            this.entityTemplate = entityTemplate;
            this.databaseClient = databaseClient;
        }

        public R2dbcEntityTemplate getEntityTemplate() {
            return entityTemplate;
        }

        public DatabaseClient getDatabaseClient() {
            return databaseClient;
        }
    }
}