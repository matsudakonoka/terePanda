package com.cnpc.epai.core.workscene.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.WorkNode;
import com.cnpc.epai.core.workscene.pojo.vo.ReportTemplateVo;

import java.util.List;

public interface WorkNodeService extends IService<WorkNode> {

    List<WorkNode> getNode(String workId);

    boolean isNodeStart(String workId, String nodeId);

    void startNode(String workId, String nodeId);
}
