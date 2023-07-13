package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface DataTransportService {
    void broadcast(JSONObject indexData) throws CloneNotSupportedException;

    Object getIndexData(JSONObject indexData);

    Object getIndex();

    JSONObject sendAsi(String satelliteIdTarget,String sourceTarget,String localMac,String projectNameTarget,String dataSourceTarget,JSONObject indexData);

    String saveObjectFromAsi(String groupID, String eoCode, String datasetId,
                             String satelliteIdTarget, String dataSourceCodeTarget,
                             String projectNameTarget, List<String> dataIdTargets,
                             String objectId, String objectName,
                             String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataTargetType,String dataBelong, String token) throws Exception;

    boolean broadCastFromTool(String wellId,String wellName, JSONArray dataSets, String projectId , HttpServletRequest httpServletRequest) throws IOException;

    boolean broadCastFromSchDesign(String wellName, String projectId) throws BusinessException;
}
