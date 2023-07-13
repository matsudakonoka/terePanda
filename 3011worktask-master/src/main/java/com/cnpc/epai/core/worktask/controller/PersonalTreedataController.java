package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.worktask.service.PersonalTreedataService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: controller
 * @author 王博
 * @version 1.0.0
 * @date  2022/4/13
 */
@RestController
@RequestMapping("/core/worktask")
public class PersonalTreedataController {

    @Autowired
    private PersonalTreedataService personalTreedataService;

    @RequestMapping(value = "/saveTreeData", method = RequestMethod.POST)
    @ApiOperation(value = "保存数据集导航",notes = "参数：[{},{}]",code=200,produces="application/json")
    public ApiResult saveTreeData(@ApiParam(value = "传参json", required = true) @RequestBody JSONArray data) {
        boolean boo = personalTreedataService.saveTreeData(data);
        if (boo){
            return ApiResult.ofSuccess();
        }else {
            return ApiResult.ofFailure();
        }
    }

    @RequestMapping(value = "/getTreeData", method = RequestMethod.GET)
    @ApiOperation(value = "获取数据集导航",notes = "参数：[{},{}]",code=200,produces="application/json")
    public JSONArray getTreeData() {
        JSONArray array = personalTreedataService.getTreeData();
        return array;
    }

    @RequestMapping(value = "/matchNodesDatasets", method = RequestMethod.POST)
    @ApiOperation(value = "匹配保存所选节点数据资源",notes = "参数：[{},{}]",code=200,produces="application/json")
    public ApiResult matchNodeDatasets(@ApiParam(value = "传参json", required = true) @RequestBody JSONArray data, HttpServletRequest request) {
        boolean boo = personalTreedataService.matchNodeDataset(data,request);
        if (boo){
            return ApiResult.ofSuccess();
        }else {
            return ApiResult.ofFailure();
        }
    }


    @RequestMapping(value = "/wellDatasetData", method = RequestMethod.GET)
    @ApiOperation(value = "单井数据集详细数据展示",notes = "参数：[{},{}]",code=200,produces="application/json")
    public ApiResult wellDatasetData(@ApiParam(name = "datasetId", value = "数据集ID", required = true) @RequestParam(name = "datasetId", defaultValue="") String datasetId,
                                     @ApiParam(name = "wellNames", value = "井名", required = true) @RequestParam(name = "wellNames", defaultValue="") JSONArray wellNames,
                                     HttpServletRequest request,Pageable pageable) {
        JSONObject jsonObject = personalTreedataService.wellDatasetData(datasetId,wellNames,request,pageable);
        return ApiResult.ofSuccessResult(jsonObject);
    }

}
