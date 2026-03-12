package com.scrm.r2dbc.plus.test.config;

import com.scrm.r2dbc.plus.fill.DefaultMetaObjectHandler;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.generator.DefaultIdGenerator;
import com.scrm.r2dbc.plus.generator.IdGenerator;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.version.VersionProcessor;
import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;

/**
 * 简单测试配置类
 * 只配置XML解析相关的Bean，不需要数据库连接
 * 
 * @author dason
 */
@SpringBootConfiguration
public class SimpleTestConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator() {
        return new DefaultIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public FieldFillProcessor fieldFillProcessor() {
        return new FieldFillProcessor();
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
        String[] mapperLocations = {"classpath*:mapper/**/*.xml"};
        for (String location : mapperLocations) {
            try {
                XmlMapperParser.parseMapperXmls(location);
                System.out.println("成功解析XML映射文件: " + location);
            } catch (Exception e) {
                System.out.println("解析XML映射文件失败: " + location + ", 错误: " + e.getMessage());
            }
        }
    }
}
