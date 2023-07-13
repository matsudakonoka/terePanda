package com.cnpc.epai.core.worktask.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@ToString
@Entity
@Table(name = "SR_PERSONAL_TREEDATA")
public class SrPersonalTreedata {
    @Id
    @Column(name="id")
    @ApiModelProperty("ID")
    private String id;

    @Column(name="user_id")
    @ApiModelProperty("用户ID")
    private String userId;

    @Column(name="tree_data")
    @ApiModelProperty("用户保存的节点树")
    private String treeData;

    @Column(name="remarks")
    @ApiModelProperty("备注")
    private String remarks;

    @Column(name="bsflag")
    @ApiModelProperty("删除标识")
    private String bsflag;

    @Column(name="create_user")
    @ApiModelProperty("创建人")
    private String createUser;

    @Column(name="create_date")
    @ApiModelProperty("创建时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name="update_user")
    @ApiModelProperty("修改人")
    private String updateUser;

    @Column(name="update_date")
    @ApiModelProperty("修改时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
}
