package com.cnpc.epai.core.worktask.controller;


import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.worktask.service.ToolDatasetService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/core/worktask/tooldataset")
public class ToolDatasetController {

    @Autowired
    ToolDatasetService toolDatasetService;

    @RequestMapping(value = "/getDatasetByToolId", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据常用工具ID查询绑定数据集列表", notes = "", code =200,produces="application/json")
    public List<Map> getObjectData(
            @ApiParam(name = "toolId", value = "常用工具ID", required = true) @RequestParam(name = "toolId", defaultValue="") String toolId
    ) throws IOException {
        List<Map> object = toolDatasetService.getDatasetByToolId(toolId);
        return object;
    }

    @RequestMapping(value = "/savetooldatasetlist", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存常用工具绑定数据集列表", notes = "", code =200,produces="application/json")
    public boolean saveToolDatasetList(
            @ApiParam(value = "参数列表{toolId:...,toolTypeId:...,name:...,toolPath:...,configParam:...,data:[{datasetId:...,datasetName:...}]}", type="body") @RequestBody JSONObject object,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        try {
            String toolId = (String) object.get("toolId");
            String toolTypeId = (String) object.get("toolTypeId");
            String name = (String) object.get("name");
            String toolPath = (String) object.get("toolPath");
            String configParam = (String) object.get("configParam");
            List<Map> dataList = (List<Map>) object.get("data");

            return toolDatasetService.saveAll(toolId,toolTypeId,name,toolPath,configParam,dataList,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/deletetooldatasetbyid", method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "删除常用工具绑定数据集列表", notes = "", code =200,produces="application/json")
    public boolean deleteToolDatasetbyId(
            @ApiParam(name = "dataIds", value = "工具数据集关联关系Id，多个id用,分割", required = true) @RequestParam(name = "dataIds", defaultValue="") String dataIds
    ) throws IOException {
        try {
            return toolDatasetService.deleteDataByIds(dataIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @RequestMapping(value = "/searchdata", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询数据集数据列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> searchdata(
            @ApiParam(name = "datasetCode", value = "数据集code", required = true) @RequestParam(name = "datasetCode", defaultValue="") String datasetCode,
            @ApiParam(name = "wellName", value = "当前对象名", required = false) @RequestParam(name = "wellName", defaultValue="") String wellName,
            @ApiParam(name = "page", value = "分页页数", required = true) @RequestParam(name = "page", defaultValue="") Integer page,
            @ApiParam(name = "size", value = "每页数量", required = true) @RequestParam(name = "size", defaultValue="") Integer size,
            HttpServletRequest httpServletRequest
    ) throws IOException {
        try {
            return toolDatasetService.searchdata(datasetCode,wellName,page,size,httpServletRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
