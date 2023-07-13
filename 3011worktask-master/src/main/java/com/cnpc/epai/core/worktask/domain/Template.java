package com.cnpc.epai.core.worktask.domain;

import com.cnpc.epai.common.communication.CacheManager;
import com.cnpc.epai.common.template.domain.StringIdEntity;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "SR_PUBLIC_TEMPLATE")
@ApiModel("场景模板表")
public class Template extends StringIdEntity {

    @Id
    @Column(name = "TEMPLATE_ID")
    @ApiModelProperty("场景模板ID")
    private String id;

    @Column(name = "TEMPLATE_OBEJECT_TYPE")
    @ApiModelProperty("对象类型")
    private String objectType;

    @Column(name = "TEMPLATE_NAME")
    @ApiModelProperty("场景模板名称")
    private String templateName;

    @Column(name = "IS_CHECK")
    @ApiModelProperty("是否多选，N为不多选，Y为多选")
    private String isCheck;

    @Column(name = "IS_AVAILABLE")
    @ApiModelProperty("是否可用,N为不可用，Y为可用")
    private String isAvailable;

    @Column(name = "DISCOVER_ORGS")
    @ApiModelProperty("授权组织id用，分割")
    private String discoverOrgs;

    @Transient
    @ApiModelProperty("组织机构属性")
    private List<String> discoverOrgsIds;

    @Column(name="CREATE_USER")
    @ApiModelProperty("创建用户")
    private String createUser;

    @Column(name="CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("创建时间")
    private Date createDate;

    @Column(name="UPDATE_USER")
    @ApiModelProperty("更新用户")
    private String updateUser;

    @Column(name="UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty("更新时间")
    private Date updateDate;

    @Column(name="BSFLAG")
    @ApiModelProperty("删除标识")
    private String bsflag;

    @Column(name="REMARKS")
    @ApiModelProperty("备注")
    private String remarks;

    @SuppressWarnings("rawtypes")
    public String getUserDisplayName() {
        String userDisplayName = null;
        if(StringUtils.isNotBlank(this.createUser)) {
            Map map = (Map) CacheManager.of().hOMGet("userscache", this.createUser);
            userDisplayName = (String) map.get("displayName");
        }
        return userDisplayName;
    }

    public List<String>  getDiscoverOrgNames() {
        if (this.getDiscoverOrgs()!=null && this.getDiscoverOrgs()!="") {
            List<String> s = Arrays.asList(this.getDiscoverOrgs().split(","));
            this.setDiscoverOrgsIds(s);
        }
        List<String>  rtnList = null;
        if(discoverOrgsIds != null && discoverOrgsIds.size()==0) {
            rtnList = new ArrayList<>();
        } else if (discoverOrgsIds != null && discoverOrgsIds.size()>0) {
            Map<String, String> orgMap = discoverOrgsIds.size()>0? CacheManager.of().hMGet("master_organization", discoverOrgsIds.toArray(new String[discoverOrgsIds.size()])) : new HashMap<String,String>();
            rtnList = new ArrayList<>();
            for(String orgId:discoverOrgsIds) {
                rtnList.add(orgMap.getOrDefault(orgId, orgId));
            }
        }
        return rtnList;
    }



    @PrePersist
    protected void prePersist() {
        if (StringUtils.isBlank(this.getId())) {
            this.setId(ShortUUID.randomUUID());
        }
        this.createDate = new Date();
        if(StringUtils.isBlank(this.createUser)) {
            this.createUser = SpringManager.getCurrentUser().getUserId();
        }
        if (StringUtils.isEmpty(this.isCheck)) {
            this.bsflag = "N";
        }
        if (StringUtils.isEmpty(this.isAvailable)) {
            this.bsflag = "Y";
        }
        if (StringUtils.isEmpty(this.bsflag)) {
            this.bsflag = "N";
        }
    }

}
