package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;


@Data
@TableName("sr_navigate_tree_node")
public class WorkNavigateTreeNode {
    @TableId(value = "node_id", type = IdType.ASSIGN_ID)
    private String nodeId;
    private String treeId;
    private String pNodeId;
    private String sourceNodeId;
    private String nodeName;
    private Long sortSequence;
    private String nodeIcon;
    private String nodeType;
    private String targetId;
    private String dataRegion;
    private String remarks;
    private String bsflag;
    private String isEnable;
    private String attribute;

    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}
