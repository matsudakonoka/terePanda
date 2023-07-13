package com.cnpc.epai.core.workscene.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TreeNodeVo implements Facade {
    @ApiModelProperty("业务节点ID")
    private String nodeId;
    @ApiModelProperty("业务节点名称")
    private String nodeName;

    @Override
    public String id() {
        return nodeId;
    }
}
