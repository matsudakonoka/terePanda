package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.communication.CacheManager;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.worktask.util.JsonbMaaConverter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Entity
@ToString
@Table(name = "SR_TASK_TREE_DATA")
@ApiModel("数据关联表")
@Converter(name = "jsonb", converterClass = JsonbMaaConverter.class)
public class SrTaskTreeData{

    @Id
    @Column(name = "WORK_TREE_DATA_ID")
    @ApiModelProperty("数据ID")
    private String id;

    @Column(name = "TREE_DATA_ID")
    @ApiModelProperty("数据集类型,数据列表为data，成果列表为file")
    private String datasetType;

    @Column(name = "TREE_DATA_TYPE")
    @ApiModelProperty("关联数据类型，分为结构化非结构化")
    private String dataType;

    @Column(name = "DATA_TARGET_TYPE")
    @ApiModelProperty("数据类型")
    private String dataTargetType;

    @Column(name = "FILE_ID")
    @ApiModelProperty("文件全名")
    private String fileAllName;

    @Column(name = "FILE_NAME")
    @ApiModelProperty("成果名称")
    private String fileName;

    @Column(name = "FILE_URL")
    @ApiModelProperty("文件地址")
    private String fileId;

    @Column(name = "FILE_SIZE")
    @ApiModelProperty("文件大小")
    private String fileSize;

    @Column(name = "DATA_STATUS")
    @ApiModelProperty("成果状态")
    private String dataStatus;

    @Column(name = "TASK_ID")
    @ApiModelProperty("任务ID")
    private String taskId;

    @Column(name = "WORK_ID")
    @ApiModelProperty("工作ID")
    private String workId;

    @Column(name = "DATASET_ID")
    @ApiModelProperty("数据集ID")
    private String datasetId;

    @Column(name = "DATASET_NAME")
    @ApiModelProperty("数据集名称")
    private String datasetName;

    @Column(name = "DATA_CONTENT")
    @Convert("jsonb")
    private List<Map> dataContent;

    @Column(name = "FIRST_CHOICE")
    @ApiModelProperty("首选标识,首选项为A,引用状态为B,默认状态为C")
    private String firstChoice;

    @Column(name = "SOURCE")
    @ApiModelProperty("来源")
    private String source;

    @Column(name = "OBJECT_ID")
    @ApiModelProperty("对象ID")
    private String objectId;

    @Column(name = "OBJECT_NAME")
    @ApiModelProperty("对象名")
    private String objectName;

    @Column(name = "NODE_ID")
    @ApiModelProperty("最小业务单元ID")
    private String nodeId;

    @Column(name = "NODE_NAME")
    @ApiModelProperty("最小业务单元节点名称")
    private String nodeNames;

    @Column(name="CREATE_USER")
    @ApiModelProperty("创建用户")
    private String createUser;

    @Transient
    private String createUserName;

    @Column(name="CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("创建时间")
    private Date createDate;

    @Column(name="UPDATE_USER")
    @ApiModelProperty("更新用户")
    private String updateUser;

    @Column(name="UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("更新时间")
    private Date updateDate;

    @Column(name="BSFLAG")
    @ApiModelProperty("删除标识")
    private String bsflag;

    @Column(name="REMARKS")
    @ApiModelProperty("备注")
    private String remarks;

    @Column(name="REF_SOURCE_IDS")
    @ApiModelProperty("成果追溯ID")
    private String refSourceIds;

    @Column(name="BELONG")
    @ApiModelProperty("属于")
    private String belong;

    @Transient
    private boolean flag;

    @Transient
    private String taskName;

    @Transient
    private boolean isCollected;

    @SuppressWarnings("rawtypes")
    public String getUserDisplayName() {
        String userDisplayName = null;
        if(StringUtils.isNotBlank(this.createUser)) {
            Map map = (Map) CacheManager.of().hOMGet("userscache", this.createUser);
            userDisplayName = (String) map.get("displayName");
        }
        return userDisplayName;
    }

    @PrePersist
    protected void prePersist() {
        if (StringUtils.isBlank(this.getId())) {
            this.setId(ShortUUID.randomUUID());
        }
        this.createDate = new Date();
        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
        if(StringUtils.isBlank(this.createUser)) {
            this.createUser = SpringManager.getCurrentUser().getUserId();
        }
        if (StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
        if (StringUtils.isEmpty(this.dataStatus)){
            this.dataStatus = "待提交";
        }
        if (StringUtils.isEmpty(this.firstChoice)){
            this.firstChoice = "C";
        }
    }




    @PreUpdate
    protected void preUpdate(){
        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
        if (StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
    }
}
