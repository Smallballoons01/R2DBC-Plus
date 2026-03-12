package com.scrm.r2dbc.plus.test.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scrm.r2dbc.plus.annotation.TableLogic;
import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.enums.FieldFill;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TestBaseEntity {

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDel;

    /**
     * 创建时间，默认为当前时间戳，不为空
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    /**
     * 更新时间，默认为当前时间戳，不为空，并在每次更新时自动更新
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModified;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 排序字段
     */
    private Long sortNum;
}
