package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 二期任务人员分配
 */
@Getter
@Setter
public class SrTaskAssign {
    private String taskAssignId;

    private String userId;

    private String workroomId;

    private String taskId;

    private String isManager;

    private String taskResponse;

    private String bsflag;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;

}