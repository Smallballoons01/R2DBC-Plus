package com.scrm.r2dbc.plus.test.config;

import com.scrm.r2dbc.plus.annotation.MapperScan;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 测试配置类
 *
 * <p><b>默认使用 H2 内存库</b>，使测试零外部依赖，可在 CI 与本地开箱运行。
 * 如需切到真实 MySQL，激活 {@code mysql} profile 并通过环境变量提供连接信息：
 *
 * <pre>
 * export R2DBC_TEST_URL=r2dbc:mysql://localhost:3306/test_db
 * export R2DBC_TEST_USER=root
 * export R2DBC_TEST_PASSWORD=secret
 * </pre>
 *
 * @author dason
 */
@MapperScan(basePackages = "com.scrm.r2dbc.plus.test.mapper")
@ComponentScan(basePackages = "com.scrm.r2dbc.plus.test")
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableTransactionManagement
public class TestConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TestConfiguration.class);

    /**
     * 是否使用真实 MySQL（由 {@code r2dbc-plus.test.mysql-enabled} 控制）
     */
    @Value("${r2dbc-plus.test.mysql-enabled:false}")
    private boolean mysqlEnabled;

    @Bean
    public ConnectionFactory connectionFactory(Environment env) {
        if (mysqlEnabled) {
            String url = requireEnv(env, "R2DBC_TEST_URL", "spring.r2dbc.url");
            String user = requireEnv(env, "R2DBC_TEST_USER", "spring.r2dbc.username");
            String password = requireEnv(env, "R2DBC_TEST_PASSWORD", "spring.r2dbc.password");
            log.info("测试使用真实 MySQL: {}", url);

            MySqlConnectionConfiguration cfg = parseMysqlUrl(url)
                    .username(user)
                    .password(password)
                    .build();
            return MySqlConnectionFactory.from(cfg);
        }

        log.info("测试使用 H2 内存库");
        H2ConnectionConfiguration config = H2ConnectionConfiguration.builder()
                // DB_CLOSE_DELAY=-1 保证内存库在连接关闭后不被销毁，多个测试类可复用
                .inMemory("r2dbcplus;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL")
                .username("sa")
                .password("")
                .build();
        return new H2ConnectionFactory(config);
    }

    /**
     * 解析 {@code r2dbc:mysql://host:port/database} 形式的 URL。
     * r2dbc-mysql 1.4.x 的 Builder 不接受整串 url，只能按字段装配。
     */
    private MySqlConnectionConfiguration.Builder parseMysqlUrl(String url) {
        // 形如 r2dbc:mysql://user:pass@host:3306/db?k=v
        String remain = url.substring(url.indexOf("://") + 3);
        String query = "";
        int q = remain.indexOf('?');
        if (q >= 0) {
            query = remain.substring(q + 1);
            remain = remain.substring(0, q);
        }
        String database = "";
        int slash = remain.lastIndexOf('/');
        if (slash >= 0) {
            database = remain.substring(slash + 1);
            remain = remain.substring(0, slash);
        }
        String host = remain;
        int port = 3306;
        int colon = remain.lastIndexOf(':');
        if (colon >= 0) {
            host = remain.substring(0, colon);
            port = Integer.parseInt(remain.substring(colon + 1));
        }
        if (host.isBlank()) {
            throw new IllegalArgumentException("无法从 r2dbc url 中解析主机名: " + url);
        }

        MySqlConnectionConfiguration.Builder builder = MySqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .createDatabaseIfNotExist(true);
        if (!database.isBlank()) {
            builder = builder.database(database);
        }
        for (String kv : query.split("&")) {
            if (kv.isBlank()) {
                continue;
            }
            String[] parts = kv.split("=", 2);
            String key = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            switch (key) {
                case "serverZoneId" -> builder = builder.serverZoneId(java.time.ZoneId.of(value));
                case "connectionTimeZone" -> builder = builder.connectionTimeZone(value);
                case "sslMode" -> builder = builder.sslMode(
                        io.asyncer.r2dbc.mysql.constant.SslMode.valueOf(value.toUpperCase()));
                case "zeroDateOption" -> builder = builder.zeroDateOption(
                        io.asyncer.r2dbc.mysql.constant.ZeroDateOption.valueOf(value.toUpperCase()));
                default -> log.debug("忽略未识别的 r2dbc url 参数: {}", key);
            }
        }
        return builder;
    }

    /**
     * 在 H2 上建表并灌入测试数据。真实 MySQL 下由外部准备好数据，跳过。
     */
    @Bean
    public CommandLineRunner h2SchemaInitializer(ConnectionFactory connectionFactory) {
        return args -> {
            if (mysqlEnabled) {
                return;
            }
            String sql = new String(
                    new ClassPathResource("sql/h2-schema.sql").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            // H2 内存库串行执行 DDL/DML 即可，用一个长连接跑完全部语句
            Mono.usingWhen(
                            Mono.from(ConnectionFactoryUtils.getConnection(connectionFactory)),
                            conn -> Flux.fromIterable(execStatements(sql))
                                    .concatMap(stmt -> Flux.from(conn.createStatement(stmt).execute())
                                            .flatMap(Result::getRowsUpdated)
                                            .reduce(0L, (a, b) -> a + b))
                                    .reduce(0L, (a, b) -> a + b)
                                    .map(rows -> rows),
                            Connection::close)
                    .doOnError(e -> log.error("H2 初始化失败: {}", e.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .block();
            log.info("H2 测试表结构与数据初始化完成");
        };
    }

    /**
     * 拆分 SQL 脚本为可执行语句列表（去掉整行注释与空行）
     */
    private List<String> execStatements(String sql) {
        return Arrays.stream(sql.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.lines().allMatch(line -> line.trim().startsWith("--")))
                .toList();
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    private String requireEnv(Environment env,
                              String envKey, String propertyKey) {
        String value = System.getenv(envKey);
        if (value == null || value.isBlank()) {
            value = env.getProperty(propertyKey);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "已启用 MySQL 测试模式但缺少连接配置，请设置环境变量 " + envKey
                            + " 或配置项 " + propertyKey);
        }
        return value;
    }
}