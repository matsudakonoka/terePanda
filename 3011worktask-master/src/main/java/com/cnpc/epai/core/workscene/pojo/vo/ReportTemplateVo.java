package com.cnpc.epai.core.workscene.pojo.vo;

import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportTemplateVo {
    @ApiModelProperty("是否输出报告")
    private boolean reportOutput;
    @ApiModelProperty("报告模板ID")
    private String reportTemplateId;
    @ApiModelProperty("报告模板名称")
    private String reportTemplateName;

    private TreeNodeVo[] treeNodes;
    private List<WorkNavigateTreeNode> newTree;
}
