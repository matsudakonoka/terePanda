package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.worktask.domain.*;
import com.cnpc.epai.core.worktask.mapper.SrTaskTreeDataMapper;
import com.cnpc.epai.core.worktask.service.WorkMulitObjectService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/core/worktask/workobjectMulit")
public class WorkMulitObjectController {
    @Autowired
    WorkMulitObjectService workMulitObjectService;
    @Autowired
    SrTaskTreeDataMapper srTaskTreeDataMapper;

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
        List<SrTaskTreeData> object = workMulitObjectService.getOrderObject(projectId,objectId,objectName,distance.intValue(),workId,datasetId,datasetType,request);
        return object;
    }

    @RequestMapping(value = "/getobjectChoice", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "首选成果查询", notes = "", code =200,produces="application/json")
    public Map<String,Object> getObjectData(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作ID", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象ID", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "datasetId", value = "数据集id，多个用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
            @ApiParam(name = "status", value = "首选状态", required = true) @RequestParam(name = "status", defaultValue="") String status,
            @ApiParam(name = "dataType", value = "成果类型", required = false) @RequestParam(name = "dataType", defaultValue="") String dataType,
            HttpServletRequest httpServletRequest
    ) throws Exception {
        try {
            Map<String,Object> object = workMulitObjectService.getObjectChoice(projectId,workId,objectId,nodeId,datasetId,status,dataType,httpServletRequest);
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
        return workMulitObjectService.callBackTool(boName,ptList,workId);
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
        Map<String,Object> object = workMulitObjectService.getTreeDataList(workId,nodeId,nodeNames,datasetId,objectId,objectNames,httpServletRequest);
        return object;
    }

    @RequestMapping(value = "/getnewobjectdatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据列表新版", notes = "", code =200,produces="application/json")
    public Map<String,Object> getNewObjectDataList(
            @ApiParam(name = "projectId", value = "项目id", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象Id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(value = "查询内容",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        List<Map> content = data.toJavaList(Map.class);
        Map<String,Object> object = workMulitObjectService.getNewTreeDataList(projectId,workId,objectId,content,httpServletRequest);
        return object;
    }
    @RequestMapping(value = "/getNewObjectDataRecord", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据列表新版", notes = "", code =200,produces="application/json")
    public Object getNewObjectDataRecord(
            @ApiParam(name = "projectId", value = "项目id", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象Id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(value = "查询内容",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        List<Map> content = data.toJavaList(Map.class);
        Map<String,List<SrTaskTreeDataEx>> object = workMulitObjectService.getNewObjectDataRecord(projectId,workId,objectId,content,httpServletRequest);
        return object;
    }

    @RequestMapping(value = "/getnewobjectdatalistEx", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据列表新版", notes = "", code =200,produces="application/json")
    public Object getNewObjectDataListEx(
            @ApiParam(name = "projectId", value = "项目id", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象Id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(value = "查询内容",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        List<Map> content = data.toJavaList(Map.class);
        Object object = workMulitObjectService.getNewTreeDataListEx(projectId,workId,objectId,content,httpServletRequest);
        return object;
    }
    @RequestMapping(value = "/getNewObjectDataRecordDetails", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询数据列表新版", notes = "", code =200,produces="application/json")
    public Object getNewObjectDataRecordDetails(
            @ApiParam(name = "projectId", value = "项目id", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "workId", value = "工作id", required = true) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "objectId", value = "对象Id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(value = "查询内容",required = true)@RequestBody JSONArray data,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        List<Map> content = data.toJavaList(Map.class);
        List<String> idList = content.stream().map(obj -> (String) obj.get("id")).collect(Collectors.toList());

        List<SrTaskTreeData> object = workMulitObjectService.getNewObjectDataRecordDetails(projectId,idList);
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
            object = workMulitObjectService.getNodeResourceList(nodeIds,resType,request);
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
        return workMulitObjectService.getAllObject(nodeId,objectId,userId);
    }

    @RequestMapping(value = "/getobjectbyyear", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "近期成果列表", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getObjectByYear(
            @ApiParam(name = "nodeId", value = "最小业务单元ID", required = true) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "userId", value = "当前用户id", required = false) @RequestParam(name = "userId", defaultValue="") String userId
    ) {
        return workMulitObjectService.getObjectByYear(nodeId,userId);
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
            @ApiParam(name = "belong", value = "属于", required = false) @RequestParam(name = "belong", defaultValue="") String belong,
            Pageable page, HttpServletRequest httpServletRequest
    ) throws IOException {
        return workMulitObjectService.getObjectBy(page,workId,fileName,nodeId,dataStatus,startTime,endTime,datasetId,userId,belong,httpServletRequest);
    }

    @RequestMapping(value = "/getallObjectby", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据条件查询成果列表包含全景图", notes = "", code =200,produces="application/json")
    public List<SrTaskTreeData> getAllObjectBy(
            @ApiParam(name = "workId", value = "工作ID", required = false) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "nodeId", value = "当前选中节点id,多个id用,号分割", required = false) @RequestParam(name = "nodeId", defaultValue="") String nodeId,
            @ApiParam(name = "datasetId", value = "当前选中节点包含数据集id,多个id用,号分割", required = false) @RequestParam(name = "datasetId", defaultValue="") String datasetId
    ) throws IOException {
        return workMulitObjectService.getAllObjectBy(workId,nodeId,datasetId);
    }

    @RequestMapping(value = "/finddatabyid", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据ID查询数据成果", notes = "", code =200,produces="application/json")
    public ApiResult getSrDataById(@ApiParam(name = "dataId", value = "单条数据主键ID", required = true) @RequestParam(name = "dataId", defaultValue="") String dataId) {
        SrTaskTreeData srTaskTreeData = workMulitObjectService.getSrDataById(dataId);
        return ApiResult.ofSuccessResultMsg(srTaskTreeData,"查询成功");
    }

    @RequestMapping(value = "/getobjectbyuser", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "个人首页成果列表", notes = "", code =200,produces="application/json")
    public Page<SrTaskTreeData> getObjectByUserTask(
            Pageable page
    ) {
        return workMulitObjectService.getObjectByUserTask(page);
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
            SrTaskTreeData ss = workMulitObjectService.saveData(workId, taskId, wellName, wellPathD, httpServletRequest);
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

    @PostMapping("/beforeResults")
    @ResponseBody
    @ApiOperation(value ="报告邻井复用成果列表、研究资料的数据",notes="",code=200,produces="application/json")
    public ApiResult beforeResults(@ApiParam(value = "邻井复用",required = true)@RequestBody JSONObject object,
                                   HttpServletRequest httpServletRequest){
        ApiResult apiResult = ApiResult.newInstance();
        String objectId = object.get("objectId")==null?"":object.get("objectId").toString();
        String objectName = object.get("objectName")==null?"":object.get("objectName").toString();
        String objectType = object.get("objectType")==null?"":object.get("objectType").toString();
        String workIdgb = object.get("workIdgb")==null?"":object.get("workIdgb").toString();
        String workIdfy = object.get("workIdfy")==null?"":object.get("workIdfy").toString();
        String nodeIds = object.get("nodeIds")==null?"":object.get("nodeIds").toString();//T2树id
        if(StringUtils.isEmpty(objectId) || StringUtils.isEmpty(objectName) || StringUtils.isEmpty(objectType)){
            apiResult.setMsg("对象ID、对象名称或对象类型不能为空！");
        }else if(StringUtils.isEmpty(workIdgb) || StringUtils.isEmpty(workIdfy)){
            apiResult.setMsg("报告或复用报告对应的workId不能为空！");
        }else if(StringUtils.isEmpty(nodeIds)){
            apiResult.setMsg("复用报告对应的节点ID不能为空！");
        }
        if(StringUtils.isNotEmpty(apiResult.getMsg())){
            apiResult.setFlag(false);
            return apiResult;
        }
        try{
            List<SrTaskTreeData> dataList =workMulitObjectService.beforeResults(object,httpServletRequest);
            apiResult.setFlag(true);
            apiResult.setResult(dataList);
            apiResult.setMsg("保存成功");
        }catch(Exception e){
            apiResult.setFlag(false);
            apiResult.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return apiResult;
    }

    @RequestMapping(value = "/saveupdateobject", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存用户上传成果数据", notes = "", code =200,produces="application/json")
    public Map<String,Object> saveObject(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "dataTargetTypeZt", value = "区分研究资料或成果列表", required = true) @RequestParam(name = "dataTargetTypeZt", defaultValue="") String dataTargetTypeZt,
            @ApiParam(value = "上传数据",required = true)@RequestBody JSONArray data,
            SrTaskTreeData srTaskTreeData, HttpServletRequest httpServletRequest) {
        Map<String,Object> map = new HashMap<>();
        try {
            SrTaskTreeData ss = workMulitObjectService.saveSrTreeData2(projectId,data,srTaskTreeData,dataTargetTypeZt,httpServletRequest);
            if (ss == null){
                map.put("res","上传数据缺少必要数据");
                map.put("content",null);
            }else {
                map.put("res","保存成功");
                map.put("content",ss);
                return map;
            }
        } catch (Exception e) {
            map.put("msg",e.getMessage());
            map.put("res",e);
            map.put("content",null);
            e.printStackTrace();
        }
        return map;
    }
    @RequestMapping(value = "/saveobjectdatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "同步侏罗纪数据列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> saveObjectDataList(
            @ApiParam(value = "数据列表数据", type="body") @RequestBody JSONObject object,
            HttpServletRequest request
    ) throws Exception {
        Map<String,Object> map = new HashMap<>();
        int x = workMulitObjectService.saveTreeDataList(object,request);
        map.put("size",x);
        map.put("content",object);
        map.put("result","同步成功");
        return map;
    }
    @GetMapping("/getObjectTreeData")
//    @RequestMapping(value = "/getObjectTreeData", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询数据集" ,notes = "", code = 200,produces = "application/json")
    public ApiResult getObjectByUser(
            @ApiParam(name = "workId", value = "工作id", required = false) @RequestParam(name = "workId", required = false) String workId,
            @ApiParam(name = "objectId",value = "对象id",required = false) @RequestParam(name = "objectId", required = false) String objectId,
            @ApiParam(name = "datasetId",value = "数据集id",required = false) @RequestParam(name = "datasetId", required = false) String datasetId
    ){
        ApiResult apiResult=ApiResult.newInstance();
        try{ //成果浏览
            QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("WORK_ID",workId);
            if(StringUtils.isNotEmpty(objectId)){
                queryWrapper.eq("object_id",objectId);
            }
            if(StringUtils.isNotEmpty(datasetId)){
                queryWrapper.eq("dataset_id",datasetId);
            }
            queryWrapper.eq("source","侏罗纪");
            List<SrTaskTreeDataEx> list = srTaskTreeDataMapper.selectList(queryWrapper);
            list.forEach(item->{
                item.setDataContent(null);
            });
            apiResult.setFlag(true);
            apiResult.setResult(list);
            apiResult.setMsg("查询成功");
        }catch(Exception e){
            apiResult.setFlag(false);
            apiResult.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return apiResult;
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
            List<Map> list;
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
            List<SrTaskTreeData> srTaskTreeData1 = workMulitObjectService.saveDataContent(projectId,srTaskTreeData,list,httpServletRequest);
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
            List<Map> list = JSON.parseArray(data.toJSONString(),Map.class);
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
            List<SrTaskTreeData> srTaskTreeDataList = workMulitObjectService.importDataList(projectId,srTaskTreeData,httpServletRequest);
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
    @RequestMapping(value = "/importdatalistEx", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "导入数据列表内容", notes = "", code =200,produces="application/json")
    public Map<String,Object> importDataListEx(
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
            List<Map> list = JSON.parseArray(data.toJSONString(),Map.class);
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
            List<SrTaskTreeData> srTaskTreeDataList = workMulitObjectService.importDataListEx(projectId,srTaskTreeData,list,httpServletRequest);
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
            map.put("result",e.getMessage());
            map.put("res",e);
            map.put("content",null);
            e.printStackTrace();
        }
        return map;
    }

    @RequestMapping(value = "/savesrtreedatalist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新数据列表及成果列表对象内容", notes = "", code =200,produces="application/json")
    public SrTaskTreeData saveSrTreeDataList( @ApiParam(value = "数据对象",required = true)@RequestBody SrTaskTreeData srTaskTreeData) {
        boolean x = false;
        try {
            return workMulitObjectService.saveSrTreeDataList(srTaskTreeData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SrTaskTreeData();
    }
    /* 作者： 谁家的小乖*/
    @RequestMapping(value = "/saveResultList", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新数据列表及成果列表", notes = "", code =200,produces="application/json")
    public JSONObject saveResultList(@ApiParam(value = "数据对象",required = true)@RequestBody List<SrTaskTreeData> ResultList) {
        JSONObject jsonObject = new JSONObject();
        try {
            ArrayList<SrTaskTreeData> backList = workMulitObjectService.saveResultList(ResultList);
            jsonObject.put("rssult",backList);
            jsonObject.put("flag",true);
            jsonObject.put("msg","成功");
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("rssult",new ArrayList<>());
            jsonObject.put("flag",true);
            jsonObject.put("msg","成功");
            return jsonObject;
        }
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
            return workMulitObjectService.updateFirstChoice(id,firstChoice);
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
            return workMulitObjectService.deleteSrTreeDataList(projectId,data,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @RequestMapping(value = "/deleteSrTreeDataListDataRow", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "删除列表数据", notes = "", code =200,produces="application/json")
    public boolean deleteSrTreeDataListDataRow(
            @ApiParam(name = "id", value = "id", required = false) @RequestParam(name="id", defaultValue="") String id,
            @ApiParam(value = "删除列表数据，[{\"id\":\"关联关系表id\",\"DSID\":\"当前被删除数据的DSID},....]",required = true)@RequestBody List<Map> data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            return workMulitObjectService.deleteSrTreeDataListRow(id,data,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @RequestMapping(value = "/deleteSrTreeDataListDataEx", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "删除列表数据", notes = "", code =200,produces="application/json")
    public boolean deleteSrTreeDataListDataEx(
            @ApiParam(name = "id", value = "id", required = false) @RequestParam(name="id", defaultValue="") String id,
            @ApiParam(value = "删除列表数据，[{\"id\":\"关联关系表id\",\"DSID\":\"当前被删除数据的DSID},....]",required = true)@RequestBody List<Map> data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            return workMulitObjectService.deleteSrTreeDataListEx(id,data,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @RequestMapping(value = "/updateSrTreeDataListDataEx", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新明细数据", notes = "", code =200,produces="application/json")
    public boolean updateSrTreeDataListDataEx(
            @ApiParam(name = "id", value = "id", required = true) @RequestParam(name="id", defaultValue="") String id,
            @ApiParam(name = "datasetId", value = "datasetId", required = true) @RequestParam(name="datasetId", defaultValue="") String datasetId,
            @ApiParam(value = "更新明细数据,主记录的明细数据[{}.{}]",required = true)@RequestBody List<Map> data,
            HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            if(StringUtils.isEmpty(id) || StringUtils.isEmpty(datasetId)){
                return false;
            }
            return workMulitObjectService.updatedatalistEx(id,data,datasetId,httpServletRequest);
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
            return workMulitObjectService.submitObjectData(workId,nodeId,dataId,datasetIds);
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
            return workMulitObjectService.deleteObjectList(projectId,idList,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x;
    }

    @RequestMapping(value = "/savesrworkcollect", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存收藏", notes = "", code =200,produces="application/json")
    public ApiResult saveSrWorkCollect(@ApiParam(name = "resultId", value = "成果ID", required = false) @RequestParam(name = "resultId", defaultValue="") String resultId) {
        SrWorkCollect srWorkCollect = workMulitObjectService.saveSrWorkCollect(resultId);
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
        return workMulitObjectService.getSrWorkCollect(fileName, startTime, endTime, pageable);
    }

    @RequestMapping(value = "/deleteSrWorkCollect", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "取消收藏", notes = "", code =200,produces="application/json")
    public ApiResult deleteSrWorkCollect(@ApiParam(name = "resultId", value = "成果ID", required = false) @RequestParam(name = "resultId", defaultValue="") String resultId) {
        try {
            workMulitObjectService.deleteSrWorkCollect(resultId);
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
            return workMulitObjectService.getSrTreeData(workId,dataSetId,key);
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
            return workMulitObjectService.getAllTool(id,httpServletRequest);
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
            return workMulitObjectService.getToolOfDataset(treeId,toolId,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @GetMapping("/getSrTaskTreeDataListByIds/{ids}")
    public ApiResult getSrTaskTreeDataListByIds(@PathVariable String[]  ids){
        ApiResult apiResult = ApiResult.newInstance();
        workMulitObjectService.getSrTaskTreeDataListByIds(ids,apiResult);
        return apiResult;
    }
    @PostMapping("/updateT3NodeOrder")
    public ApiResult updateT3NodeOrder(@RequestBody List<WorkNavigateTreeNode> list){
        ApiResult apiResult = ApiResult.newInstance();
        workMulitObjectService.updateT3NodeOrder(list,apiResult);

        return apiResult;
    }

    /**
     * 保存文件信息
     */
    @RequestMapping(value = "/saveFileInfo", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存文件信息", notes = "保存地质资料上传文件信息", code =200,produces="application/json")
    public ApiResult saveFileInfo(@ApiParam(name = "fileState", value = "文件状态", required = false) @RequestParam(name = "fileState",required = false) String fileState,
                                  @ApiParam(name = "uploadUser", value = "上传人", required = true) @RequestParam(name = "uploadUser",required = false) String uploadUser,
                                  @ApiParam(name = "uploadUserId", value = "上传人ID", required = false) @RequestParam(name = "uploadUserId",required = false) String uploadUserId,
                                  @ApiParam(name = "objectName", value = "研究对象名称", required = true) @RequestParam(name = "objectName",required = false) String objectName,
                                  @ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId",required = false) String objectId,
                                  @ApiParam(value = "文件信息", required = true)@RequestBody JSONArray fileInfo) {
        try {
            if (fileInfo.size() != 0){
                List<Map> list = JSON.parseArray(fileInfo.toJSONString(), Map.class);
                List<WorkTaskFileUpload> workTaskFileUploadList = new ArrayList<>();
                for(int i = 0; i < list.size(); i++){
                    WorkTaskFileUpload workTaskFileUpload = new WorkTaskFileUpload();
                    String uploadId = ShortUUID.randomUUID();
                    workTaskFileUpload.setId(uploadId);
                    workTaskFileUpload.setFileId(list.get(i).get("fileId").toString());
                    workTaskFileUpload.setFileName(list.get(i).get("fileName").toString());
                    String isUpdateFile = list.get(i).get("isUpdateFile").toString();
                    workTaskFileUpload.setSortFlag((Integer) list.get(i).get("coincidence"));
                    if(isUpdateFile.equals("1")) {
                        try{
                            workMulitObjectService.updateOldFile(uploadId, objectId, list.get(i).get("fileId").toString(), list.get(i).get("fileName").toString(), list.get(i).get("isCollection").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        workTaskFileUpload.setFileUpdate("1");
                        workTaskFileUpload.setFileCollection(list.get(i).get("isCollection").toString());
                    }else {
                        workTaskFileUpload.setId(uploadId);
                        workTaskFileUpload.setFileUpdate("0");
                        workTaskFileUpload.setFileCollection("0");
                    }
                    workTaskFileUpload.setFileBrowsing("0");
                    workTaskFileUpload.setFileState(fileState);
                    workTaskFileUpload.setUploadUser(uploadUser);
                    workTaskFileUpload.setUploadUserId(uploadUserId);
                    workTaskFileUpload.setObjectName(objectName);
                    workTaskFileUpload.setObjectId(objectId);
                    workTaskFileUpload.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    workTaskFileUpload.setCreateDate(new Date(System.currentTimeMillis()));
                    workTaskFileUpload.setBsflag("N");
                    workTaskFileUploadList.add(workTaskFileUpload);
                }
                workTaskFileUploadList = workMulitObjectService.saveFileInfo(workTaskFileUploadList);
                return ApiResult.ofSuccessResultMsg(workTaskFileUploadList,"保存成功");
            }else{
                return ApiResult.ofFailureResultMsg(null, "文件为空");
            }
        } catch (Exception e){
            e.printStackTrace();
            return ApiResult.ofFailureResultMsg(null, "保存失败");
        }
    }

    /**
     * 文件查询
     * @param objectId 研究对象id
     * @param fileName 文件名称
     * @param pageable 分页
     * @return 文件数据
     */
    @RequestMapping(value = "/getQueryFile", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询获取文件列表", notes = "", code =200, produces="application/json")
    public Page<WorkTaskFileUpload> getFileListByObjIdAndFileName(
            @ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "fileName", value = "文件名称", required = true) @RequestParam(name = "fileName", defaultValue="") String fileName,
            @ApiParam(name = "collectionTab", value = "收藏页查询", required = false) @RequestParam(name = "collectionTab", defaultValue="") String collectionTab,
            @ApiParam(name = "browsingTab", value = "浏览记录页查询", required = false) @RequestParam(name = "browsingTab", defaultValue="") String browsingTab,
            @ApiParam(name = "updateTab", value = "更新页查询", required = false) @RequestParam(name = "updateTab", defaultValue="") String updateTab,
            Pageable pageable) {
        return workMulitObjectService.findByObjectIdAndFileName(objectId, fileName, collectionTab, browsingTab, updateTab, pageable);
    }

    /**
     * 同名文件查询
     * @param objectId 研究对象id
     * @param fileName 文件名称
     * @return 文件信息
     */
    @RequestMapping(value = "/getSameFile", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取同名文件", notes = "", code =200, produces="application/json")
    public ApiResult getSameFile(
            @ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "fileName", value = "文件名称", required = true) @RequestParam(name = "fileName", defaultValue="") String fileName) {
        WorkTaskFileUpload sameFile = workMulitObjectService.findFileByObjIdAndFn(objectId, fileName);
        if (sameFile != null){
            return ApiResult.ofSuccessResultMsg(sameFile, "存在同名文件");
        }
        return ApiResult.ofSuccessResultMsg(null, "不存在同名文件");
    }

    //文件收藏
    @RequestMapping(value = "/saveFileCollection", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "保存收藏", notes = "", code =200,produces="application/json")
    public ApiResult saveFileCollection(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                        @ApiParam(name = "fileName", value = "文件名称", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName) {
        WorkTaskFileUploadRecord wr = workMulitObjectService.saveFileCollection(objectId, fileName);
        if (wr!= null) {
            return ApiResult.ofSuccessResultMsg(wr, "收藏成功");
        } else {
            return ApiResult.ofFailureResultMsg(null,"收藏失败");
        }
    }

    //取消收藏
    @RequestMapping(value = "/deleteFileCollection", method = RequestMethod.GET)
    @ResponseBody
    public ApiResult deleteFileCollection(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                          @ApiParam(name = "fileId", value = "文件id", required = false) @RequestParam(name = "fileId", defaultValue="") String fileId,
                                          @ApiParam(name = "fileName", value = "文件名称", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName) {
        String adjacentFile = "0";
        WorkTaskFileUploadRecord wr = workMulitObjectService.deleteFileCollection(objectId, fileId, fileName, adjacentFile);
        if (wr!= null) {
            return ApiResult.ofSuccessResultMsg(wr, "取消收藏成功");
        } else {
            return ApiResult.ofFailureResultMsg(null,"取消收藏失败");
        }
    }

    //浏览记录
    @RequestMapping(value = "/saveFileBrowsing", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "保存浏览记录", notes = "", code =200,produces="application/json")
    public ApiResult saveFileBrowsing(@ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                      @ApiParam(name = "fileName", value = "文件名称", required = true) @RequestParam(name = "fileName", defaultValue="") String fileName,
                                      @ApiParam(name = "onFileId", value = "批注文件id", required = false) @RequestParam(name = "onFileId", defaultValue="") String onFileId) {
        WorkTaskFileUploadRecord wr = workMulitObjectService.saveFileBrowsing(objectId, fileName, onFileId);
        if (wr.getRecordFlag() == null) {
            return ApiResult.ofSuccessResultMsg(wr, "浏览记录已存在");
        } else if (wr.getRecordFlag().equals("b")) {
            return ApiResult.ofSuccessResultMsg(wr, "浏览记录已生成");
        } else {
            return ApiResult.ofFailureResultMsg(null,"浏览记录生成失败");
        }
    }


    /**
     * 获取地质资料文件列表
     * @param objectId 研究对象id
     * @return 文件数据
     */
    @RequestMapping(value = "/getFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取文件列表", notes = "", code =200, produces="application/json")
    public List<WorkTaskFileUpload> getFileList(
            @ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId) {
        return workMulitObjectService.getFileList(objectId);
    }

    /**
     * 获取更新文件列表
     * @param objectId 研究对象id
     * @param pageable 分页
     * @return 文件数据
     */
    @RequestMapping(value = "/getUpdateFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取更新文件列表", notes = "", code =200, produces="application/json")
    public Page<WorkTaskFileUpload> getUpdateFileList(
            @ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            Pageable pageable) {
        String fileName = "";
        return workMulitObjectService.getUpdateFileList(objectId, fileName, pageable);
    }

    //获取收藏列表
    @RequestMapping(value = "/getCollectionFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取收藏文件列表", notes = "", code =200, produces="application/json")
    public Page<WorkTaskFileUpload> getCollectionFileList(
            @ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            Pageable pageable) {
        String fileName = "";
        return workMulitObjectService.getCollectionFileList(objectId, fileName, pageable);
    }

    //获取浏览列表
    @RequestMapping(value = "/getBrowsingFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取浏览文件列表", notes = "", code =200, produces="application/json")
    public Page<WorkTaskFileUpload> getBrowsingFileList(
            @ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
            Pageable pageable) {
        String fileName = "";
        return workMulitObjectService.getBrowsingFileList(objectId, fileName, pageable);
    }

    /**
     * 邻井资料
     */
    @RequestMapping(value = "/getAdjacentFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取邻井文件列表", notes = "", code =200, produces="application/json")
    public List getAdjacentFileList(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId) throws Exception {
        String fileName = "";
        return workMulitObjectService.getAdjacentFileList(objectId, fileName);
    }

    //邻井资料文件查询
    @RequestMapping(value = "/searchAdjacentFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询获取邻井文件列表", notes = "", code =200, produces="application/json")
    public List searchAdjacentFileList(
            @ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
            @ApiParam(name = "fileName", value = "文件名称", required = true) @RequestParam(name = "fileName", defaultValue="") String fileName,
            @ApiParam(name = "collectionTab", value = "收藏页查询", required = false) @RequestParam(name = "collectionTab", defaultValue="") String collectionTab,
            @ApiParam(name = "browsingTab", value = "浏览记录页查询", required = false) @RequestParam(name = "browsingTab", defaultValue="") String browsingTab) throws Exception {
        return workMulitObjectService.searchAdjacentFileList(objectId, fileName, collectionTab, browsingTab);
    }

    //收藏邻井资料
    @RequestMapping(value = "/saveAdjacentFileCollection", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "保存收藏", notes = "", code =200,produces="application/json")
    public ApiResult saveAdjacentFileCollection(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                        @ApiParam(name = "fileName", value = "文件名称", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName,
                                        @ApiParam(name = "fileId", value = "文件id", required = false) @RequestParam(name = "fileId", defaultValue="") String fileId,
                                        @ApiParam(name = "fileState", value = "文件状态", required = false) @RequestParam(name = "fileState", defaultValue="") String fileState,
                                        @ApiParam(name = "fileUploadUser", value = "文件上传人", required = false) @RequestParam(name = "fileUploadUser", defaultValue="") String fileUploadUser,
                                        @ApiParam(name = "createDate", value = "创建时间", required = false) @RequestParam(name = "createDate", defaultValue="") String createDate) {
        WorkTaskFileUploadRecord wr = workMulitObjectService.saveAdjacentFileCollection(objectId, fileName, fileId, fileState, fileUploadUser, createDate);
        if (wr!= null) {
            return ApiResult.ofSuccessResultMsg(wr, "收藏成功");
        } else {
            return ApiResult.ofFailureResultMsg(null,"收藏失败");
        }
    }

    //取消收藏邻井资料
    @RequestMapping(value = "/deleteAdjacentFileCollection", method = RequestMethod.GET)
    @ResponseBody
    public ApiResult deleteAdjacentFileCollection(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                          @ApiParam(name = "fileId", value = "文件id", required = false) @RequestParam(name = "fileId", defaultValue="") String fileId,
                                          @ApiParam(name = "fileName", value = "文件名称", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName) {
        String adjacentFile = "1";
        WorkTaskFileUploadRecord wr = workMulitObjectService.deleteFileCollection(objectId, fileId, fileName, adjacentFile);
        if (wr!= null) {
            return ApiResult.ofSuccessResultMsg(wr, "取消收藏成功");
        } else {
            return ApiResult.ofFailureResultMsg(null,"取消收藏失败");
        }
    }

    //获取邻井收藏列表
    @RequestMapping(value = "/getAdjCollectionFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取邻井文件列表", notes = "", code =200, produces="application/json")
    public List getAdjCollectionFileList(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId){
        String fileName = "";
        return workMulitObjectService.getAdjCollectionFileList(objectId, fileName);
    }

    //邻井资料浏览记录
    @RequestMapping(value = "/saveAdjacentFileBrowsing", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "保存邻井资料浏览记录", notes = "", code =200,produces="application/json")
    public ApiResult saveAdjacentFileBrowsing(@ApiParam(name = "objectId", value = "研究对象id", required = true) @RequestParam(name = "objectId", defaultValue="") String objectId,
                                              @ApiParam(name = "fileName", value = "文件名称", required = false) @RequestParam(name = "fileName", defaultValue="") String fileName,
                                              @ApiParam(name = "fileId", value = "文件id", required = false) @RequestParam(name = "fileId", defaultValue="") String fileId,
                                              @ApiParam(name = "fileState", value = "文件状态", required = false) @RequestParam(name = "fileState", defaultValue="") String fileState,
                                              @ApiParam(name = "fileUploadUser", value = "文件上传人", required = false) @RequestParam(name = "fileUploadUser", defaultValue="") String fileUploadUser,
                                              @ApiParam(name = "createDate", value = "创建时间", required = false) @RequestParam(name = "createDate", defaultValue="") String createDate) {
        WorkTaskFileUploadRecord wr = workMulitObjectService.saveAdjacentFileBrowsing(objectId, fileName, fileId, fileState, fileUploadUser, createDate);
        if (wr.getRecordFlag() == null) {
            return ApiResult.ofSuccessResultMsg(wr, "浏览记录已存在");
        } else if (wr.getRecordFlag().equals("b")) {
            return ApiResult.ofSuccessResultMsg(wr, "浏览记录已生成");
        } else {
            return ApiResult.ofFailureResultMsg(null,"浏览记录生成失败");
        }
    }

    //获取邻井浏览列表
    @RequestMapping(value = "/getAdjBrowsingFileList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取邻井文件浏览列表", notes = "", code =200, produces="application/json")
    public List getAdjBrowsingFileList(@ApiParam(name = "objectId", value = "研究对象id", required = false) @RequestParam(name = "objectId", defaultValue="") String objectId){
        String fileName = "";
        return workMulitObjectService.getAdjBrowsingFileList(objectId, fileName);
    }
}
