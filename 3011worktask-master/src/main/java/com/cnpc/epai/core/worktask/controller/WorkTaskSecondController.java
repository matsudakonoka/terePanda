package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.worktask.pojo.SrTaskMgr;
import com.cnpc.epai.core.worktask.service.WorkTaskSecondService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理二期服务
 */
@RestController
@RequestMapping("/core/worktask/second")
public class WorkTaskSecondController {

    @Autowired
    WorkTaskSecondService taskSecondService;


    @RequestMapping(produces = "application/json", value = "/", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存任务", notes = "保存任务", code = 200, produces = "application/json")
    public SrTaskMgr saveWorkTask(SrTaskMgr workTask,
                                  @RequestParam String userIds,
//                                  @RequestParam( name = "sceneClass",required = false)String sceneClass,
                                  @ApiParam(name = "nodes", value = "[{\n" +
                                          "\t\"id\": 'ywnFz590yxmTWpF4j9hFHHOYATHBKyRs',\n" +
                                          "\t\"children\": [{\n" +
                                          "\t\t\"id\": '7Deb4rNbdYlQ62QJNDh9fPrUf31VzNbC',\n" +
                                          "\t\t\"children\": [],\n" +
                                          "\t\t\"dataSetList\": [{\n" +
                                          "\t\t\t\"id\": \"1CXKVhkiQ34DecMRJ2aCV3qmU7SkEB5P\"\n" +
                                          "\t\t}]\n" +
                                          "\t}],\n" +
                                          "\t\"dataSetList\": [{\n" +
                                          "\t\t\"id\": \"MYLm39VlDOxrTaAiYzds2Nhz39FG2R4y\"\n" +
                                          "\t}]\n" +
                                          "}]")
                                  @RequestParam(name = "nodes", required = false) String nodes,
                                  @RequestParam( name = "toolIds",required = false)String toolIds,
                                  @RequestParam( name = "softwareIds",required = false)String softwareIds) throws BusinessException {
        if (StringUtils.isEmpty(userIds)) {
            throw new BusinessException("-1","任务需要有指派人员。");
        }
        return taskSecondService.createWorktask(workTask, userIds,nodes,toolIds,softwareIds);
    }

    @RequestMapping(produces = "application/json", value = "/scene", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存场景任务", notes = "保存场景任务", code = 200, produces = "application/json")
    public SrTaskMgr saveSceneWorkTask(
            @ApiParam(name = "nodes", value = "{\n" +
                    "    \"workTask\":{\n" +
                    "        \"taskId\":\"bdxah6n23rhbf99duxd3o327\",\n" +
                    "        \"workroomId\":\"ACTIJD100001976\",\n" +
                    "        \"taskName\":\"测试041801\",\n" +
                    "        \"currentState\":null,\n" +
                    "        \"startDate\":\"2022-04-18 19:55:46\",\n" +
                    "        \"endDate\":\"2022-04-18 19:56:46\",\n" +
                    "        \"bsflag\":\"N\",\n" +
                    "        \"deleteDate\":null,\n" +
                    "        \"remarks\":\"测试0418\",\n" +
                    "        \"createUser\":\"n8ON3ACANcUpKsVBeD1uknDhSN6jneVN\",\n" +
                    "        \"createDate\":\"2022-04-18 15:07:11\",\n" +
                    "        \"updateUser\":\"\",\n" +
                    "        \"updateDate\":\"\",\n" +
                    "        \"taskType\":\"SCENE\",\n" +
                    "        \"planId\":null\n" +
                    "    },\n" +
                    "    \"userIds\":\"n8ON3ACANcUpKsVBeD1uknDhSN6jneVN\",\n" +
                    "    \"toolIds\":null,\n" +
                    "    \"softwareIds\":null,\n" +
                    "    \"objectTrees\":null,\n" +
                    "    \"sceneIds\":\"z1p896pcctkkawi4cx50hz4l\",\n" +
                    "    \"businessNodes\":[\n" +
                    "        Object{...}\n" +
                    "    ],\n" +
                    "    \"managementNodes\":[\n" +
                    "        Object{...}\n" +
                    "    ]\n" +
                    "}")
            @RequestBody JSONObject param
    ) throws BusinessException {
        return taskSecondService.createSceneWorktask(param);
    }


    @RequestMapping(value = "/taskscene", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询任务预置场景列表", notes = "" )
    public ApiResult getPresetSceneList(
            @ApiParam(name = "workroomId", value = "工作室ID") @RequestParam(required = true) String workroomId,
            @ApiParam(name = "taskId", value = "任务ID") @RequestParam(required = true) String taskId
    ){
        return ApiResult.ofSuccessResult( taskSecondService.getPresetSceneList(workroomId,taskId));
    }

    @RequestMapping(value = "/{taskId}/submitTask",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "提交任务", notes = "提交任务", code = 200, produces = "application/json")
    public String submitTask(@ApiParam(name = "taskId", value = "任务id", required = true) @PathVariable("taskId")String taskId,
                             @ApiParam(name = "fileIds", value = "逗号分割的关联文件ID", required = true) @RequestParam String fileIds
    ) throws BusinessException {
        return taskSecondService.submitTask(taskId,fileIds);
    }



//    @RequestMapping(produces = "application/json", value = "/scene", method = RequestMethod.POST)
//    @ResponseBody
//    @ApiOperation(value = "保存场景任务", notes = "保存场景任务", code = 200, produces = "application/json")
//    public SrTaskMgr saveSceneTask(SrTaskMgr task
//    ) throws BusinessException {
//        return taskSecondService.saveSceneWorktask(task);
//    }

}
