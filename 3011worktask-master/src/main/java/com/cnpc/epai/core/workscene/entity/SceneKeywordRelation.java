package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.io.Serializable;

/**
 * (SrSceneKeywordRelation)实体类
 *
 * @author makejava
 * @since 2021-11-19 17:28:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "keywordRelation",description = "关键字关联信息")
@TableName("sr_scene_keyword_relation")
@Accessors(chain = true)
public class SceneKeywordRelation implements Serializable {
    private static final long serialVersionUID = -60032041688945701L;

    @ApiModelProperty("关联id")
    @TableId(value = "relation_id",type = IdType.UUID)
    private String relationId;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("应用id")
    private String applicationId;

    @ApiModelProperty("关键字id")
    private String keywordId;

    @ApiModelProperty("关键字名称")
    private String keywordName;

    @ApiModelProperty("排序号")
    private Long sortNum;
    /**
     * 1表示在用，0表示删除
     */
    @ApiModelProperty(value ="逻辑删除",hidden = true)
    private String bsflag;

    @ApiModelProperty("备注")
    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value="创建人",hidden = true)
    private String createUser;

    @ApiModelProperty(value="创建时间",hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @ApiModelProperty(value="更新人",hidden = true)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @ApiModelProperty(value="更新时间",hidden = true)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;



}

