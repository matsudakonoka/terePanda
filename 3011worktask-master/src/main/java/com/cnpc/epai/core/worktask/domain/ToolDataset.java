package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.communication.CacheManager;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Data
@Entity
@ToString
@Table(name = "SR_META_TOOL_DATASET")
@ApiModel("常用工具关联数据集关系表")
public class ToolDataset {

    @Id
    @Column(name = "TOOL_DATASET_ID")
    @ApiModelProperty("工具关联ID")
    private String id;

    @Column(name = "TOOL_ID")
    @ApiModelProperty("常用工具ID")
    private String toolId;

    @Column(name = "DATASET_ID")
    @ApiModelProperty("数据集ID")
    private String datasetId;

    @Column(name = "DATASET_NAME")
    @ApiModelProperty("数据集名称")
    private String datasetName;

    @Column(name = "REMARKS")
    @ApiModelProperty("备注")
    private String remarks;

    @Column(name="BSFLAG")
    @ApiModelProperty("删除标识")
    private String bsflag;

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

    @PrePersist
    protected void onBeforeCreate() {
        this.id = ShortUUID.randomUUID();
        this.createDate = new Date();
        this.createUser = SpringManager.getCurrentUser().getUserId();

        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
        this.bsflag = "N";
    }

    @PreUpdate
    protected void onBeforeUpdate() {
        this.updateDate = new Date();
        this.updateUser = SpringManager.getCurrentUser().getUserId();
    }

    @SuppressWarnings("rawtypes")
    public String getUserDisplayName() {
        String userDisplayName = null;
        if(StringUtils.isNotBlank(this.createUser)) {
            Map map = (Map) CacheManager.of().hOMGet("userscache", this.createUser);
            userDisplayName = (String) map.get("displayName");
        }
        return userDisplayName;
    }
}
