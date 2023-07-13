package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 二期任务日志
 */
@Getter
@Setter
public class SrTaskLog {
    private String taskLogId;

    private String userId;

    private String workroomId;

    private String taskId;

    private Date operTime;

    private String operType;

    private String operContent;

    private String bsflag;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;

}