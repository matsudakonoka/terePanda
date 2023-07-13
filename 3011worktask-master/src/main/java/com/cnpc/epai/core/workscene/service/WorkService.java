package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.Geo;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.pojo.vo.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface WorkService extends IService<Work> {
    Work create(WorkVo workVo);

    boolean configTemplate(String workId, ReportTemplateVo reportTemplate);

    void configShare(String workId, ShareVo share);

    Object getTree(String workId, String nodeName, String type, String pNodeId);

    List getMinNodes(String treeId, String nodeId);

    JSONObject getWork(String workId);

    Object getDocument(String workId, String userId, String status, Date startTime, Date endTime);

    Object getTreeByUser(String workId, String nodeName);

    boolean isExist(String template, String geoType, GeoVo[] geoVos);


    Object getWorkByTemplate(String template, boolean withStatus);

    List<Geo> getGeo(String workId);

    Object workNodes(String workId);

    List<Work> getWorkByShare(ShareVo share);

    Object getDataObject(String workId, String nodeId, String dataSets);


}
