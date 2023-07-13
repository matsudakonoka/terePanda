package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.worktask.repository.AuthorityRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description: 任务人员分配
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */

@Getter
@Setter
@Entity
@Table(name = "SR_TASK_ASSIGN")
public class WorkTaskAssign extends StringIdEntity {


    @Id
    @Column(name = "TASK_ASSIGN_ID")
    private String id;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name="TASK_ID")
    private WorkTask workTask;

    @Column(name = "WORKROOM_ID")
    private String projectId;

//    @Column(name = "IS_MANAGER")
//    private String isManager;

    @Column(name = "BSFLAG")
    private String bsflag;

    @Column(name = "USER_ID")
    private String userId;

    @Transient
    private String userName;

    public String getUserName(){
        return SpringManager.getBean(AuthorityRepository.class).getUserDisplayName(getUserId());
    }

    //空：未响应；Y：已接受；N：已拒绝
    @Column(name = "TASK_RESPONSE")
    private String taskResponse;


    @Column(name = "CREATE_USER")
    private String createUser;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "REMARKS")
    private String remarks;
}
