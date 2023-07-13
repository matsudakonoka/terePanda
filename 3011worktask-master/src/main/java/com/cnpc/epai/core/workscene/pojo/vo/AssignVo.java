package com.cnpc.epai.core.workscene.pojo.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AssignVo {
    @ApiModelProperty("业务节点ID")
    private String treeNodeId;
    @ApiModelProperty("业务节点名称")
    private String treeNodeName;
    private ChargeUserVo[] chargeUser;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date endTime;
    @ApiModelProperty("建议")
    private String recommend;
}
