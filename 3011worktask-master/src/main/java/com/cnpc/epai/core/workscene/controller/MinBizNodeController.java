package com.cnpc.epai.core.workscene.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.service.MinBizNodeService;
import com.cnpc.epai.core.workscene.service.TreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "最小业务单元分组")
@RestController
@RequestMapping("/core/worktask/minBizNode")
public class MinBizNodeController {

    @Autowired
    private MinBizNodeService minBizNodeService;

    @ApiOperation(value = "添加最小业务单元节点")
    @GetMapping("/addMinBizNode")
    public Object addMinBizNode(@RequestParam(name = "bizNodeName") String bizNodeName) {
        return minBizNodeService.addMinBizNode(bizNodeName);
    }

    @ApiOperation(value = "获取所有最小业务单元节点")
    @GetMapping("/allMinBizNode")
    public Object allMinBizNode(Boolean isAll) {
        return minBizNodeService.allMinBizNode(isAll);
    }

    @ApiOperation(value = "查询最小业务单元节点")
    @GetMapping("/searchNode")
    public Object searchNode(String bizNodeName) {
        return minBizNodeService.searchNode(bizNodeName);
    }

    @ApiOperation(value = "最小业务单元节点完成配置")
    @GetMapping("/completeConfig")
    public Object completeConfig(@RequestParam(name = "bizNodeId") String bizNodeId) {
        return minBizNodeService.completeConfig(bizNodeId);
    }

    @ApiOperation(value = "更新最小业务单元节点")
    @GetMapping("/updateNode")
    public Object updateNode(String treeId, String bizNodeId, String type, String bizNodeName) {
        return minBizNodeService.updateNode(treeId, bizNodeId, type, bizNodeName);
    }

    @ApiOperation(value = "同步最小业务单元节点")
    @PostMapping("/syncBizNode")
    public Object syncBizNode(@RequestBody Map<String, Object> data) {
        return minBizNodeService.syncBizNode(data);
    }


}
