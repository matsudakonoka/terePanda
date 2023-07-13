package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Builder
@Data
@TableName("sr_work_node")
public class WorkNode {

    @TableId(value = "work_node_id", type = IdType.ASSIGN_ID)
    private String workNodeId;
    private String workId;
    private String treeNodeId;
    private String treeNodeName;
    private String chargeLatest;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "YYYY-MM-dd")
    private Date startTime;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "YYYY-MM-dd")
    private Date endTime;

    private String recommend;

    private boolean start;

    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}
