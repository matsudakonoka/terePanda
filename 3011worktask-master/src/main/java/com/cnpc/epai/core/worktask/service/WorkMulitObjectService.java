package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.worktask.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface WorkMulitObjectService {
    List<SrTaskTreeData> getOrderObject(String projectId, String wellId, String wellName, Integer distance, String workId,
                                        String datasetId, String datasetType, HttpServletRequest request) throws IOException;

    List<SrTaskTreeData> getObjectByYear(String nodeId,String userId);

    List<SrTaskTreeData> getObjectByUser(String workId, String userId, String nodeId, String dataStatus, Date startTime, Date endTime, String datasetId);

    Map<String,Object> getObjectBy(Pageable page, String workId, String fileName, String nodeId, String dataStatus, Date startTime, Date endTime, String datasetId, String userId,String belong, HttpServletRequest httpServletRequest) throws IOException;

    SrTaskTreeData saveSrTreeData(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                  HttpServletRequest httpServletRequest);
    SrTaskTreeData saveSrTreeData2(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                   String dataTargetTypeZt,HttpServletRequest httpServletRequest) throws Exception;

    List<SrTaskTreeData> beforeResults(JSONObject Object, HttpServletRequest httpServletRequest) throws Exception;

    SrWorkCollect saveSrWorkCollect(String resultId);

    Page<SrWorkCollect> getSrWorkCollect(String fileName, Date startTime, Date endTime, Pageable pageable);

    void deleteSrWorkCollect(String resultId);

    List<SrTaskTreeData> getSrTreeData(String workId, String dataSetId, String key);

    Page<SrTaskTreeData> getObjectByUserTask(Pageable page);

    List<SrTaskTreeData> getAllObject(String nodeId,String wellId, String userId);

    int saveTreeDataList(JSONObject dataList, HttpServletRequest request) throws Exception;

    List<Map<String,Object>> callBackTool(String boName, List<String> ptList, String workId);

    Map<String,Object> getTreeDataList(String workId,String nodeId,String nodeNames,String datasetId, String objectId,String objectNames,HttpServletRequest httpServletRequest) throws IOException;

    Map<String,Object> getObjectChoice(String projectId, String workId, String objectId, String nodeId, String datasetId, String status, String dataType, HttpServletRequest httpServletRequest) throws IOException;

    SrTaskTreeData saveSrTreeDataList(SrTaskTreeData srTaskTreeData);

    ArrayList<SrTaskTreeData> saveResultList(List<SrTaskTreeData> ResultList);

    boolean deleteSrTreeDataList(String projectId,List<Map> data, HttpServletRequest httpServletRequest) throws IOException;
    boolean deleteSrTreeDataListEx(String projectId,List<Map> data, HttpServletRequest httpServletRequest) throws IOException;
    boolean deleteSrTreeDataListRow(String projectId,List<Map> data, HttpServletRequest httpServletRequest) throws IOException;
    boolean updatedatalistEx(String workTreeDataId,List<Map> data, String datasetId,HttpServletRequest httpServletRequest) throws IOException;


    boolean deleteObjectList(String projectId,List<String> dataList,HttpServletRequest httpServletRequest) throws IOException;

    Map<String, Object> getNodeResourceList(String nodeIds, String resType,HttpServletRequest request) throws IOException;

    SrTaskTreeData getSrDataById(String dataId);

    List<SrTaskTreeData> saveDataContent(String projectId, SrTaskTreeData srTaskTreeData,List<Map> list, HttpServletRequest httpServletRequest) throws IOException;

    boolean updateFirstChoice(String id,String firstChoice);

    List<SrTaskTreeData> getAllObjectBy(String workId, String nodeId, String datasetId);

    boolean submitObjectData(String workId, String nodeId,String dataId, String datasetIds);

    List<SrTaskTreeData> importDataList(String projectId,SrTaskTreeData srTaskTreeData, HttpServletRequest httpServletRequest) throws IOException;
    List<SrTaskTreeData> importDataListEx(String projectId,SrTaskTreeData srTaskTreeData, List<Map> list,HttpServletRequest httpServletRequest) throws Exception;

    Map<String, Object> getNewTreeDataList(String projectId,String workId,String objectId,List<Map> data,HttpServletRequest httpServletRequest) throws IOException;
    Map<String, List<SrTaskTreeDataEx>> getNewObjectDataRecord(String projectId, String workId, String objectId, List<Map> data, HttpServletRequest httpServletRequest) throws IOException;
    Object getNewTreeDataListEx(String projectId,String workId,String objectId,List<Map> data,HttpServletRequest httpServletRequest) throws IOException;
    List<SrTaskTreeData> getNewObjectDataRecordDetails(String projectId,List<String> idList) throws IOException;
    SrTaskTreeData saveData(String workId, String taskId, String wellName,String wellPath, HttpServletRequest httpServletRequest) throws IOException;

    List<Map> getObjectContentList(String workId,String nodeId, String datasetId);

    List<Map> getAllTool(String id, HttpServletRequest httpServletRequest) throws IOException;

    List<Map> getToolOfDataset(String treeId, String toolId, HttpServletRequest httpServletRequest) throws IOException;

    void getSrTaskTreeDataListByIds(String[] ids, ApiResult apiResult);

    void updateT3NodeOrder(List<WorkNavigateTreeNode> list, ApiResult apiResult);
    //保存文件信息
    List<WorkTaskFileUpload> saveFileInfo(List<WorkTaskFileUpload> workTaskFileUploadList);

    //文件查询
    Page<WorkTaskFileUpload> findByObjectIdAndFileName(String objectId, String fileName, String collectionTab, String browsingTab, String updateTab, Pageable pageable);

    //获取同名文件
    WorkTaskFileUpload findFileByObjIdAndFn(String objectId, String fileName);

    //文件更新旧文件不启用
    void updateOldFile(String uploadId, String objectId, String fileId, String fileName, String isCollection);

    //文件收藏
    WorkTaskFileUploadRecord saveFileCollection(String objectId, String fileName);

    //取消收藏
    WorkTaskFileUploadRecord deleteFileCollection(String objectId, String fileId, String fileName, String adjacentFile);

    //浏览记录
    WorkTaskFileUploadRecord saveFileBrowsing(String objectId, String fileName, String onFileId);

    //获取文件列表
    List<WorkTaskFileUpload> getFileList(String objectId);

    //获取更新文件列表
    Page<WorkTaskFileUpload> getUpdateFileList(String objectId, String fileName, Pageable pageable);

    //获取收藏文件列表
    Page<WorkTaskFileUpload> getCollectionFileList(String objectId, String fileName, Pageable pageable);

    //获取浏览文件列表
    Page<WorkTaskFileUpload> getBrowsingFileList(String objectId, String fileName, Pageable pageable);

    //获取邻井资料列表
    List getAdjacentFileList(String objectId, String fileName) throws Exception;

    //文件查询
    List searchAdjacentFileList(String objectId, String fileName, String collectionTab, String browsingTab) throws Exception;

    //邻井资料收藏
    WorkTaskFileUploadRecord saveAdjacentFileCollection(String objectId, String fileName, String fileId, String fileState, String fileUploadUser, String createDate);

    //获取收藏文件列表
    List getAdjCollectionFileList(String objectId, String fileName);

    //邻井资料浏览记录
    WorkTaskFileUploadRecord saveAdjacentFileBrowsing(String objectId, String fileName, String fileId, String fileState, String fileUploadUser, String createDate);

    //获取浏览文件列表
    List getAdjBrowsingFileList(String objectId, String fileName);
}
