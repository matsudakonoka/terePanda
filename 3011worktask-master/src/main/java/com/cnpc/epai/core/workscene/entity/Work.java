package com.cnpc.epai.core.workscene.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sr_work_msg", autoResultMap = true)
public class Work {

    @TableId(value = "work_id", type = IdType.ASSIGN_ID)
    private String workId;
    @ApiModelProperty("工作名称")
    @TableField("work_name")
    private String workName;
    @ApiModelProperty("实例ID")
    @TableField("instance_Id")
    private String instanceId;
    @ApiModelProperty("模板ID")
    @TableField("template_Id")
    private String templateId;
    @ApiModelProperty("模板名称")
    @TableField("template_Name")
    private String templateName;
    @ApiModelProperty("对象类型")
    @TableField("geo_Type")
    private String geoType;
    @ApiModelProperty("负责人")
    @TableField("charge_User")
    private String chargeUser;

    // @JSONField(format = "yyyy-MM-dd")
    private Date startTime;
    // @JSONField(format = "yyyy-MM-dd")
    private Date endTime;
    private String remarks;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject workShare;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private String createUser;

    @TableField(value = "create_date", fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(value = "update_date", fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    private boolean reportOutput;
    private String reportTemplateId;
    private String reportTemplateName;
}
