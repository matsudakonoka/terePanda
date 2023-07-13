package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "SR_FILE_DATA_RECORD")
@ApiModel("文件资料上传关联表")
public class WorkTaskFileUploadRecord extends StringIdEntity {
    @Id
    @Column(name = "RECORD_ID")
    @ApiModelProperty("记录ID")
    private String id;

    @Column(name = "FILE_NAME")
    @ApiModelProperty("文件名称")
    private String fileName;

    @Column(name = "FILE_ID")
    @ApiModelProperty("文件ID")
    private String fileId;

    @Column(name = "UPLOAD_ID")
    @ApiModelProperty("上传ID")
    private String uploadId;

    @Column(name = "FILE_UPLOAD_USER")
    @ApiModelProperty("文件上传人")
    private String fileUploadUser;

    @Column(name = "OBJECT_ID")
    @ApiModelProperty("研究对象ID")
    private String objectId;

    @Column(name = "FILE_STATE")
    @ApiModelProperty("文件状态")
    private String fileState;

    @Column(name = "FILE_FLAG")
    @ApiModelProperty("文件记录标识")
    private String fileFlag;

    @Column(name = "RECORD_FLAG")
    @ApiModelProperty("收藏浏览标识")
    private String recordFlag;

    @Column(name = "REMARKS")
    @ApiModelProperty("备注")
    private String remarks;

    @Column(name="BSFLAG")
    @ApiModelProperty("删除标识")
    private String bsflag;

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

}
