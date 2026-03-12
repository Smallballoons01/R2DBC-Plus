package com.scrm.r2dbc.plus.test.config;

import com.scrm.r2dbc.plus.fill.MetaObject;
import com.scrm.r2dbc.plus.fill.MetaObjectHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author dason
 * @since 2025-08-04
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

    }
}
