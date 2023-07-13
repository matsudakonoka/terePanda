package com.cnpc.epai.core.workscene.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ShareVo {

    @ApiModelProperty("项目是否共享")
    private boolean share;
    @ApiModelProperty("是否隔离")
    private boolean isolate;
    @ApiModelProperty("是否查看")
    private boolean view;
    @ApiModelProperty("是否下载")
    private boolean download;
    @ApiModelProperty("是否引用")
    private boolean quote;

}
