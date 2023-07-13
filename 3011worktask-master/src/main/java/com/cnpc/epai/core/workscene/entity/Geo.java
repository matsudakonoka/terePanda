package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sr_work_geo")
public class Geo {
    @TableId(value = "geo_id", type = IdType.ASSIGN_ID)
    private String geoId;
    @ApiModelProperty("工作ID")
    @TableField("work_id")
    private String workId;
    @ApiModelProperty("数据对象ID")
    @TableField("geo_obj_id")
    private String geoObjId;
    @ApiModelProperty("数据对象名称")
    @TableField("geo_obj_name")
    private String geoObjName;
    @ApiModelProperty("数据对象类型")
    @TableField("geo_type")
    private String geoType;

    private String source;
    private String childObjects;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private String createUser;

    @TableField(value = "create_date", fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(value = "update_date", fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

}
