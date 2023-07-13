package com.cnpc.epai.core.worktask.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnpc.epai.common.communication.CacheManager;
import com.cnpc.epai.core.mphandler.JsonbTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

@Data
@TableName("SR_TASK_TREE_DATA")
public class SrTaskTreeDataEx {

    @ApiModelProperty("数据ID")
    @TableId(value = "work_tree_data_id")
    private String id;

    @ApiModelProperty("数据集类型,数据列表为data，成果列表为file")
    @TableField(value = "tree_data_id")
    private String datasetType;

    @ApiModelProperty("关联数据类型，分为结构化非结构化")
    @TableField(value = "TREE_DATA_TYPE")
    private String dataType;


    @TableField(value = "FILE_ID")
    @ApiModelProperty("文件全名")
    private String fileAllName;

    @ApiModelProperty("成果名称")
    @TableField(value = "FILE_NAME")
    private String fileName;


    @ApiModelProperty("文件地址")
    @TableField(value = "FILE_URL")
    private String fileId;

    @ApiModelProperty("文件大小")
    @TableField(value = "FILE_SIZE")
    private String fileSize;

    @ApiModelProperty("成果状态")
    @TableField(value = "DATA_STATUS")
    private String dataStatus;

    @ApiModelProperty("任务ID")
    @TableField(value = "TASK_ID")
    private String taskId;

    @ApiModelProperty("工作ID")
    @TableField(value = "WORK_ID")
    private String workId;


    @ApiModelProperty("数据集ID")
    @TableField(value = "DATASET_ID")
    private String datasetId;


    @ApiModelProperty("数据集名称")
    @TableField(value = "DATASET_NAME")
    private String datasetName;



    @TableField(value = "DATA_CONTENT", typeHandler = JsonbTypeHandler.class)
    private Object dataContent;


    @ApiModelProperty("首选标识,首选项为A,引用状态为B,默认状态为C")
    @TableField(value = "FIRST_CHOICE")
    private String firstChoice;


    @ApiModelProperty("来源")
    @TableField(value = "SOURCE")
    private String source;


    @ApiModelProperty("对象ID")
    @TableField(value = "OBJECT_ID")
    private String objectId;


    @ApiModelProperty("对象名")
    @TableField(value = "OBJECT_NAME")
    private String objectName;


    @ApiModelProperty("最小业务单元ID")
    @TableField(value = "NODE_ID")
    private String nodeId;


    @ApiModelProperty("最小业务单元节点名称")
    @TableField(value = "NODE_NAME")
    private String nodeNames;

    @ApiModelProperty("创建用户")
    @TableField(value = "CREATE_USER")
    private String createUser;


    @ApiModelProperty("创建时间")
    @TableField(value = "CREATE_DATE")
    private Date createDate;

    @ApiModelProperty("更新用户")
    @TableField(value = "UPDATE_USER")
    private String updateUser;

    @TableField(value = "UPDATE_DATE")
    @ApiModelProperty("更新时间")
    private Date updateDate;

//    @TableField(value = "BSFLAG")
    @TableField(exist = false)
    @ApiModelProperty("删除标识")
    private String bsflag;

    @TableField(value = "REMARKS")
    @ApiModelProperty("备注")
    private String remarks;

    @TableField(value = "REF_SOURCE_IDS")
    @ApiModelProperty("成果追溯ID")
    private String refSourceIds;

    @TableField(exist = false)
    private String createUserName;

    public String getUserDisplayName() {
        String userDisplayName = null;
        if(StringUtils.isNotBlank(this.createUser)) {
            Map map = (Map) CacheManager.of().hOMGet("userscache", this.createUser);
            userDisplayName = (String) map.get("displayName");
        }
        return userDisplayName;
    }

}
