package com.cnpc.epai.core.workscene.pojo.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkMulitVo {

    @ApiModelProperty("研究对象ID")
    private String objectId;

    @ApiModelProperty("研究对象名称")
    private String objectName;

    @ApiModelProperty("研究对象类型")
    private String objectType;

    @ApiModelProperty("场景模板ID")
    private String templateId;

    @ApiModelProperty("负责人")
    private List<JSONObject>  chargeUsers;

    @ApiModelProperty("权限用户")
    private List<JSONObject> usersAuth;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date endTime;

}
