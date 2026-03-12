package com.scrm.r2dbc.plus.fill;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 默认元对象字段填充处理器
 * 只处理通用的时间字段填充
 *
 * @author dason
 */
@Component
public class DefaultMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        // 使用严格填充方法，确保类型安全和注解验证
        // 填充创建时间
        this.strictInsertFill(metaObject, "gmtCreate", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);

        // 填充更新时间
        this.strictInsertFill(metaObject, "gmtModified", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 兼容旧的方法调用（如果有字段没有正确的注解）
        this.setInsertFieldValByName("gmtCreate", now, metaObject);
        this.setInsertFieldValByName("createTime", now, metaObject);
        this.setInsertFieldValByName("gmtModified", now, metaObject);
        this.setInsertFieldValByName("updateTime", now, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        // 使用严格填充方法，确保类型安全和注解验证
        // 填充更新时间
        this.strictUpdateFill(metaObject, "gmtModified", LocalDateTime.class, now);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 兼容旧的方法调用（如果有字段没有正确的注解）
        this.setUpdateFieldValByName("gmtModified", now, metaObject);
        this.setUpdateFieldValByName("updateTime", now, metaObject);
    }
}
