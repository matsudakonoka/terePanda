package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.Convert;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 项目
 * @author 王淼
 * @version 1.0.0
 * @date 2018/4/20
 */
@Getter
@Setter
@Entity
@Table(name = "SR_PROJECT_WORKROOM")
public class Project extends StringIdEntity {

    @PrePersist
    protected void prePersist() {
        if (StringUtils.isBlank(this.getId())) {
            this.setId(ShortUUID.randomUUID12());
        }
        this.createDate = new Date();
        this.createUser = SpringManager.getCurrentUser().getUserId();

        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
        if (org.springframework.util.StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }

        if (org.springframework.util.StringUtils.isEmpty(this.appScenario)) {
            this.appScenario = "workroom";
        }

    }

    //工作室ID
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORKROOM_ID")
    private String id;

    //项目模板ID
//    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "MODEL_ID")
//    private Template template;
    @Column(name = "MODEL_ID")
    private String templateId;

    //项目编码
    @Column(name = "PRJNO")
    private String code;

    //工作室名称
    @Column(name = "WORKROOM_NAME")
    private String name;

    //项目类型
    @Column(name = "ACTIVITY_TYPE")
    private String activityType;
    @Transient
    private String activityTypeName;

    //项目专业类别
    @Column(name = "ACTIVITY_MAJ_TYPE")
    private String activityMajType;
    @Transient
    private String activityMajTypeName;

    //项目级别
    @Column(name = "ACTIVITY_CLASS")
    private String activityClass;
    @Transient
    private String activityClassName;

    //项目目标
    @Column(name = "ACTIVITY_OBJECTIVE")
    private String activityObjective;

    //项目内容
    @Column(name = "DESCRIPTION")
    private String description;

    //油田代码
    @Column(name = "data_region")
    private String dataRegion;

    //项目年度
    @Column(name = "YEAR_MON")
    private int yearMon;

    //计划开始时间
    @Column(name = "PLAN_START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date planStartTime;

    //计划结束时间
    @Column(name = "PLAN_END_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date planEndTime;

    //排序
    @Column(name = "SORT_SEQUENCE")
    private Integer sortSequence;

    //工作区
//    @Column(name="BOUNDARY")
//    private String boundary;

    @Column(name = "REMARKS")
    private String remarks;

    //逻辑删除标识，表示该条记录在用或者已经失效，Y表示该记录已经失效，N表示在用，默认N
    @Column(name = "BSFLAG")
    private String bsflag;

    //项目经理
//    @Transient
//    private String projectManagerId;
//
//    @Transient
//    private String projectManagerName;

    //项目经理信息列表
    @Transient
    private List<Map<String, String>> managerInfo;




    @Column(name = "CREATE_USER", updatable = false)
    private String createUser;

    @Column(name = "CREATE_DATE", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "KEYWORDS")
    private String keywords;

    //项目进度
    @Column(name = "PROJECT_SCHEDULE")
    private int schedule;


    //业务阶段
    @Column(name = "BUSINESS_PHASE")
    private String businessPhase;
    @Transient
    private String businessPhaseName;

    //能源类型
    @Column(name = "ENERGY_TYPE")
    private String energyType;

    //授权油田
    @Column(name = "auth_regions")
    private String authRegions;

    //拷贝一份id
    @Column(name = "data_group")
    private String dataGroup;

    public void setId(String id) {
        this.id = id;
        setDataGroup(id);
    }


    //研究内容
    @Column(name = "research_contents")
    private String researchContent;

    //预期成果
    @Column(name = "expected_results")
    private String expectedResult;

    //考核内容
    @Column(name = "examination_contents")
    private String examContent;


    //是否存在项目进展记录
    @Column(name = "PROGRESS_COUNT")
    private long progressCount;

    //是否共享 Y/N
    @Column(name = "IS_SHARE")
    private String isShare;
    //是否数据隔离 0-不隔离/1-隔离
    @Column(name = "SEARCH_TYPE")
    private Integer searchType;


//    //当前项目下有多少个任务
//    @Column(name = "TASK_SUM")
////    @Transient
//    private Integer taskCount;
//
//    //当前项目下的成果总数
//    @Column(name = "RESULT_SUM")
////    @Transient
//    private Integer documentCount;

    @Transient
    //申请部门 申请人所在部门
    private String applyOrg;

    @Transient
    private List<Project> children;

    @Transient
    private String levelId;

    @Transient
    private String pLevelId;

    //应用场景 目前区分出项目工作室与随钻平台 workroom/app/personal
    @Column(name = "application_scenario")
    private String appScenario;


    //研究类型 自研 外协
    @Column(name = "research_type")
    @ApiModelProperty(value="研究类型 自研 外协")
    private String researchType;

    @Transient
    private String researchTypeName;

    //研究经费
    @Column(name = "total_funds")
    @ApiModelProperty(value="研究经费")
    private Float totalFunds;

    //外协总经费
    @Transient
    private Float outsouringTotalFunds;

    //关键技术
    @Column(name = "key_tech")
    @ApiModelProperty(value="关键技术")
    private String keyTech;
    //立项理由
    @Column(name = "project_reason")
    @ApiModelProperty(value="立项理由")
    private String reason;

    //项目（课题）类型
    @Column(name = "project_type")
    @ApiModelProperty(value="项目（课题）类型")
    private String projectType;

    @Transient
    private String projectTypeName;

    //项目来源 注册（通过项目工作室直接创建）workroom、立项projectApply、 立项直接注册register
    //todo 需要更新历史数据 projectSource为workroom
    @Column(name = "project_source")
    private String projectSource;


    //项目层级： 项目、课题、专题、任务
    @Column(name = "project_grade")
    private String projectGrade;


    @PreUpdate
    protected void onBeforeUpdate() {
        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
        if (org.springframework.util.StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
    }


}
