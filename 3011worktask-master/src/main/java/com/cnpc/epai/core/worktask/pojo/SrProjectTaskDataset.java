package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *     二期数据集关联表
 */
@Getter
@Setter
public class SrProjectTaskDataset {
    //任务数据集id
    private String taskDatasetId;
// 工作室id
    private String workroomId;
//任务id
    private String taskId;
//数据集id
    private String datasetId;
//排序字段
    private Long sortSequence;

    private String bsflag;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;
//节点id
    private String businessId;
}