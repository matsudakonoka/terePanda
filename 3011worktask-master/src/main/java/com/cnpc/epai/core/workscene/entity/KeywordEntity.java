package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liuTao
 * @version 1.0
 * @name KeywordEntity
 * @description
 * @date 2021/10/14 10:11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "keyword",description = "关键字")
@TableName("sr_scene_keyword_re")
@Accessors(chain = true)
public class KeywordEntity implements Serializable {
    @ApiModelProperty("关键字id")
    @TableId(value = "keyword_id",type = IdType.UUID)
    private String keywordId;

    @ApiModelProperty("关键字名称")
    private String keywordName;

    @ApiModelProperty("关键字类型id")
    private String typeId;

    @ApiModelProperty("关键字状态")
    private String keywordStatus;

    @ApiModelProperty("关键字排序号")
    private Integer keywordSortNum;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("使用次数")
    private Integer usageCount;

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


}
