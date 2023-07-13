package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 任务专业软件
 */
@Getter
@Setter
public class SrProjectTaskSoftware {
    private String taskSoftwareId;

    private String workroomId;

    private String taskId;

    private String softwareId;

    private String softwareTypeId;

    private Long sortSequence;

    private String bsflag;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;

}