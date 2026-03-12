package com.scrm.r2dbc.plus.test.entity;

import com.scrm.r2dbc.plus.annotation.*;
import com.scrm.r2dbc.plus.enums.FieldFill;
import com.scrm.r2dbc.plus.enums.IdType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简洁的 r2dbc-plus 示例实体类
 * 演示各种注解的正确使用方式
 * 
 * @author dason
 */
@Data
@Accessors(chain = true)
@TableName("clean_example")
public class CleanExampleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，使用雪花算法自动生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间，插入时自动填充
     */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(value = "gmt_modified", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;

    /**
     * 逻辑删除字段，0=未删除，1=已删除
     */
    @TableLogic(value = "0", delval = "1")
    @TableField("is_del")
    private Integer isDel;

    /**
     * 版本号，用于乐观锁
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 扩展字段，不存储到数据库
     */
    @TableField(exist = false)
    private String extraInfo;
}
