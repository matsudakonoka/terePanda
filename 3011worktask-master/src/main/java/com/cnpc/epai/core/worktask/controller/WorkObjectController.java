package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.SrWorkCollect;
import com.cnpc.epai.core.worktask.service.WorkObjectService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

@RestController
@RequestMapping("/core/worktask/workobject")
public class WorkObjectController {


    @Autowired
    WorkObjectService workObjectService;

    @RequestMapping(value = "/getobjectdata", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "智能推荐", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getObjectData(
            @ApiParam(name = "projectId", value = "项目ID", required = false) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "objectId", value = "对象Id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "objectName", value = "对象名称", required = false) @RequestParam(name = "objectName", defaultValue="") String objectName,
            @ApiParam(name = "distance", value = "半径距离", required = false) @RequestParam(name="distance", defaultValue="") Double distance,
            @ApiParam(name = "workId", value = "工作Id", required = false) @RequestParam(name="workId", defaultValue="") String workId,
            @ApiParam(name = "datasetId", value = "数据集Id", required = false) @RequestParam(name="datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "type", value = "数据集类型", required = false) @RequestParam(name="type", defaultValue="") String datasetType,
            HttpServletRequest request
    ) throws IOException {
        if (distance != null && distance.intValue()<10){
            distance = distance*1000;
        }
        List<SrTaskTreeData> object = workObjectService.getOrderObject(projectId,objectId,objectName,distance.intValue(),workId,datasetId,datasetType,request);
        return object;
    }

    @RequestMapping(value = "/saveobjectdatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "同步侏罗纪数据列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> saveObjectDataList(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作Id", required = false) @RequestParam(name="workId", defaultValue="") String workId,
            @ApiParam(name = "datasetId", value = "数据集Id", required = false) @RequestParam(name="datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "datasetName", value = "数据集名称", required = false) @RequestParam(name="datasetName", defaultValue="") String datasetName,
            @ApiParam(value = "数据列表数据", type="body") @RequestBody JSONArray dataList,
            HttpServletRequest request
    ) throws IOException {
        Map<String,Object> map = new HashMap<>();
        List<SrTaskTreeData> object = workObjectService.saveTreeDataList(projectId,workId,datasetId,datasetName,dataList,request);
        if (object == null || object.size()==0){
            map.put("content",null);
            map.put("result","缺少必要参数");
        }else {
            Integer x = 0;
            for (SrTaskTreeData srTaskTreeData:object){
                if (srTaskTreeData.getDataContent()==null || srTaskTreeData.getDataContent().size()==0){

                }else {
                    x = x+srTaskTreeData.getDataContent().size();
                }
            }
            map.put("size",x);
            map.put("content",object);
            map.put("result","同步成功");
        }
        return map;
    }

    @RequestMapping(value = "/getobjectChoice", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "首选成果查询", notes = "", code =200,produces="application/json")
    public Map<String,Object> getObjectData(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作ID", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象ID", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "datasetId", value = "数据集id，多个用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "status", value = "首选状态", required = true) @RequestParam(name = "status", defaultValue="") String status,
            @ApiParam(name = "dataType", value = "成果类型", required = false) @RequestParam(name = "dataType", defaultValue="") String dataType,
            HttpServletRequest httpServletRequest
    ) throws Exception {
        try {
            Map<String,Object> object = workObjectService.getObjectChoice(projectId,workId,objectId,nodeId,datasetId,status,dataType,httpServletRequest);
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getobjectChoiceB", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "引用成果查询", notes = "", code =200,produces="application/json")
    public Map<String,Object> getObjectDataB(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作ID", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象ID", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "datasetId", value = "数据集id，多个用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "status", value = "首选状态", required = true) @RequestParam(name = "status", defaultValue="") String status,
            @ApiParam(name = "dataType", value = "成果类型", required = false) @RequestParam(name = "dataType", defaultValue="") String dataType,
            HttpServletRequest httpServletRequest
    ) throws Exception {
        try {
            Map<String,Object> object = workObjectService.getObjectChoiceB(projectId,workId,objectId,nodeId,datasetId,status,dataType,httpServletRequest);
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/callbacktool", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "常用工具回调接口", notes = "", code =200,produces="application/json")
    public List<Map<String,Object>> callBackTool(
            @ApiParam(name = "boName", value = "对象名称", required = true) @RequestParam(name = "boName", defaultValue="") String boName,
            @ApiParam(name = "ptList", value = "数据集合", required = true) @RequestParam(name = "ptList", defaultValue="") List<String> ptList,
            @ApiParam(name = "workId", value = "工作Id", required = true) @RequestParam(name = "workId", defaultValue="") String workId
    ) {
        return workObjectService.callBackTool(boName,ptList,workId);
    }

    @RequestMapping(value = "/getobjectdatalist", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询数据列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> getObjectDataList(
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "nodeId", value = "最小业务单元Id", required = true) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "nodeNames", value = "最小业务单元Id", required = true) @RequestParam(name = "nodeNames", defaultValue="") String nodeNames,
            @ApiParam(name = "datasetId", value = "当前选中节点包含数据集id,多个id用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "objectId", value = "对象Id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "objectNames", value = "对象名称", required = true) @RequestParam(name = "objectNames", defaultValue="") String objectNames,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        Map<String,Object> object = workObjectService.getTreeDataList(workId,nodeId,nodeNames,datasetId,objectId,objectNames,httpServletRequest);
        return object;
    }

    @RequestMapping(value = "/getnewobjectdatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据列表新版", notes = "", code =200,produces="application/json")
    public Map<String,Object> getNewObjectDataList(
            @ApiParam(name = "projectId", value = "项目id", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象Id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(value = "查询内容",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        List<Map> content = data.toJavaList(Map.class);
        Map<String,Object> object = workObjectService.getNewTreeDataList(projectId,workId,objectId,content,httpServletRequest);
        return object;
    }

    @RequestMapping(value = "/getNodeResourceList", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据集资源列表", notes = "", code =200,produces="application/json")
    public Map<String, Object> getNodeResourceList(
            @ApiParam(name = "nodeIds", value = "最小业务单元ID，多个ID之间用逗号分隔开", required = true) @RequestParam(name = "nodeIds", defaultValue="") String nodeIds,
            @ApiParam(name = "resType", value = "资源类型", required = true) @RequestParam(name = "resType", defaultValue="") String resType,
            HttpServletRequest request
    ) {
        Map<String, Object> object = null;
        try {
            object = workObjectService.getNodeResourceList(nodeIds,resType,request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }



    @RequestMapping(value = "/getoallbject", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "历史成果列表", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getAllObject(
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = true) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "objectId", value = "对象ID", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "userId", value = "当前用户id", required = false) @RequestParam(name = "userId", defaultValue="") String userId
    ) {
        return workObjectService.getAllObject(nodeId,objectId,userId);
    }

    @RequestMapping(value = "/getobjectbyyear", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "近期成果列表", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getObjectByYear(
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = true) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "userId", value = "当前用户id", required = false) @RequestParam(name = "userId", defaultValue="") String userId,
            HttpServletRequest httpServletRequest
    ) {
        return workObjectService.getObjectByYear(httpServletRequest,nodeId,userId);
    }

    @RequestMapping(value = "/getobjectbywork", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "工作内成果列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> getObjectByWork(
            @ApiParam(name = "workId", value = "工作ID", required = false) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "fileName", value = "关键字", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName,
            @ApiParam(name = "nodeId", value = "当前选中节点id,多个id用,号分割", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "dataStatus", value = "成果状态", required = false) @RequestParam(name = "objectStatus", defaultValue="") String dataStatus,
            @ApiParam(name = "startTime", value = "开始时间", required = false) @RequestParam(name="startTime", defaultValue="") Date startTime,
            @ApiParam(name = "endTime", value = "结束时间", required = false) @RequestParam(name="endTime", defaultValue="") Date endTime,
            @ApiParam(name = "datasetId", value = "当前选中节点包含数据集id,多个id用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "userId", value = "当前用户id", required = false) @RequestParam(name = "userId", defaultValue="") String userId,
            Pageable page,HttpServletRequest httpServletRequest
    ) throws IOException {
        return workObjectService.getObjectBy(page,workId,fileName,nodeId,dataStatus,startTime,endTime,datasetId,userId,httpServletRequest);
    }

    @RequestMapping(value = "/getallObjectby", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据条件查询成果列表包含全景图", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getAllObjectBy(
            @ApiParam(name = "workId", value = "工作ID", required = false) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "nodeId", value = "当前选中节点id,多个id用,号分割", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "datasetId", value = "当前选中节点包含数据集id,多个id用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId
    ) throws IOException {
        return workObjectService.getAllObjectBy(workId,nodeId,datasetId);
    }

    @RequestMapping(value = "/finddatabyid", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据ID查询数据成果", notes = "", code =200,produces="application/json")
    public ApiResult getSrDataById(@ApiParam(name = "dataId", value = "单条数据主键ID", required = true) @RequestParam(name = "dataId", defaultValue="") String dataId) {
        SrTaskTreeData srTaskTreeData = workObjectService.getSrDataById(dataId);
        return ApiResult.ofSuccessResultMsg(srTaskTreeData,"查询成功");
    }

    @RequestMapping(value = "/getobjectbyuser", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "个人首页成果列表", notes = "", code =200,produces="application/json")
    public Page<SrTaskTreeData> getObjectByUserTask(
            Pageable page
    ) {
        return workObjectService.getObjectByUserTask(page);
    }

    @ApiOperation(value = "Compass保存成果接口", notes = "Compass保存成果接口", code = 200, produces = "application/json")
    @PostMapping("/saveData")
    public Map<String, Object> saveData(@ApiParam(name = "workId", value = "工作id", required = false) @RequestParam(name = "workId", required = false) String workId,
                                        @ApiParam(name = "taskId", value = "任务id", required = false) @RequestParam(name = "taskId",required = false) String taskId,
                                        @ApiParam(name = "wellName", value = "井名", required = true) @RequestParam(name = "wellName") String wellName,
                                        @ApiParam(name = "wellPath", value = "井轨迹,多条以逗号分割，依次存储测深|垂深|井斜角|方位角|东西位移|南北位移|闭合距", required = true) @RequestParam(name = "wellPath") String wellPath,
//                                       @ApiParam(name = "wellPath", value = "井轨迹", required = true) @RequestBody String wellPath,
                                        HttpServletRequest httpServletRequest) {
        Map<String, Object> map = new HashMap<>();
        try {
            String wellPathD = URLDecoder.decode( wellPath,"UTF-8");
            wellPathD = new String(Base64.getDecoder().decode(wellPathD),"utf-8");
            SrTaskTreeData ss = workObjectService.saveData(workId, taskId, wellName, wellPathD, httpServletRequest);
            if (ss == null){

                map.put("res","上传数据缺少必要数据");
                map.put("content",null);
            }else {
                map.put("res","保存成功");
                map.put("content",ss);
                return map;
            }
        } catch (Exception e) {
            map.put("res",e);
            map.put("content",null);
            e.printStackTrace();
        }
        return map;
    }


    @RequestMapping(value = "/saveupdateobject", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存用户上传成果数据", notes = "", code =200,produces="application/json")
    public Map<String,Object> saveObject(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(value = "上传数据",required = true)@RequestBody JSONArray data,
            SrTaskTreeData srTaskTreeData, HttpServletRequest httpServletRequest) {
        boolean x = false;
        Map<String,Object> map = new HashMap<>();
        try {
            SrTaskTreeData ss = workObjectService.saveSrTreeData(projectId,data,srTaskTreeData,httpServletRequest,null);
            if (ss == null){
                map.put("res","上传数据缺少必要数据");
                map.put("content",null);
            }else {
                map.put("res","保存成功");
                map.put("content",ss);
                return map;
            }
        } catch (Exception e) {
            map.put("res",e);
            map.put("content",null);
            e.printStackTrace();
        }
        return map;
    }

    @RequestMapping(value = "/saveupdatedata", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存用户上传数据列表内容", notes = "", code =200,produces="application/json")
    public Map<String,Object> saveDataContent(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "datasetType", value = "数据集类型", required = false) @RequestParam(name = "datasetType", defaultValue="") String datasetType,
            @ApiParam(name = "dataType", value = "数据类型", required = false) @RequestParam(name = "dataType", defaultValue="") String dataType,
            @ApiParam(name = "objectId", value = "对象ID", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "objectName", value = "对象名称", required = false) @RequestParam(name = "objectName", defaultValue="") String objectName,
            @ApiParam(name = "workId", value = "工作Id", required = false) @RequestParam(name="workId", defaultValue="") String workId,
            @ApiParam(name = "datasetId", value = "数据集Id", required = false) @RequestParam(name="datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "datasetName", value = "数据集名称", required = false) @RequestParam(name="datasetName", defaultValue="") String datasetName,
            @ApiParam(name = "nodeId", value = "最小业务单元id", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "nodeNames", value = "最小业务单元名称", required = false) @RequestParam(name = "nodeNames", defaultValue="") String nodeNames,
            @ApiParam(name = "source", value = "数据来源", required = false) @RequestParam(name = "source", defaultValue="") String source,
            @ApiParam(name = "taskId", value = "任务Id", required = false) @RequestParam(name = "taskId", defaultValue="") String taskId,
            @ApiParam(value = "上传数据",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        Map<String,Object> map = new HashMap<>();
        try {
            List<Map> list = new ArrayList<>();
            list = JSON.parseArray(data.toJSONString(),Map.class);
            SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
            srTaskTreeData.setDatasetType(datasetType);
            srTaskTreeData.setDataType("结构化");
            srTaskTreeData.setObjectId(objectId);
            srTaskTreeData.setObjectName(objectName);
            srTaskTreeData.setWorkId(workId);
            srTaskTreeData.setDatasetId(datasetId);
            srTaskTreeData.setDatasetName(datasetName);
            srTaskTreeData.setNodeId(nodeId);
            srTaskTreeData.setNodeNames(nodeNames);
            srTaskTreeData.setSource(source);
            srTaskTreeData.setDataContent(list);
            srTaskTreeData.setTaskId(taskId);
            List<SrTaskTreeData> srTaskTreeData1 = workObjectService.saveDataContent(projectId,srTaskTreeData,list,httpServletRequest);
            if (srTaskTreeData1 == null){
                map.put("content",null);
                map.put("res","缺少必要参数，无法同步");
            }else if (srTaskTreeData1.size()==0){
                map.put("content",null);
                map.put("result","导入重复数据");
            }else {
                map.put("content",srTaskTreeData1);
                map.put("res","同步成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("content",null);
            map.put("res",e);
        }
        return map;
    }

    @RequestMapping(value = "/importdatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "导入数据列表内容", notes = "", code =200,produces="application/json")
    public Map<String,Object> importDataList(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "datasetType", value = "数据集类型", required = false) @RequestParam(name = "datasetType", defaultValue="") String datasetType,
            @ApiParam(name = "dataType", value = "数据类型", required = false) @RequestParam(name = "dataType", defaultValue="") String dataType,
            @ApiParam(name = "objectId", value = "对象ID", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "objectName", value = "对象名称", required = false) @RequestParam(name = "objectName", defaultValue="") String objectName,
            @ApiParam(name = "workId", value = "工作Id", required = false) @RequestParam(name="workId", defaultValue="") String workId,
            @ApiParam(name = "datasetId", value = "数据集Id", required = false) @RequestParam(name="datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "datasetName", value = "数据集名称", required = false) @RequestParam(name="datasetName", defaultValue="") String datasetName,
            @ApiParam(name = "nodeId", value = "最小业务单元id", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "nodeNames", value = "最小业务单元名称", required = false) @RequestParam(name = "nodeNames", defaultValue="") String nodeNames,
            @ApiParam(name = "source", value = "数据来源", required = false) @RequestParam(name = "source", defaultValue="") String source,
            @ApiParam(name = "taskId", value = "任务Id", required = false) @RequestParam(name = "taskId", defaultValue="") String taskId,
            @ApiParam(value = "导入数据",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        Map<String,Object> map = new HashMap<>();
        try {
            List<Map> list = new ArrayList<>();
            list = JSON.parseArray(data.toJSONString(),Map.class);
            SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
            srTaskTreeData.setDatasetType(datasetType);
            srTaskTreeData.setDataType("结构化");
            srTaskTreeData.setObjectId(objectId);
            srTaskTreeData.setObjectName(objectName);
            srTaskTreeData.setWorkId(workId);
            srTaskTreeData.setDatasetId(datasetId);
            srTaskTreeData.setDatasetName(datasetName);
            srTaskTreeData.setNodeId(nodeId);
            srTaskTreeData.setNodeNames(nodeNames);
            srTaskTreeData.setSource(source);
            srTaskTreeData.setDataContent(list);
            srTaskTreeData.setTaskId(taskId);
            List<SrTaskTreeData> srTaskTreeDataList = workObjectService.importDataList(projectId,srTaskTreeData,httpServletRequest);
            if (srTaskTreeDataList==null){
                map.put("content",null);
                map.put("result","缺少必要参数");
            }else if (srTaskTreeDataList.size()==0){
                map.put("content",null);
                map.put("result","导入重复数据");
            }else {
                map.put("content",srTaskTreeDataList);
                map.put("result","导入成功");
            }
        } catch (Exception e) {
            map.put("content",null);
            map.put("result",e);
            e.printStackTrace();
        }
        return map;
    }

    @RequestMapping(value = "/savesrtreedatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新数据列表及成果列表对象内容", notes = "", code =200,produces="application/json")
    public SrTaskTreeData saveSrTreeDataList(
            @ApiParam(value = "数据对象",required = true)@RequestBody SrTaskTreeData srTaskTreeData) {
        boolean x = false;
        try {
            return workObjectService.saveSrTreeDataList(srTaskTreeData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SrTaskTreeData();
    }

    @RequestMapping(value = "/updateFirstChoice", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更改首选项状态", notes = "", code =200,produces="application/json")
    public boolean updateFirstChoice(
            @ApiParam(name = "id", value = "成果主键", required = true) @RequestParam(name = "id", defaultValue="") String id,
            @ApiParam(name = "firstChoice", value = "首选项状态", required = true) @RequestParam(name = "firstChoice", defaultValue="") String firstChoice
    ) {
        boolean x = false;
        try {
            return workObjectService.updateFirstChoice(id,firstChoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x;
    }

    @RequestMapping(value = "/deletedatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "删除列表数据", notes = "", code =200,produces="application/json")
    public boolean deleteSrTreeDataListData(
            @ApiParam(name = "projectId", value = "项目Id", required = false) @RequestParam(name="projectId", defaultValue="") String projectId,
            @ApiParam(value = "删除列表数据，[{\"id\":\"关联关系表id\",\"DSID\":\"当前被删除数据的DSID},....]",required = true)@RequestBody List<Map> data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            return workObjectService.deleteSrTreeDataList(projectId,data,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/submitObjectData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "提交成果数据", notes = "", code =200,produces="application/json")
    public boolean submitObjectData(
            @ApiParam(name = "workId", value = "工作Id", required = false) @RequestParam(name="workId", defaultValue="") String workId,
            @ApiParam(name = "nodeId", value = "最小业务单元id", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "dataId", value = "单个成果ID", required = false) @RequestParam(name="dataId", defaultValue="") String dataId,
            @ApiParam(name = "datasetIds", value = "数据集Id,多个用,号分割", required = false) @RequestParam(name="datasetIds", defaultValue="") String datasetIds
    ){
        boolean x = false;
        try {
            return workObjectService.submitObjectData(workId,nodeId,dataId,datasetIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/deleteobjectlist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "删除成果数据", notes = "", code =200,produces="application/json")
    public boolean deleteSrTreeDataListObject(
            @ApiParam(name = "projectId", value = "项目Id", required = false) @RequestParam(name="projectId", defaultValue="") String projectId,
            @ApiParam(name = "id", value = "成果主键,多个主键用,号分割", required = true) @RequestParam(name = "id", defaultValue="") String id,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            List<String> idList = new ArrayList<>();
            if(StringUtils.isNotBlank(id)) {
                idList = Arrays.asList(id.split(","));
            }else {
                return false;
            }
            return workObjectService.deleteObjectList(projectId,idList,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x;
    }

    @RequestMapping(value = "/savesrworkcollect", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存收藏", notes = "", code =200,produces="application/json")
    public ApiResult saveSrWorkCollect(@ApiParam(name = "resultId", value = "成果ID", required = false) @RequestParam(name = "resultId", defaultValue="") String resultId) {
        SrWorkCollect srWorkCollect = workObjectService.saveSrWorkCollect(resultId);
        return ApiResult.ofSuccessResultMsg(srWorkCollect,"保存成功");
    }

    @RequestMapping(value = "/getcollectlist", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取收藏列表", notes = "", code =200,produces="application/json")
    public Page<SrWorkCollect> saveSrWorkCollect(
            @ApiParam(name = "fileName", value = "关键字", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName,
            @ApiParam(name = "startTime", value = "开始时间", required = false) @RequestParam(name="startTime", defaultValue="") Date startTime,
            @ApiParam(name = "endTime", value = "结束时间", required = false) @RequestParam(name="endTime", defaultValue="") Date endTime,
            Pageable pageable) {
        return workObjectService.getSrWorkCollect(fileName, startTime, endTime, pageable);
    }

    @RequestMapping(value = "/deleteSrWorkCollect", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "取消收藏", notes = "", code =200,produces="application/json")
    public ApiResult deleteSrWorkCollect(@ApiParam(name = "resultId", value = "成果ID", required = false) @RequestParam(name = "resultId", defaultValue="") String resultId) {
        try {
            workObjectService.deleteSrWorkCollect(resultId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ApiResult.ofSuccess();
    }

    @RequestMapping(value = "/getsrtredata", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取上传数据", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getSrTreeData(
            @ApiParam(name = "workId", value = "工作ID", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "dataSetId", value = "数据集ID", required = false) @RequestParam(name = "dataSetId", defaultValue="") String dataSetId,
            @ApiParam(name = "key", value = "关键字", required = false) @RequestParam(name = "key", defaultValue="") String key
    ) {
        boolean x = false;
        try {
            return workObjectService.getSrTreeData(workId,dataSetId,key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getAllTool", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取所有工具", notes = "", code =200,produces="application/json")
    public List<Map> getAllTool(
            @ApiParam(name = "id", value = "树ID", required = true) @RequestParam(name = "id", defaultValue="") String id,
            HttpServletRequest httpServletRequest
    ) {
        boolean x = false;
        try {
            return workObjectService.getAllTool(id,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getToolOfDataset", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取组件所属节点下数据集", notes = "", code =200,produces="application/json")
    public List<Map> getToolOfDataset(
            @ApiParam(name = "treeId", value = "树ID", required = true) @RequestParam(name = "treeId", defaultValue="") String treeId,
            @ApiParam(name = "toolId", value = "组件ID", required = true) @RequestParam(name = "toolId", defaultValue="") String toolId,
            HttpServletRequest httpServletRequest
    ) {
        boolean x = false;
        try {
            return workObjectService.getToolOfDataset(treeId,toolId,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
