package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.worktask.pojo.SrPresetSceneTask;
import com.cnpc.epai.core.worktask.pojo.SrTaskMgr;

import java.util.List;
import java.util.Map;


public interface WorkTaskSecondService {

    SrTaskMgr createWorktask(SrTaskMgr workTask, String userIds,String nodes, String toolIds, String softwareIds)throws BusinessException;

    SrTaskMgr createSceneWorktask(Map<String,Object> paramMap) throws BusinessException;

    List<Map<String,Object>> getPresetSceneList(String workroomId, String taskId);

    String submitTask(String taskId, String fileIds)throws BusinessException;
}
