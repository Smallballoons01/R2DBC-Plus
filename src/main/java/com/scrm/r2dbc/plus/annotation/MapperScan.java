package com.scrm.r2dbc.plus.annotation;

import com.scrm.r2dbc.plus.config.R2dbcPlusAutoConfiguration;
import com.scrm.r2dbc.plus.config.R2dbcPlusMapperScannerConfigurer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 MapperScan 注解
 *
 * @author dason
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({R2dbcPlusAutoConfiguration.class, R2dbcPlusMapperScannerConfigurer.class})
public @interface MapperScan {
    
    /**
     * Mapper 扫描包路径
     */
    String[] basePackages() default {};
    
    /**
     * XML 映射文件位置模式
     */
    String[] mapperLocations() default {"classpath*:mapper/**/*.xml"};
}
