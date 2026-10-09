package com.scrm.r2dbc.plus.config;

import com.scrm.r2dbc.plus.annotation.Mapper;
import com.scrm.r2dbc.plus.annotation.MapperScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * R2DBC-Plus Mapper 扫描配置器
 *
 * <p>扫描带 {@link Mapper} 注解的接口并注册为 Spring Bean。
 *
 * <p><b>注意</b>：本类实现 {@link BeanDefinitionRegistryPostProcessor}，作为 starter 组件
 * 不应通过 {@code @Component} 全局生效（会强制实例化整个应用上下文），因此改由
 * {@link R2dbcPlusAutoConfiguration} 通过 {@code @Import} 精确引入。
 *
 * @author dason
 */
@Slf4j
public class R2dbcPlusMapperScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
    private String[] basePackages = new String[0];

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        initBasePackages();

        if (basePackages.length == 0) {
            // 这是用户最常踩的坑：不加 @MapperScan 会导致注入 Mapper 时报
            // NoSuchBeanDefinitionException，但根因在这里。用 WARN 明示，
            // 否则只有 debug 级别可见，极难排查。
            log.warn("""

                    ======================================================================
                    R2DBC-Plus 未扫描到任何 Mapper：未检测到 @MapperScan 注解。
                    如果你的 Mapper 接口标注了 @Mapper，请确保启动类上有：
                        @MapperScan("com.example.mapper")
                    或者检查 basePackages 是否指向了正确的包路径。
                    ======================================================================
                    """);
            return;
        }

        Set<Class<?>> mapperInterfaces = scanMapperInterfaces();
        for (Class<?> mapperInterface : mapperInterfaces) {
            registerMapperBean(registry, mapperInterface);
        }

        if (mapperInterfaces.isEmpty()) {
            log.warn("R2DBC-Plus 在包 {} 下未找到带 @Mapper 注解的接口。"
                            + "请确认 Mapper 接口已标注 @Mapper 且继承了 BaseMapper。",
                    basePackages.length == 1 ? basePackages[0] : String.join(", ", basePackages));
            return;
        }

        log.info("R2DBC-Plus Mapper 扫描完成，共注册 {} 个 Mapper: {}", mapperInterfaces.size(),
                mapperInterfaces.stream().map(Class::getSimpleName).toList());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 无需处理
    }

    /**
     * 初始化扫描包配置
     */
    private void initBasePackages() {
        Set<String> allBasePackages = new LinkedHashSet<>();

        if (applicationContext != null) {
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(MapperScan.class);
            for (String beanName : beanNames) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    MapperScan mapperScan =
                            org.springframework.core.annotation.AnnotationUtils.findAnnotation(
                                    bean.getClass(), MapperScan.class);
                    if (mapperScan != null) {
                        allBasePackages.addAll(Set.of(mapperScan.basePackages()));
                    }
                } catch (Exception e) {
                    log.warn("读取 @MapperScan 配置失败，bean={}: {}", beanName, e.getMessage());
                }
            }
        }

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
            for (String basePackage : basePackages) {
                if (basePackage == null || basePackage.isBlank()) {
                    continue;
                }
                String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

                for (Resource resource : resources) {
                    if (!resource.isReadable()) {
                        continue;
                    }
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    try {
                        Class<?> clazz = Class.forName(className, false,
                                Thread.currentThread().getContextClassLoader());
                        if (clazz.isInterface() && clazz.isAnnotationPresent(Mapper.class)) {
                            mapperInterfaces.add(clazz);
                        }
                    } catch (ClassNotFoundException | LinkageError e) {
                        // 忽略无法加载的类
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan mapper interfaces", e);
        }

        return mapperInterfaces;
    }

    /**
     * 为 Mapper 接口注册 Bean 定义
     */
    private void registerMapperBean(BeanDefinitionRegistry registry, Class<?> mapperInterface) {
        String beanName = getBeanName(mapperInterface);
        if (registry.containsBeanDefinition(beanName)) {
            log.warn("Mapper Bean 已存在，跳过注册: {}", beanName);
            return;
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(R2dbcPlusMapperFactoryBean.class);
        builder.addConstructorArgValue(mapperInterface);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        // Mapper 代理需要尽早初始化，避免持有半初始化的 ApplicationContext
        beanDefinition.setLazyInit(false);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 获取 Bean 名称
     *
     * <p>对全大写缩写类名（如 {@code XMLUserMapper}）做 Introspector 风格的解缩，
     * 避免生成 {@code xMLUserMapper} 这类不直观名称。
     */
    private String getBeanName(Class<?> mapperInterface) {
        String simpleName = mapperInterface.getSimpleName();
        String decapitalized = java.beans.Introspector.decapitalize(simpleName);
        if (!simpleName.equals(decapitalized)) {
            return decapitalized;
        }
        // Introspector 对全大写缩写原样返回，此时手动小写首字母更符合直觉
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}