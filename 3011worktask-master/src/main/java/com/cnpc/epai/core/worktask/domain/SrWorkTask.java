package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.repository.DataSetRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 王博
 * @version 1.0.0
 * @Description: 工作实体
 * @date 2021/9/7
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "SR_WORK_TASK")
public class SrWorkTask extends StringIdEntity {

    @Id
    @Column(name = "TASK_ID")
    private String id;

    @Column(name = "TASK_NAME")
    private String taskName;

    @Column(name = "WORK_ID")
    private String workId;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "CHARGE_USER_ID")
    private String chargeUserId;

    @Column(name = "CHARGE_USER_NAME")
    private String chargeUserName;

    @Column(name = "TREE_NODE_IDS")
    private String treeNodeIds;

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

    @Transient
    private String GeoType;

}
