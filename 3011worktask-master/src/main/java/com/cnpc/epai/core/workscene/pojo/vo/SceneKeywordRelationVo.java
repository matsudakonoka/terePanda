package com.cnpc.epai.core.workscene.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cnpc.epai.core.workscene.entity.SceneKeywordRelation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "keywordRelationVo",description = "关键字关联信息VO类")
@Accessors(chain = true)
public class SceneKeywordRelationVo implements Serializable {
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
    @ApiModelProperty("逻辑删除")
    private String bsflag;

    @ApiModelProperty("备注")
    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @ApiModelProperty("关键字类型名称")
    private String typeName;
}
