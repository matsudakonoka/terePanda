package com.cnpc.epai.core.worktask.domain;


import com.cnpc.epai.common.template.domain.StringIdEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description: 应用软件用户信息和项目的关系
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Getter
@Setter
@Entity
@Table(name = "sr_project_user_software")
public class ProjectUserSoftware extends StringIdEntity {

    @Id
    @Column(name = "user_software_id")
    private String id;

    @Column(name = "satellite_id")
    private String satelliteId;

    @Column(name = "workroom_id")
    private String projectId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "software_id")
    private String softwareId;

    @Column(name = "software_user")
    private String softwareUser;

    @Column(name = "software_pwd")
    private String softwarePwd;

    @Column(name = "REMARKS")
    private String remarks;

    //逻辑删除标识，表示该条记录在用或者已经失效，Y表示该记录已经失效，N表示在用，默认N
    @Column(name = "BSFLAG")
    private String bsflag;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
}
