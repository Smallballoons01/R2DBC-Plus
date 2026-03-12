package com.scrm.r2dbc.plus.test.entity;

import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.annotation.TableId;
import com.scrm.r2dbc.plus.annotation.TableName;
import com.scrm.r2dbc.plus.annotation.Version;
import com.scrm.r2dbc.plus.enums.FieldFill;
import com.scrm.r2dbc.plus.enums.IdType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 测试用户实体类
 * 用于测试R2DBC Plus的各种功能
 */
@Data
@Accessors(chain = true)
@TableName("test_user")
public class TestUser {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，唯一
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间，自动填充
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 版本号，用于乐观锁
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 构造函数
     */
    public TestUser() {
        this.enabled = true;
        this.version = 0;
        this.deleted = false; // 对应逻辑删除值 "0"
    }

    /**
     * 便捷构造函数
     */
    public TestUser(String username, String email, Integer age) {
        this();
        this.username = username;
        this.email = email;
        this.age = age;
    }
}