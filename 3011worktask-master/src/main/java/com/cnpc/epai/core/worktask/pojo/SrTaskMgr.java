package com.cnpc.epai.core.worktask.pojo;

import com.cnpc.epai.core.worktask.domain.Project;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 二期任务实体
 */
@Getter
@Setter
public class SrTaskMgr implements Serializable {
    private String taskId;

    private String workroomId;

    private String datasetId;

    private String taskName;

    private String currentState;

    private String workType;

    private Date startDate;

    private Date endDate;

    private String bsflag;

    private Date deleteDate;

    private String remarks;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;

    private Short achievementsNumber;

    private String taskType;

    private String planId;

    @Transient
    private Map<String, Object> attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="workroom_id",referencedColumnName="workroom_id",insertable = false,updatable = false)
    private Project project;


    @OneToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "taskId")
    private List<SrTaskAssign> taskAssign;


}