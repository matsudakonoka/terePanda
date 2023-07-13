package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 王淼
 * @Title: A6
 * @Package com.cnpc.epai.core.worktask.domain
 * @Description: 功能描述
 * @date 19:48 2018/7/9
 * {修改记录：修改人、修改时间、修改内容等}
 */
@Getter
@Setter
@Entity
@Table(name = "SR_PROJECT_TASK_SOFTWARE")
public class WorkTaskSoftware extends StringIdEntity {
    @Id
    @Column(name = "TASK_SOFTWARE_ID")
    private String id;

    @Column(name = "WORKROOM_ID")
    private String projectId;

    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "SOFTWARE_ID")
    private String softwareId;

    @Column(name = "SOFTWARE_TYPE_ID")
    private String softwareTypeId;


    @Column(name="SORT_SEQUENCE")
    private Integer showOrder;

    @Column(name="BSFLAG")

    private String bsflag;
    @Column(name = "REMARKS")
    private String remarks;

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
}
