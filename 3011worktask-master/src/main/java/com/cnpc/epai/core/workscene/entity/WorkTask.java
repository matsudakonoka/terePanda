package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sr_work_task")
public class WorkTask {

    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private String taskId;
    private String taskName;
    private String workId;
    private String templateId;
    private String chargeUserId;
    private String chargeUserName;
    private String treeNodeIds;

    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

}
