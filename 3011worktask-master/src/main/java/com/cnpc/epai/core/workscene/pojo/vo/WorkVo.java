package com.cnpc.epai.core.workscene.pojo.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkVo {

    @ApiModelProperty("工作id, 创建工作不传")
    private String workId;
    @ApiModelProperty("场景模板ID")
    private String templateId;
    @ApiModelProperty("场景模板名称")
    private String templateName;
    private String dataRegion;
    private JSONObject[] workObjects;
    private TreeNodeVo[] treeNodes;
    private List<WorkNavigateTreeNode> newTree;
    @ApiModelProperty("地质体类型")
    private String geoType;

    private String workName;
    @ApiModelProperty("工作名称")
    private ChargeUserVo[] chargeUser;


    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date endTime;

    @ApiModelProperty("备注")
    private String remarks;


}
