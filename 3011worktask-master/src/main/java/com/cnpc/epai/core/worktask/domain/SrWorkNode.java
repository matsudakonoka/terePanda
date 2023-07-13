package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 王博
 * @version 1.0.0
 * @Description: 工作实体
 * @date 2021/9/20
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "SR_WORK_NODE")
public class SrWorkNode extends StringIdEntity {

    @Id
    @Column(name = "WORK_NODE_ID")
    private String id;

    @Column(name = "TREE_NODE_ID")
    private String treeNodeID;

    @Column(name = "WORK_ID")
    private String workId;

    @Column(name = "TREE_NODE_NAME")
    private String treeNodeName;

    @Column(name = "CHARGE_LATEST")
    private String chargeLatest;

    @Column(name = "BSFLAG")
    private String bsflag;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Transient
    private String userName;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "START_TIME")
    @Temporal(TemporalType.DATE)
    private Date startTime;

    @Column(name = "END_TIME")
    @Temporal(TemporalType.DATE)
    private Date endTime;

    @Column(name = "RECOMMEND")
    private String recommend;

    @Transient
    private String workProgress;
}
