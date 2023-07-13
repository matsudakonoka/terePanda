package com.cnpc.epai.core.workscene.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChargeUserVo implements Facade {

    @ApiModelProperty("负责人ID")
    private String chargeUserId;
    @ApiModelProperty("负责人名称")
    private String chargeUserName;

    @Override
    public String id() {
        return chargeUserId;
    }
}
