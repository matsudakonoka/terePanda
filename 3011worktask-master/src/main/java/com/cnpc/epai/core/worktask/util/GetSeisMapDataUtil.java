package com.cnpc.epai.core.worktask.util;

import com.alibaba.fastjson.JSONArray;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.richfit.epai.asi.bo.SEIS.SEISFault;
import com.richfit.epai.asi.bo.SEIS.SEISHorizon3DDate;
import com.richfit.epai.asi.bo.SEIS.SEISPolygonSet;
import com.richfit.epai.asi.servicefactory.AsiEoDataServiceClientImpl;
import com.richfit.epai.asi.servicefactory.AsiFactory;

import java.util.List;

/**
 * @ClassName: GetSeisMapDataUtil
 * @Description:
 * @Author
 * @Date 2022/8/25
 * @Version 1.0
 */
public class GetSeisMapDataUtil {

    /**
     * 获取单个三维地震解释层位
     *
     * @param asiFactory
     * @param satelliteId    卫星端ID
     * @param dataSourceCode 账户配置ID
     * @param projectName    工区名称
     * @param id             数据ID
     * @param groupID        工区id
     * @param token
     * @return 单个三维地震解释层位信息列表
     */
    public JSONArray getSEISHorizon3DDateMap(AsiFactory asiFactory,String satelliteId, String dataSourceCode, String projectName, String id, String datasetId,String groupID,String objectId, String objectName,
                                             String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataBelong,String token) {
        JSONArray result = new JSONArray();
        try {
                String findOneId = id + "," + token + "," + groupID + "," + datasetId+ "," + objectId+ "," + objectName+ "," + dataSetName+ "," + workId+ "," + nodeId+ "," + nodeNames+ "," + taskId+ "," + dataBelong;
            SEISHorizon3DDate data = (SEISHorizon3DDate) asiFactory.createDataServiceFactory(satelliteId, dataSourceCode, projectName).
                    createAsiEoDataServiceClient(SEISHorizon3DDate.class).findOne(findOneId);
            result.add(data);
            //放入监听处理
//            String dataUrl = getPlatformDAddress(data.getDataUrl());
//            data.setDataUrl(dataUrl);
//            list = translate.getSEISHorizon3DDate(data);
//            list.get(0).put("EO_SOURCE_ID", satelliteId + "|" + dataSourceCode + "|" + projectName + "|" + id);

        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }



    /**
     * 从卫星端获取单个断层多边形集合
     *
     * @param asiFactory
     * @param satelliteId    卫星端ID
     * @param dataSourceCode 账户配置ID
     * @param projectName    工区名称
     * @param id             数据ID
     * @param groupID        工区id
     * @param token
     * @return 单个断层多边形集合
     */
    public JSONArray getSEISPolygonSetMap(AsiFactory asiFactory,String satelliteId, String dataSourceCode, String projectName, String id, String datasetId,String groupID,String objectId, String objectName,
                                          String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataBelong,String token) {
        JSONArray result = new JSONArray();
        try {
            String findOneId = id + "," + token + "," + groupID + "," + datasetId+ "," + objectId+ "," + objectName+ "," + dataSetName+ "," + workId+ "," + nodeId+ "," + nodeNames+ "," + taskId+ "," + dataBelong;
            AsiEoDataServiceClientImpl asiEoDataServiceClient = asiFactory.createDataServiceFactory(satelliteId, dataSourceCode, projectName).
                    createAsiEoDataServiceClient(SEISPolygonSet.class);
            asiEoDataServiceClient.getOperators();
            SEISPolygonSet data = (SEISPolygonSet)asiEoDataServiceClient .findOne(findOneId);
            result.add(data);
            //放入监听处理
//            String dataUrl = getPlatformDAddress(data.getDataUrl());
//            data.setDataUrl(dataUrl);
//            list = translate.getSEISPolygonSet(data);

        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }


    /**
     * 从卫星端获取单个断层多边形集合
     *
     * @param asiFactory
     * @param satelliteId    卫星端ID
     * @param dataSourceCode 账户配置ID
     * @param projectName    工区名称
     * @param id             数据ID
     * @param groupID        工区id
     * @param token
     * @return 单个断层多边形集合
     */
    public JSONArray getSEISFaultMap(AsiFactory asiFactory, String satelliteId, String dataSourceCode, String projectName, String id, String datasetId, String groupID,String objectId, String objectName,
                                     String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataBelong, String token) {
        JSONArray result = new JSONArray();
        try {
            String findOneId = id + "," + token + "," + groupID + "," + datasetId+ "," + objectId+ "," + objectName+ "," + dataSetName+ "," + workId+ "," + nodeId+ "," + nodeNames+ "," + taskId+ "," + dataBelong;
            AsiEoDataServiceClientImpl asiEoDataServiceClient = asiFactory.createDataServiceFactory(satelliteId, dataSourceCode, projectName).
                    createAsiEoDataServiceClient(SEISFault.class);
            List<String> operators = asiEoDataServiceClient.getOperators();
            SEISFault data = (SEISFault)asiEoDataServiceClient .findOne(findOneId);
            result.add(data);
            //放入监听处理
//            String dataUrl = getPlatformDAddress(data.getDataUrl());
//            data.setDataUrl(dataUrl);
//            list = translate.getSEISFault(data);

        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }

    private boolean saveupdateobject(){
        String dataTargetTypeZt = "研究资料";
        SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
        srTaskTreeData.setDataType("非结构化");
        srTaskTreeData.setObjectId(null);//井id
        return false;
    }
}
