package com.cnpc.epai.core.worktask.domain;


import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.repository.DataSetRepository;
import com.cnpc.epai.core.worktask.repository.DatasetTreeRepository;
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
@Table(name = "SR_TASK_MGR")
public class WorkTask extends StringIdEntity {

    @Id
    @Column(name = "TASK_ID")
    private String id;

    @Column(name = "WORKROOM_ID")
    private String projectId;

    @Transient
    private String projectName;

    @Column(name = "DATASET_ID")
    private String datasetId;

    //任务名称
    @Column(name = "TASK_NAME")
    private String taskName;

    //任务明细的当前状态（1代表未响应，2代表进行中，3代表拒绝，4代表已提交，5代表已完成，6代表已终止,7代表审核未通过）
    @Column(name = "CURRENT_STATE")
    private String currentState;

    //工作类型（0代表独立，1代表协同）
    @Column(name = "WORK_TYPE")
    private String workType;

    //任务类型,成果任务:RESULT;场景任务:SCENE
    @Column(name = "TASK_TYPE")
    private String taskType;

    //删除标识
    @Column(name = "BSFLAG")
    private String bsflag;

    //删除日期
    @Column(name = "DELETE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleteDate;

    //任务开始时间
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    //任务结束时间
    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    @Column(name = "PLAN_ID")
    private String planId;

    //需要补充说明的内容
    @Column(name = "REMARKS")
    private String remarks;

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

    //成果数量
    @Column(name = "ACHIEVEMENTS_NUMBER")
    private Integer achievementsNumber;

    @Transient
    private Map<String, Object> attribute;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="workroom_id",referencedColumnName="workroom_id",insertable = false,updatable = false)
    private Project project;


    @OneToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "workTask")
    private List<WorkTaskAssign> workTaskAssigns;

    /**
     * 一期数据名字
     * @return
     */
    public String getDataSetName() {
        if (!StringUtils.isEmpty(getDatasetId())) {
            SrMetaDataset dto = SpringManager.getBean(DataSetRepository.class).findOne(getDatasetId());
            if (dto == null) {
                return "";
            }
            return dto.getName();
        } else {
            return "";
        }

    }

    /**
     * 二期数据集名字
     * @return
     */
    public String getDatasetName() {
        if (!StringUtils.isEmpty(getDatasetId())) {
            String datasetName = SpringManager.getBean(DatasetTreeRepository.class).getDatasetName(getDatasetId());
            return datasetName;
        } else {
            return "";
        }

    }

}
