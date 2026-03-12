package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.annotation.Mapper;
import com.scrm.r2dbc.plus.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * R2dbc-Plus Mapper 扫描配置器
 * 用于扫描 @Mapper 注解的接口并注册为 Spring Bean
 * 
 * @author dason
 */
@Component
public class R2dbcPlusMapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
    private String[] basePackages;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 获取扫描包配置
        initBasePackages();

        // 扫描 @Mapper 注解的接口
        Set<Class<?>> mapperInterfaces = scanMapperInterfaces();

        // 为每个 Mapper 接口注册 Bean 定义
        for (Class<?> mapperInterface : mapperInterfaces) {
            registerMapperBean(registry, mapperInterface);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 不需要实现
    }

    /**
     * 初始化扫描包配置
     */
    private void initBasePackages() {
        Set<String> allBasePackages = new HashSet<>();
        
        // 从应用上下文中查找所有 @MapperScan 注解
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MapperScan.class);
        if (beanNames.length > 0) {
            for (String beanName : beanNames) {
            try {
                    Object bean = applicationContext.getBean(beanName);
                MapperScan mapperScan = bean.getClass().getAnnotation(MapperScan.class);
                if (mapperScan != null && mapperScan.basePackages().length > 0) {
                        // 收集所有的basePackages
                        for (String basePackage : mapperScan.basePackages()) {
                            allBasePackages.add(basePackage);
                        }
                }
            } catch (Exception e) {
                    // 忽略错误，继续处理下一个
                }
            }
        }

        // 如果收集到了basePackages，使用收集到的；否则使用默认配置
        if (!allBasePackages.isEmpty()) {
            this.basePackages = allBasePackages.toArray(new String[0]);
        }
    }

    /**
     * 扫描 @Mapper 注解的接口
     */
    private Set<Class<?>> scanMapperInterfaces() {
        Set<Class<?>> mapperInterfaces = new HashSet<>();

        try {
            // 扫描配置的包下的所有类
            for (String basePackage : basePackages) {
                String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        String className = metadataReader.getClassMetadata().getClassName();

                        try {
                            Class<?> clazz = Class.forName(className);

                            // 检查是否为接口且有 @Mapper 注解
                            if (clazz.isInterface() && clazz.isAnnotationPresent(Mapper.class)) {
                                mapperInterfaces.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            // 忽略无法加载的类
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan mapper interfaces", e);
        }

        return mapperInterfaces;
    }

    /**
     * 为 Mapper 接口注册 Bean 定义
     */
    private void registerMapperBean(BeanDefinitionRegistry registry, Class<?> mapperInterface) {
        String beanName = getBeanName(mapperInterface);
        
        // 创建 Bean 定义
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(R2dbcPlusMapperFactoryBean.class);
        builder.addConstructorArgValue(mapperInterface);
        builder.setAutowireMode(2); // AUTOWIRE_BY_TYPE
        
        // 注册 Bean 定义
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    /**
     * 获取 Bean 名称
     */
    private String getBeanName(Class<?> mapperInterface) {
        String className = mapperInterface.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
