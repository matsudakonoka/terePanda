package com.cnpc.epai.core.workscene.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class GeoVo {
    @ApiModelProperty("地质体对象ID")
    private String geoObjId;
    @ApiModelProperty("地质体对象名称")
    private String geoObjName;
    private String objectType;
}
