package com.cnpc.epai.core.workscene.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author liuTao
 * @version 1.0
 * @name KeywordMaintenance
 * @description
 * @date 2021/10/14 13:24
 */
@Data
public class KeywordMaintenanceVo {
    @ApiModelProperty("关键字id")
    private String keywordId;

    @ApiModelProperty("关键字名称")
    private String keywordName;

    @ApiModelProperty("关键字类型")
    private String typeId;

    @ApiModelProperty("关键字类型")
    private String typeName;

    @ApiModelProperty("关键字状态")
    private String keywordStatus;

    @ApiModelProperty("关键字排序号")
    private Integer keywordSortNum;

    @ApiModelProperty("逻辑删除")
    private String bsflag;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateDate;
}
