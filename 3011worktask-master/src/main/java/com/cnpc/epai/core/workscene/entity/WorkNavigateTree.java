package com.cnpc.epai.core.workscene.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Builder
@Data
@TableName("sr_navigate_tree")
public class WorkNavigateTree {
    @TableId(value = "tree_id", type = IdType.ASSIGN_ID)
    private String treeId;
    private String isTemplate;
    private String templateName;
    private String templateLevel;
    private String source_templateId;
    private String treeType;
    private String purpose;
    private String purposeId;
    private String dataRegion;
    private String remarks;
    private String bsflag;
    private String keyWords;
    private String businessType;
    private String masterType;
    private String isEnable;
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}
