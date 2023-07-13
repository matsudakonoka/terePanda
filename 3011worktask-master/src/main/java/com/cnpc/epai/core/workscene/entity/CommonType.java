package com.cnpc.epai.core.workscene.entity;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 *  @Name: CommonType
 *  @Description:
 *  @Version: V1.0.0
 *  @Author: 陈淑造
 *  @create 2021/10/14 13:03
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "commonType",description = "通用类型")
@TableName("sr_scene_common_type")
@Accessors(chain = true)
public class CommonType implements Serializable {

    @ApiModelProperty("类型Id")
    @TableId(value = "type_id",type = IdType.UUID)
    private String typeId;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("类型启用状态")
    private String typeStatus;

    @ApiModelProperty("排序号")
    private Integer typeSortNum;

    @ApiModelProperty("通用类型")
    private String commonType;

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
