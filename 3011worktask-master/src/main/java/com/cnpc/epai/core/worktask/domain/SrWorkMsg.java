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
 * @date 2021/9/7
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "SR_WORK_MSG")
public class SrWorkMsg extends StringIdEntity {
    @Id
    @Column(name = "WORK_ID")
    private String id;

    @Column(name = "WORK_NAME")
    private String workName;

    @Column(name = "INSTANCE_ID")
    private String instanceId;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "GEO_TYPE")
    private String geoType;

    @Column(name = "CHARGE_USER")
    private String chargeUser;

    @Column(name = "START_TIME")
    @Temporal(TemporalType.DATE)
    private Date startTime;

    @Column(name = "END_TIME")
    @Temporal(TemporalType.DATE)
    private Date endTime;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "WORK_SHARE")
    private String workShare;

    @Column(name = "BSFLAG")
    private String bsflag;

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

    @Column(name = "REPORT_OUTPUT")
    private boolean reportOutput;

    @Column(name = "REPORT_TEMPLATE_ID")
    private String reportTemplateId;

    @Column(name = "REPORT_TEMPLATE_NAME")
    private String reportTemplateName;
}
