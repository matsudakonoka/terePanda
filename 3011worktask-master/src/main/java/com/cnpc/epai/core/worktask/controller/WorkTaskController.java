package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.template.service.TreeCondiDto;
import com.cnpc.epai.common.template.service.TreeJsonDto;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.domain.WorkTask;
import com.cnpc.epai.core.worktask.domain.WorkTaskBusinessNode;
import com.cnpc.epai.core.worktask.domain.WorkTaskLog;
import com.cnpc.epai.core.worktask.service.WorkTaskService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: controller
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */

@RestController
@RequestMapping("/core/worktask")
public class WorkTaskController {
    @Autowired
    WorkTaskService workTaskService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取工作任务列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public Page<WorkTask> findAll(Pageable page,
                                  @ApiParam(name = "condition", value = "Map格式的查询条件", required = false)
                                  @RequestParam( name = "condition",required = false) String condition) {
        return workTaskService.findByBsflag(page,condition);
    }
    @RequestMapping(value = "/level2/", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取二级工作任务列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public List<WorkTask> findAll(@ApiParam(name = "condition", value = "Map格式的查询条件", required = false)
                                  @RequestParam( name = "condition",required = false) String condition) {
        return workTaskService.findByLevel2List(condition);
    }
    @RequestMapping(value = "/personal", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取个人工作任务列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public Page<WorkTask> findPersonTask(Pageable page,
                                  @ApiParam(name = "condition", value = "Map格式的查询条件", required = false)
                                  @RequestParam( name = "condition",required = false) String condition) {
        return workTaskService.findPersonTask(page,condition);
    }
    
    @RequestMapping(value = "/personal", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取个人工作任务列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public Page<WorkTask> findPersonTaskPost(Pageable page,
                                  @ApiParam(name = "condition", value = "Map格式的查询条件", required = false) @RequestBody String condition) {
        return workTaskService.findPersonTask(page,condition);
    }
    
    @RequestMapping(value = "/{projectId}/getByProject", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据项目ID查询任务列表", notes = "请填写项目ID", code = 200, produces = "application/json")
    public Page<WorkTask> findByProject(@ApiParam(name = "projectId", value = "项目名称", required = true) @PathVariable("projectId") String workroomId,
                                        @ApiParam(name = "status", value = "逗号分隔的所有状态类型", required = false) @RequestParam (required = false)String status,
                                        Pageable page) {
        return workTaskService.findByProject(workroomId, status,page);
    }

    @RequestMapping(value = "/{id}/getworktask", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据任务ID查询任务", notes = "请填写任务ID", code = 200, produces = "application/json")
    public WorkTask findById(@ApiParam(name = "id", value = "任务id", required = true) @PathVariable("id") String id) {
        return workTaskService.findById(id);
    }


    @RequestMapping(produces = "application/json", value = "/", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存任务", notes = "保存任务", code = 200, produces = "application/json")
    public WorkTask saveWorkTask(WorkTask workTask,
                                 @RequestParam String userIds,
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
                                         "}]") @RequestParam(name = "nodes", required = false) String nodes,
                                 @RequestParam( name = "toolIds",required = false)String toolIds,
                                 @RequestParam( name = "softwareIds",required = false)String softwareIds) throws BusinessException {
        if (StringUtils.isEmpty(userIds)) {
            throw new BusinessException("-1","任务需要有指派人员。");
        }
        return workTaskService.createWorktask(workTask, userIds,nodes,toolIds,softwareIds);
    }

    @RequestMapping(value = "/{projectId}/{userId}/getByProjectAndUser", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询任务列表", notes = "查询任务列表", code = 200, produces = "application/json")
    public Page<WorkTask> findByProjectIdAndUserId(@PathVariable("projectId")String projectId,
                                                         @PathVariable("userId")String userId,
                                                         @ApiParam(name = "status", value = "逗号分隔的所有状态类型", required = false) @RequestParam String status,
                                                         Pageable page){
        return workTaskService.findByProjectIdAndUserId(projectId,userId,status,page);
    }


    @RequestMapping(value = "/{workTaskId}/answerAssignment",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "响应指派任务", notes = "响应指派任务", code = 200, produces = "application/json")
    public String answerAssignment(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String workTaskId,
                                   @ApiParam(name = "oprType", value = "操作类型 pass/refuse", required = true) @RequestParam String oprType,
                                   @ApiParam(name = "oprContent", value = "操作内容(如果拒绝，填写相关原因)", required = false) @RequestParam String oprContent) throws BusinessException {
        return workTaskService.answerAssignment(workTaskId,oprType,oprContent);
    }

    @RequestMapping(value = "/{workTaskId}/submitTask",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "提交任务", notes = "提交任务", code = 200, produces = "application/json")
    public String submitTask(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String workTaskId,
                             @ApiParam(name = "treeDataIds", value = "逗号分隔的成果树数据ID", required = true) @RequestParam String treeDataIds) throws BusinessException {
        return workTaskService.submitTask(workTaskId,treeDataIds);
    }

    @RequestMapping(value = "/{workTaskId}/reminders",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "催办", notes = "催办", code = 200, produces = "application/json")
    public String reminders(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String workTaskId,
                            @ApiParam(name = "endDate", value = "任务终止日期", required = false) @RequestParam Date endDate) throws BusinessException {
        return workTaskService.reminders(workTaskId,endDate);
    }

    @RequestMapping(value = "/{workTaskId}/delete",produces = "application/json",  method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "删除任务", notes = "删除任务", code = 200, produces = "application/json")
    public String deleteTask(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String taskId) throws BusinessException {
        return workTaskService.deleteTask(taskId);
    }

    @RequestMapping(value = "/{workTaskId}/stop",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "终止任务", notes = "终止任务", code = 200, produces = "application/json")
    public String stopTask(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String taskId) throws BusinessException {
        return workTaskService.taskOver(taskId);
    }

    @RequestMapping(value = "/{workTaskId}/delay",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "延期任务", notes = "延期任务", code = 200, produces = "application/json")
    public String delay(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId")String taskId,
                        @ApiParam(name = "endDate", value = "任务延期日期", required = false) @RequestParam Date endDate) throws BusinessException {
        return workTaskService.delay(taskId,endDate);
    }


    @RequestMapping(value = "/{workTaskId}/getLogs", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "根据任务ID操作日志", notes = "根据任务ID操作日志", code = 200, produces = "application/json")
    public List<WorkTaskLog> findById(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId") String id,
                                      @ApiParam(name = "userId", value = "用户id", required = false)@RequestParam(required = false) String userId,
                                      @ApiParam(name = "status", value = "状态(1代表指派，2代表接受指派，3代表拒绝指派，4代表提交成果，5代表审核通过，6代表审核不通过，7代表修改，8代表删除，9代表催办 10延期)", required = false)@RequestParam(required = false) String status) throws BusinessException {
        return workTaskService.findWorkTaskLog(id, userId, status);
    }


    @RequestMapping(value = "/reboot",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "重启任务", notes = "重启任务", code = 200, produces = "application/json")
    public String rebootWorkTask(WorkTask workTask, @RequestParam String userIds) throws BusinessException {
        if (StringUtils.isEmpty(userIds)) {
            throw new BusinessException("-1","任务需要有指派人员。");
        }
        return workTaskService.rebootWorkTask(workTask, userIds);
    }

    @RequestMapping(value = "/{workTaskId}/audit", produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "审核任务", notes = "审核任务", code = 200, produces = "application/json")
    public String audit(@ApiParam(name = "workTaskId", value = "任务id", required = true) @PathVariable("workTaskId") String workTaskId,
                        @ApiParam(name = "oprType", value = "操作类型 pass/refuse", required = true) @RequestParam String oprType) throws BusinessException {
        return workTaskService.audit(workTaskId, oprType);
    }

    @RequestMapping(value = "/{projectId}/taskProgressStatistics", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "任务进展统计", notes = "任务进展统计", code = 200, produces = "application/json")
    public Map<String, List> taskProgressStatistics(@PathVariable("projectId") String projectId){
        return workTaskService.taskProgressStatisticalByUser(projectId);
    }

    @RequestMapping(value = "/{projectId}/taskStatisticsByUser", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "人员任务统计", notes = "人员任务统计", code = 200, produces = "application/json")
    public List<Object>  taskStatisticsByUser(@PathVariable("projectId") String projectId){
        return workTaskService.taskProgressStatisticalByCurrentState(projectId);
    }
    
    @RequestMapping(value = "/{projectId}/getTaskCount", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(hidden = true, value = "根据项目ID查询不同状态任务的数量", notes = "", code = 200, produces = "application/json")
    public Map<String, Object> findCountByProjectId (@ApiParam(name = "projectId", value = "项目名ID", required = true) @PathVariable("projectId") String projectId){
    	return workTaskService.findCountByProjectId(projectId);
    }


    @RequestMapping(value = "/{id}/getTool", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询当前项目的常用工具", notes = "项目id", code = 200, produces = "application/json")
    public List<TreeJsonDto> getTool(@ApiParam(name = "id", value = "任务id") @PathVariable("id") String id) {
        return workTaskService.getToolFullTree(id);
    }

    @RequestMapping(value = "/{id}/getDataset", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询当前项目的常用工具", notes = "项目id", code = 200, produces = "application/json")
    public List<SrMetaDataset> getDataset(@ApiParam(name = "id", value = "任务id") @PathVariable("id") String id) {
        return workTaskService.getDataset(id);
    }


    @RequestMapping(value = "/{id}/getApplicationLeaf", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询当前任务的应用软件", notes = "任务id", code = 200, produces = "application/json")
    public List<Map<String, Object>> getProjectApplicationLeaf(@ApiParam(name = "id", value = "编号", required = true) @PathVariable("id") String id,
                                                               @RequestParam(required = false) String eoCode,
                                                               @ApiParam(name = "isDeskTop", value = "是否显示软件云桌面", required = false) @RequestParam(required = false) String isDeskTop) {
        boolean isDeskTopShow = false;
        if (StringUtils.isEmpty(isDeskTop) || ("N").equals(isDeskTop)) {
            isDeskTopShow = false;
        } else {
            isDeskTopShow = true;
        }
        return workTaskService.getWorkTaskApplicationLeaf(id, eoCode, "N", isDeskTopShow);
    }

    @RequestMapping(value = "/{id}/getApplication", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询当前任务的应用软件", notes = "任务id", code = 200, produces = "application/json")
    public List getWorkTaskApplication(@ApiParam(name = "id", value = "编号", required = true) @PathVariable("id") String id,
                                                   @RequestParam(required = false) String eoCode,
                                                           @ApiParam(name = "isDeskTop", value = "是否显示软件云桌面", required = false) @RequestParam(required = false) String isDeskTop) {
        boolean isDeskTopShow = false;
        if (StringUtils.isEmpty(isDeskTop) || ("N").equals(isDeskTop)) {
            isDeskTopShow = false;
        } else {
            isDeskTopShow = true;
        }
        return workTaskService.getWorkTaskApplicationTree(id,eoCode,"N",isDeskTopShow);
    }

    /**
     * 获取业务节点与数据集树
     *
     * @param treeCategoryPamra
     * @return
     */
    @RequestMapping(value = "/fullTree", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "树节点", notes = "无需添加参数", code = 200, produces = "application/json")
    public List<TreeJsonDto> fullTree(@RequestParam(value = "categoryObj", required = false) TreeCondiDto[] treeCategoryPamra,
                                      @ApiParam(name = "taskId", value = "任务id", required = false)@RequestParam(value = "taskId", required = false) String taskId,
                                      @ApiParam(name = "dataRegion", value = "dataRegion", required = false)@RequestParam(value = "dataRegion", required = false) String dataRegion) {
        return workTaskService.getBusinessNodeFullTree(treeCategoryPamra,taskId, dataRegion);
    }


    @RequestMapping(value = "/{id}/getByName", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "按照名称查询业务节点", notes = "业务节点名称", code = 200, produces = "application/json")
    public List<WorkTaskBusinessNode> getNodesByName(@ApiParam(name = "id", value = "任务id", required = true) @PathVariable("id") String id,
                                                     @ApiParam(name = "name", value = "业务节点名称") @RequestParam String name,
                                                     @ApiParam(name = "type", value = "查询类型") @RequestParam String type){
        return workTaskService.getNodesByName(id,name,type);
    }

    @RequestMapping(value = "/sceneWorktask/",produces = "application/json",  method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "创建场景任务", notes = "创建场景任务", code = 200, produces = "application/json")
    public WorkTask createSceneWorktask(@ApiParam(name = "projectId", value = "项目id") @RequestParam String projectId,
                                      @ApiParam(name = "name", value = "任务名称") @RequestParam String name,
                                      @ApiParam(name = "startDate", value = "任务开始时间") @RequestParam Date startDate,
                                      @ApiParam(name = "endDate", value = "任务结束时间") @RequestParam Date endDate,
                                      @ApiParam(name = "planId", value = "一级任务ID", required = false) @RequestParam(value = "planId", required = false, defaultValue = "") String planId,
                                      @ApiParam(name = "userIds", value = "执行人ids") @RequestParam String userIds) throws BusinessException {
        WorkTask workTask = new WorkTask();
        workTask.setTaskName(name);
        workTask.setStartDate(startDate);
        workTask.setEndDate(endDate);
        workTask.setPlanId(planId);
        workTask.setTaskType("SCENE");
        workTask.setProjectId(projectId);
        userIds +=","+ SpringManager.getCurrentUser().getUserId();
        return workTaskService.createSceneWorktask(workTask, userIds);
    }

    @RequestMapping(value = "/renameBusinessNode", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更改业务节点名称", notes = "更改业务节点名称", code = 200, produces = "application/json")
    public String renameBusinessNode(@ApiParam(name = "name", value = "业务节点名称") @RequestParam String name,
                                     @ApiParam(name = "id", value = "业务节点id") @RequestParam String id){
        return workTaskService.renameBusinessNode(id,name);
    }

    @RequestMapping(value = "/{id}/getdatasetTree", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取任务的所有数据集结构", notes = "填写相关信息", code = 200, produces = "application/json")
    public List<SrMetaDataset> getDatasetTree(@PathVariable("id") String taskId,
                                              @ApiParam(name = "dataRegion", value = "dataRegion", required = false)@RequestParam(value = "dataRegion", required = false) String dataRegion){
        return workTaskService.getDatasetTree(taskId, dataRegion);
    }


    @RequestMapping(value = "/countWorkTask", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "统计项目下的所有任务", notes = "请填写项目ID", code = 200, produces = "application/json")
    public Map findByProject(@ApiParam(name = "projectIds", value = "项目名称", required = true) @RequestParam("projectIds") String workroomIds) {
        return workTaskService.countWorkTask(workroomIds);
    }
    

    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/scenetask/{taskId}/syncdata", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "同步场景任务数据并生成导航树", notes = "", code = 200, produces = "application/json")
    public String scenetaskSync(
    		@ApiParam(name = "taskId", value = "项目任务ID", required = true) @PathVariable("taskId") String taskId,
    		@ApiParam(name = "jsonParam", value = "数据格式", required = true) @RequestBody JSONObject jsonParam) throws Exception {
    	return workTaskService.sceneTaskSync(taskId, jsonParam);
    }

    @RequestMapping(value = "/{taskId}/saveFullTreeDataByTask", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "树结构保存", notes = "", code = 200, produces = "application/json")
    public String saveFullTreeDataByTask(
            @ApiParam(name = "taskId", value = "项目任务ID", required = true) @PathVariable("taskId") String taskId,
            @ApiParam(name = "dataRegion", value = "dataRegion") @RequestParam(value = "dataRegion", required = false) String dataRegion,
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
                    "}]") @RequestParam String nodes) throws Exception {
        return workTaskService.saveFullTreeDataByTask(taskId, dataRegion,nodes);
    }
}
