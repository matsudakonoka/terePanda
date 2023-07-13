package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.pojo.vo.AssignVo;
import com.cnpc.epai.core.workscene.pojo.vo.ReportTemplateVo;

import java.util.List;

public interface WorkSceneTaskService extends IService<WorkTask> {
    void assignWork(String workId, AssignVo[] assignVos) throws Exception;

    List<JSONObject> getMinWorks(String workId, String userId, String nodeId);

    WorkTask getWorkTask(String workId, String userId);

    Object getNode(String workId, String userId);

    List<WorkTask> getWorkTask(String workId);
}
