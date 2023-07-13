package com.cnpc.epai.core.worktask.domain;


import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.repository.AuthorityRepository;
import com.cnpc.epai.core.worktask.repository.DataSetRepository;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author 王淼
 * @version 1.0.0
 * @Description: 任务实体
 * @date 2018/4/20
 */
@Getter
@Setter
@Entity
@Table(name = "SR_PROJECT_PLAN")
public class ProjectPlan extends StringIdEntity {

    @Id
    @Column(name = "PLAN_ID")
    private String id;

    @ApiModelProperty("工作室ID")
    @Column(name = "WORKROOM_ID")
    private String projectId;
    
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    @ApiModelProperty("起始日期")
    private Date startDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    @ApiModelProperty("截止日期")
    private Date endDate;

    @Column(name = "WORK_CONTENT")
    @ApiModelProperty("研究内容及工作安排")
    private String workContent;

    @Column(name = "PHASE_TERGET")
    @ApiModelProperty("阶段目标")
    private String phaseTerget;

    @Column(name = "RESPONSIBLE_USER")
    @ApiModelProperty("负责人")
    private String responsibleUser;
    
    @Transient
    @ApiModelProperty("负责人名称")
    private String responsibleUserName;
    
    public String getResponsibleUserName(){
        return SpringManager.getBean(AuthorityRepository.class).getUserDisplayName(getResponsibleUser());
    }

    @Column(name = "SORT_SEQUENCE")
    @ApiModelProperty("排序")
    private Integer sortSequence;

    @Column(name = "PLAN_WEIGHTINGS")
    @ApiModelProperty("比重")
    private Integer planWeightings;

    @Column(name = "PLAN_PROGRESS")
    @ApiModelProperty("进度")
    private Integer planProgress;

    @Column(name = "STATE")
    @ApiModelProperty("状态 1未应用2应用")
    private Integer state;

    @Column(name = "REMARKS")
    private String remarks;
    
    @Column(name = "BSFLAG")
    private String bsflag;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Transient
    private String createUserName;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
    

    @PrePersist
    protected void onBeforeCreate(){
        this.createDate = new Date();
        this.createUser = SpringManager.getCurrentUser().getUserId();

        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();

        if (StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
    }

    @PreUpdate
    protected void onBeforeUpdate(){

        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();

        if (StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
    }
}
