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
@Table(name = "SR_FILE_DATA_UPLOAD")
@ApiModel("文件资料上传表")
public class WorkTaskFileUpload extends StringIdEntity {
    @Id
    @Column(name = "UPLOAD_ID")
    @ApiModelProperty("上传ID")
    private String id;

    @Column(name = "FILE_ID")
    @ApiModelProperty("文件ID")
    private String fileId;

    @Column(name = "FILE_NAME")
    @ApiModelProperty("文件名称")
    private String fileName;

    @Column(name = "FILE_UPDATE")
    @ApiModelProperty("文件更新")
    private String fileUpdate;

    @Column(name = "FILE_STATE")
    @ApiModelProperty("文件状态")
    private String fileState;

    @Column(name = "FILE_COLLECTION")
    @ApiModelProperty("文件收藏")
    private String fileCollection;

    @Column(name = "FILE_BROWSING")
    @ApiModelProperty("文件浏览记录")
    private String fileBrowsing;

    @Column(name = "UPLOAD_USER")
    @ApiModelProperty("上传人")
    private String uploadUser;

    @Column(name = "UPLOAD_USER_ID")
    @ApiModelProperty("上传人id")
    private String uploadUserId;

    @Column(name = "OBJECT_NAME")
    @ApiModelProperty("研究对象名称")
    private String objectName;

    @Column(name = "OBJECT_ID")
    @ApiModelProperty("研究对象id")
    private String objectId;

    @Column(name = "ONFILE_ID")
    @ApiModelProperty("批注文件id")
    private String onFileId;

    @Column(name = "SORT_FLAG")
    @ApiModelProperty("排序标识")
    private Integer sortFlag;

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
