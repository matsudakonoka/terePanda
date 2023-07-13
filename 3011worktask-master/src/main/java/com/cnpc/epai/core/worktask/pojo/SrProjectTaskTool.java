package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 二期任务常用工具
 */
@Getter
@Setter
public class SrProjectTaskTool {
    private String taskToolId;

    private String workroomId;

    private String taskId;

    private String toolId;

    private Long sortSequence;

    private String bsflag;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;
}