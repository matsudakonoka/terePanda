package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ToString
@Table(name = "SR_EXPERT_CONTENT")
@ApiModel("专家信息表")
public class ExpertContent {
    @Id
    @Column(name = "EXPERT_CONTENT_ID")
    @ApiModelProperty("专家信息ID")
    private String id;

    @Column(name = "EXPERT_NAME")
    @ApiModelProperty("专家名称")
    private String expertName;

    @Column(name = "EXPERT_UNIT")
    @ApiModelProperty("专家单位")
    private String expertUnit;

    @Column(name = "PROFESSION_ID")
    @ApiModelProperty("专家职务ID")
    private String professionId;

    @Column(name = "PROFESSION_NAME")
    @ApiModelProperty("专家职务名称")
    private String professionName;

    @Column(name = "EXPERT_LAND_LINE")
    @ApiModelProperty("专家座机")
    private String landLine;

    @Column(name = "EXPERT_CELL_PHONE")
    @ApiModelProperty("专家手机")
    private String cellPhone;

    @Column(name = "EXPERT_ADRESS")
    @ApiModelProperty("专家办公地址")
    private String adress;

    @Column(name="CREATE_USER")
    @ApiModelProperty("创建用户")
    private String createUser;

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
