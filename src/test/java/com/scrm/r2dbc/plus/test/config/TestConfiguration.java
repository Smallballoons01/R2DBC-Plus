package com.scrm.r2dbc.plus.test.config;

import com.scrm.r2dbc.plus.annotation.MapperScan;
import com.scrm.r2dbc.plus.config.R2dbcPlusAutoConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * 测试配置类
 * 用于R2dbc-Plus模块的单元测试
 *
 * @author dason
 */
@MapperScan(basePackages = "com.scrm.r2dbc.plus.test.mapper")
@ComponentScan(basePackages = "com.scrm")
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableTransactionManagement
@Import(R2dbcPlusAutoConfiguration.class)
public class TestConfiguration {

    @Bean
    public ConnectionFactory connectionFactory() {
        // 使用MySQL数据库进行测试
        return MySqlConnectionFactory.from(
            MySqlConnectionConfiguration.builder()
                .host(env.getProperty("R2DBC_TEST_HOST"))
                .port(Integer.parseInt(env.getProperty("R2DBC_TEST_PORT", "3306")))
                .username(env.getProperty("R2DBC_TEST_USER"))
                .password(env.getProperty("R2DBC_TEST_PASSWORD"))
                .database(env.getProperty("R2DBC_TEST_DB"))
                .build()
        );
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }
}
