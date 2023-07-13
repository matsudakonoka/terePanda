package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "SR_WORK_COLLECT")
@ApiModel("数据关联表")
public class SrWorkCollect extends StringIdEntity {

    @Id
    @Column(name = "COLLECT_ID")
    @ApiModelProperty("收藏ID")
    private String id;

    @Column(name = "FILE_NAME")
    @ApiModelProperty("文件名称")
    private String fileName;

    @Column(name = "FILE_URL")
    @ApiModelProperty("文件地址")
    private String fileUrl;

    @Column(name = "FILE_SIZE")
    @ApiModelProperty("文件大小")
    private String fileSzie;

    @Column(name="COLLECT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("收藏时间")
    private Date collect_date;

    @Column(name="UPLOAD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("创建时间")
    private Date uploadDate;

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

    @Transient
    private String workName;

    @Column(name="RESULT_TYPE")
    private String resultType;

    /*@PrePersist
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
    }*/

}
