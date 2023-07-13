package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.worktask.repository.AuthorityRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description: 任务日志
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Getter
@Setter
@Entity
@Table(name = "SR_TASK_LOG")
public class WorkTaskLog extends StringIdEntity {

    @Id
    @Column(name = "TASK_LOG_ID")
    private String id;

    @ManyToOne()
    @JoinColumn(name="TASK_ID")
    private WorkTask workTask;


    @Column(name = "WORKROOM_ID")
    private String projectId;

    @Column(name = "OPER_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date operTime;

    //操作类型（1代表指派，2代表接受指派，3代表拒绝指派，4代表提交成果，5代表审核通过，6代表审核不通过，7代表修改，8代表删除，9代表催办 10延期）
    @Column(name = "OPER_TYPE")
    private String operType;

    @Column(name = "OPER_CONTENT")
    private String operContent;


    @Column(name = "BSFLAG")
    private String bsflag;

    @Column(name = "USER_ID")
    private String userId;

//    @Transient
//    private String operUserName;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    public String getOperUserName(){
        if (!StringUtils.isEmpty(getUserId())) {
            String userDisplayName = SpringManager.getBean(AuthorityRepository.class).getUserDisplayName(getUserId());
            return userDisplayName;
        }else{
            return "";
        }

    }


}
