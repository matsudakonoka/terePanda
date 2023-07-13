package com.cnpc.epai.core.worktask.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName sr_focuson_link
 */
@TableName(value ="sr_focuson_link")
@Data
public class SrFocusonLink implements Serializable {
    /**
     * 
     */
    @TableField(value = "link_id")
    @TableId(value = "link_id",type = IdType.UUID)
    private String linkId;

    /**
     * 研究对象
     */
    @TableField(value = "research_type")
    private String researchType;

    /**
     * 跳转地址
     */
    @TableField(value = "link")
    private String link;

    /**
     * 地址参数
     */
    @TableField(value = "link_param")
    private String linkParam;

    /**
     * 是否启用，1启用，0不启用
     */
    @TableField(value = "is_valid")
    private Integer isValid;

    /**
     * 
     */
    @TableField(value = "remarks")
    @ApiModelProperty(value = "备注",hidden = true)
    private String remarks;

    /**
     * 删除标识，1在用，-5删除
     */
    @ApiModelProperty(value = "删除标识,1表示再用，-5表示删除",hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private int bsflag;

    /**
     * 
     */
    @ApiModelProperty(value = "创建人",hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /**
     * 
     */
    @ApiModelProperty(value = "创建时间",hidden = true)
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8",pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy--MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 
     */
    @ApiModelProperty(value = "修改人",hidden = true)
    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    /**
     * 
     */
    @ApiModelProperty(value = "修改时间",hidden = true)
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(timezone = "GMT+8",pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy--MM-dd HH:mm:ss")
    private Date updateDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}