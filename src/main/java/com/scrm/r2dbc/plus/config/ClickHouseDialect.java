package com.scrm.r2dbc.plus.config;

import org.springframework.data.r2dbc.dialect.PostgresDialect;

/**
 * Spring Data R2DBC 默认只内置了 H2、MariaDB、MySQL、PostgreSQL、MSSQL、Oracle 这 6 种 R2dbcDialect 实现
 * ClickHouse 方言，先复用 Postgres 的 SQL 映射。
 */
public class ClickHouseDialect extends PostgresDialect {
}