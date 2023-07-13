package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.template.domain.TreeEntity;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 王淼
 * @Title: A6
 * @Package com.cnpc.epai.core.worktask.domain
 * @Description: 功能描述
 * @date 17:19 2018/7/11
 * {修改记录：修改人、修改时间、修改内容等}
 */

@Getter
@Setter
@Entity
@Table(name = "SR_TASK_BUSINESS_NODO", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"parent", "name"})})
public class WorkTaskBusinessNode extends TreeEntity<WorkTaskBusinessNode,SrMetaDataset> {
    @Id
    @Column(name="BUSINESS_ID")
    private String id;

    @Column(name="WORKROOM_ID")
    private String projectId;

    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name="BUSINESS_NAME")
    private String name;

    @Column(name="BSFLAG")
    private String bsFlag;

    @Column(name="IS_START_USE")
    private String isStartUse;

    @Column(name="IS_STANDARD")
    private String isStandard;

    @Column(name="REMARKS")
    private String remarks;

    @Column(name="SORT_SEQUENCE")
    private Integer showOrder;

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


    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name="P_BUSINESS_ID")
    private WorkTaskBusinessNode parent;

    @Column(name="CODE")
    private String code;

    @Override
    public void setCode(String code){
        this.code = this.id;
    }

    @JsonIgnore
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.REFRESH},mappedBy = "parent")
    private List<WorkTaskBusinessNode> children = new ArrayList<WorkTaskBusinessNode>();

    @OneToMany(mappedBy = "workTaskBusinessNode")
    @OrderBy(value = " showOrder asc")
    @JsonIgnore
    private List<WorkTaskDataSet> rels = new ArrayList<WorkTaskDataSet>();

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "SR_PROJECT_WORKROOM_TREE",
            joinColumns = {
                    @JoinColumn(name = "BUSINESS_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "DATASET_ID")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"BUSINESS_ID", "DATASET_ID"})})
    private List<SrMetaDataset> elements = new ArrayList<SrMetaDataset>();

    @Override
    public List<SrMetaDataset> getElements(){
        List<SrMetaDataset> list = new ArrayList<SrMetaDataset>();
        for(WorkTaskDataSet rel:rels){
            list.add(rel.getDataSet());
        }
        return list;
    }
}
