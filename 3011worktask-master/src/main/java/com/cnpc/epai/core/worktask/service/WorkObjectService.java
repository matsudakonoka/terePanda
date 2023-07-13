package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONArray;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.SrWorkCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface WorkObjectService {
    List<SrTaskTreeData> getOrderObject(String projectId, String wellId, String wellName, Integer distance, String workId,
                                        String datasetId,String datasetType, HttpServletRequest request) throws IOException;

    List<SrTaskTreeData> getObjectByYear(HttpServletRequest httpServletRequest,String nodeId,String userId);

    List<SrTaskTreeData> getObjectByUser(String workId, String userId, String nodeId, String dataStatus, Date startTime, Date endTime,String datasetId);
    List<SrTaskTreeData> getObjectByUser(String workId, String userId, String nodeId, String dataStatus, Date startTime, Date endTime,String datasetId,String dataTargetType);
    List<SrTaskTreeData> getObjectByUser(String workId, String userId, String nodeId, String dataStatus, Date startTime, Date endTime,String datasetId,String dataTargetType,String dataType);

    Map<String,Object> getObjectBy(Pageable page, String workId, String fileName, String nodeId, String dataStatus, Date startTime, Date endTime, String datasetId,String userId,HttpServletRequest httpServletRequest) throws IOException;

    SrTaskTreeData saveSrTreeData(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                  HttpServletRequest httpServletRequest,String token);

    SrWorkCollect saveSrWorkCollect(String resultId);

    Page<SrWorkCollect> getSrWorkCollect(String fileName, Date startTime, Date endTime, Pageable pageable);

    void deleteSrWorkCollect(String resultId);

    List<SrTaskTreeData> getSrTreeData(String workId, String dataSetId, String key);

    Page<SrTaskTreeData> getObjectByUserTask(Pageable page);

    List<SrTaskTreeData> getAllObject(String nodeId,String wellId, String userId);

    List<SrTaskTreeData> saveTreeDataList(String projectId, String workId, String datasetId, String datasetName,JSONArray dataList, HttpServletRequest request) throws IOException;

    List<Map<String,Object>> callBackTool(String boName, List<String> ptList, String workId);

    Map<String,Object> getTreeDataList(String workId,String nodeId,String nodeNames,String datasetId, String objectId,String objectNames,HttpServletRequest httpServletRequest) throws IOException;

    Map<String,Object> getObjectChoice(String projectId, String workId, String objectId, String nodeId, String datasetId, String status, String dataType, HttpServletRequest httpServletRequest) throws IOException;

    SrTaskTreeData saveSrTreeDataList(SrTaskTreeData srTaskTreeData);


    boolean deleteSrTreeDataList(String projectId,List<Map> data, HttpServletRequest httpServletRequest) throws IOException;


    boolean deleteObjectList(String projectId,List<String> dataList,HttpServletRequest httpServletRequest) throws IOException;

    Map<String, Object> getNodeResourceList(String nodeIds, String resType,HttpServletRequest request) throws IOException;

    SrTaskTreeData getSrDataById(String dataId);

    List<SrTaskTreeData> saveDataContent(String projectId, SrTaskTreeData srTaskTreeData,List<Map> list, HttpServletRequest httpServletRequest) throws IOException;

    boolean updateFirstChoice(String id,String firstChoice);

    List<SrTaskTreeData> getAllObjectBy(String workId, String nodeId, String datasetId);

    boolean submitObjectData(String workId, String nodeId,String dataId, String datasetIds);

    List<SrTaskTreeData> importDataList(String projectId,SrTaskTreeData srTaskTreeData, HttpServletRequest httpServletRequest) throws IOException;

    Map<String, Object> getNewTreeDataList(String projectId,String workId,String objectId,List<Map> data,HttpServletRequest httpServletRequest) throws IOException;

    SrTaskTreeData saveData(String workId, String taskId, String wellName,String wellPath, HttpServletRequest httpServletRequest) throws IOException;

    List<Map> getObjectContentList(String workId,String nodeId, String datasetId);

    List<Map> getAllTool(String id, HttpServletRequest httpServletRequest) throws IOException;

    List<Map> getToolOfDataset(String treeId, String toolId, HttpServletRequest httpServletRequest) throws IOException;

    Map<String, Object> getObjectChoiceB(String projectId, String workId, String objectId, String nodeId, String datasetId, String status, String dataType, HttpServletRequest httpServletRequest);
}
