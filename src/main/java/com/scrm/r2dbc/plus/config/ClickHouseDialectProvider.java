package com.scrm.r2dbc.plus.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.dialect.DialectResolver.R2dbcDialectProvider;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.util.Optional;

public class ClickHouseDialectProvider implements R2dbcDialectProvider {

    @Override
    public Optional<R2dbcDialect> getDialect(ConnectionFactory factory) {
        return Optional.of(factory)
                .filter(f -> f.getMetadata().getName().toLowerCase().contains("clickhouse"))
                .map(f -> new ClickHouseDialect());
    }
}