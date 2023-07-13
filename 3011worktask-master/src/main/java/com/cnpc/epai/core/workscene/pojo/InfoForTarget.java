package com.cnpc.epai.core.workscene.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class InfoForTarget {
	@ApiParam("数据源 数据库ID")
	private String dataSourceCodeTarget;

	@ApiParam("目标EPSG编码")
	private Integer epsgCode;

	@ApiParam("工区名称")
	private String projectNameTarget;

	@ApiParam("推送的卫星端ID")
	private String satelliteIdTarget;

	@ApiParam("推送卫星端的softname")
	private String sourceTarget;

	@ApiParam("测网名称")
	private String surveyNameTarget;

	@ApiParam("测网类型")
	private String surveyTypeTarget;

	@ApiParam("目标用户")
	private String userID;

	@ApiParam("井集名称")
	private String wellSetTarget;

	@ApiParam("isSendable")
	private String isSendable;
}
