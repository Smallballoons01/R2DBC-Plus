package com.scrm.r2dbc.plus.test.entity;

import com.scrm.r2dbc.plus.annotation.TableId;
import com.scrm.r2dbc.plus.annotation.TableName;
import com.scrm.r2dbc.plus.annotation.Version;
import com.scrm.r2dbc.plus.enums.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 在线客服账号实体类
 *
 * @author dason
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Data
@TableName("test_account")
public class TestAccount extends TestBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id，自动增长
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 用户uuid
     */
    private String uuid;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否启用  0 启用  1 禁用
     */
    private Integer isOpen;

    /**
     * 国家
     */
    private String country;

    /**
     * 版本字段
     */
    @Version
    private Integer version;
}
