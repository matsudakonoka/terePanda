package com.cnpc.epai.core.workscene.controller;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.workscene.service.DataService;
import com.cnpc.epai.core.workscene.service.DataTransportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Api(tags = "数据传输分组")
@RestController
@RequestMapping("/core/worktask/transport")
public class DataTransportController {

    @Autowired
    DataTransportService dataTransportService;

    @Autowired
    DataService dataService;


    @ApiOperation(value = "广播索引")
    @PostMapping("/broadcast")
    public Boolean broadcast(@RequestBody JSONObject indexData) {
        Boolean success;
        try {
            dataTransportService.broadcast(indexData);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    @ApiOperation(value = "获取索引")
    @PostMapping("/getIndex")
    public Object getIndex() {
        return dataTransportService.getIndex();
    }

    @ApiOperation(value = "获取索引详细数据")
    @PostMapping("/getIndexData")
    public Object getIndexData(@RequestBody(required = false) JSONObject indexData) {
        return dataTransportService.getIndexData(indexData);
    }


    @ApiOperation(value = "推送数据到专业软件接口")
    @PostMapping("/sendAsi")
    public Object sendAsi(@ApiParam(value = "目标卫星端id，多个以,分割", required = true) @RequestParam(required = true)String satelliteIdTarget,
                          @ApiParam(value = "目标卫星端类型", required = true) @RequestParam(required = true)String sourceTarget,
                          @ApiParam(value = "物理地址", required = true) @RequestParam(required = true)String localMac,
                          @ApiParam(value = "工区", required = true) @RequestParam(required = true)String projectNameTarget,
                          @ApiParam(value = "数据库code", required = true) @RequestParam(required = true)String dataSourceTarget,
                          @ApiParam(value = "索引数据", required = true)@RequestBody(required = false) JSONObject indexData) {
        JSONObject result = dataTransportService.sendAsi(satelliteIdTarget, sourceTarget, localMac,projectNameTarget,dataSourceTarget,indexData);
        if(result.values().contains(false)){
            return ApiResult.ofFailureResult(result);
        }else{
            return ApiResult.ofSuccessResult(result);
        }
    }

    @RequestMapping(value = "/saveObjectFromAsi", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "专业软件数据引用", notes = "将要引用的数据用3013查询出来，然后进行数据引用",
            code = 200, produces = "application/json")
    public ApiResult saveSatelliteDataMap(
            @ApiParam(value = "业务ID", required = true)
            @RequestParam(required = true) String groupID,
            @ApiParam(value = "数据类型ID,来自数据集关联的eoCode", required = true)
            @RequestParam(required = true) String eoCode,
            @ApiParam(value = "数据集id", required = true)
            @RequestParam(required = true) String datasetId,
            @ApiParam(value = "数据目标卫星端ID 必填", required = true)
            @RequestParam(required = true) String satelliteIdTarget,
            @ApiParam(value = "账户配置ID", required = true)
            @RequestParam(required = true) String dataSourceCodeTarget,
            @ApiParam(value = "工区名称 必填", required = true)
            @RequestParam(required = true) String projectNameTarget,
            @ApiParam(value = "数据ID列表", required = true)
            @RequestBody(required = true) List<String> dataIdTargets,
            @ApiParam(value = "对象id", required = true)
            @RequestParam(required = true) String objectId,
            @ApiParam(value = "对象名称", required = true)
            @RequestParam(required = true) String objectName,
            @ApiParam(value = "数据集名称", required = true)
            @RequestParam(required = true) String dataSetName,
            @ApiParam(value = "工作id", required = true)
            @RequestParam(required = true) String workId,
            @ApiParam(value = "节点id", required = true)
            @RequestParam(required = true) String nodeId,
            @ApiParam(value = "节点名称", required = true)
            @RequestParam(required = true) String nodeNames,
            @ApiParam(value = "任务id", required = true)
            @RequestParam(required = true) String taskId,
            @ApiParam(value = "数据类型", required = true)
            @RequestParam(required = false) String dataTargetType,
            @ApiParam(value = "数据所属", required = false)//研究院，工程院
            @RequestParam(required = false) String dataBelong
    ) throws BusinessException {
        String token = TokenUtil.getToken();
        String result = "";
        try {
            result = dataTransportService.saveObjectFromAsi(groupID, eoCode, datasetId, satelliteIdTarget, dataSourceCodeTarget,
                    projectNameTarget, dataIdTargets, objectId, objectName,
                    dataSetName, workId, nodeId, nodeNames, taskId, dataTargetType,dataBelong,token);
            return ApiResult.ofSuccessResult(result);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.ofSuccessResult("保存失败");
        }
    }

    @ApiOperation(value = "广播来自组件的数据")
    @PostMapping("/broadCastFromTool")
    public Object broadCastFromTool(@RequestBody JSONObject data, HttpServletRequest httpServletRequest) throws IOException {
        return dataTransportService.broadCastFromTool(data.getString("associateId"),
                data.getString("attentionName"),data.getJSONArray("dataSets"),
                data.getString("projectId"),httpServletRequest);
    }

    @ApiOperation(value = "广播来自方案设计的数据")
    @PostMapping("/broadCastFromSchDesign")
    public Object broadCastFromSchDesign(@ApiParam(value = "井名", required = true) @RequestParam(required = true) String wellName,
                                         @ApiParam(value = "项目id", required = true) @RequestParam(required = true) String projectId) throws IOException {
        try {
            boolean b = dataTransportService.broadCastFromSchDesign(wellName, projectId);
            return ApiResult.ofSuccessResult(b);
        }catch (Exception e){
            ApiResult apiResult = ApiResult.ofFailureResult(e.toString());
            apiResult.setCode("400");
            return apiResult;
        }
    }
}
