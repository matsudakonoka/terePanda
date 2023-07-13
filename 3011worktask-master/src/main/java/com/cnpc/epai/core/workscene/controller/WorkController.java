package com.cnpc.epai.core.workscene.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.entity.Behavior;
import com.cnpc.epai.core.workscene.entity.Geo;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.workscene.pojo.vo.*;
import com.cnpc.epai.core.workscene.service.*;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(tags = "工作创建分组")
@RestController
@RequestMapping("/core/worktask")
public class WorkController {

    @Autowired
    private WorkService workService;

    @Autowired
    private WorkSceneTaskService workTaskService;

    @Autowired
    private GeoService geoService;

    @Autowired
    private WorkNodeService workNodeService;


    @Autowired
    private TreeService treeService;

    @Autowired
    private DataService dataService;

    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;


    @ApiOperation(value = "创建工作, workId 为空创建工作，不为空修改工作")
    @PostMapping("/create")
    public Work create(@RequestBody WorkVo workVo) {
        return workService.create(workVo);
    }
    @ApiOperation(value="T3模糊查询")
    @PostMapping("/searchT3Tree")
    public List<WorkNavigateTreeNode> searchT3Tree(String workId,String nodeType,String nodeName){
        QueryWrapper<WorkNavigateTreeNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tree_id",workId);
//        queryWrapper.eq("node_type","WORKUNIT");
        if(nodeType!=null){
            queryWrapper.like("node_type",nodeType);
        }
        if(nodeName!=null){
            queryWrapper.like("node_name",nodeName);
        }
        List<WorkNavigateTreeNode> workTreeNodelist = workNavigateTreeNodeService.list(queryWrapper);
        return workTreeNodelist;
    }

    @ApiOperation(value = "获取工作")
    @PostMapping("/getWork")
    public JSONObject getWork(String workId) {
        return workService.getWork(workId);
    }

    @ApiOperation(value = "业务管理：配置输出模板")
    @PostMapping("/{workId}/configTemplate")
    public boolean configTemplate(@PathVariable String workId, @RequestBody ReportTemplateVo reportTemplate) {
        return workService.configTemplate(workId, reportTemplate);
    }

    @ApiOperation(value = "共享管理：配置共享设置")
    @PostMapping("/{workId}/configShare")
    public void configShare(@PathVariable String workId, @RequestBody ShareVo share) {
        workService.configShare(workId, share);
    }

    @ApiOperation(value = "数据选取：配置数据对象")
    @PostMapping("/{workId}/configGeoObjects")
    public ApiResult configGeoObjects(@PathVariable String workId, @RequestBody Map[] geos) {
        ApiResult apiResult = ApiResult.newInstance();
        try {
            geoService.configGeoObjects(workId, geos);
            apiResult.setFlag(true);
            apiResult.setMsg("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
            apiResult.setMsg(e.getMessage());
            apiResult.setFlag(false);
        }
        return apiResult;
    }

    @ApiOperation(value = "任务管理：工作指派")
    @PostMapping("/assignWork")
    public boolean assignWork(String workId, @RequestBody AssignVo[] assigns) {
        try {
            workTaskService.assignWork(workId, assigns);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @ApiOperation(value = "获取模板业务树")
    @GetMapping("/{workId}/getTree")
    public Object getTree(@PathVariable String workId, String name, String type, String pNodeId) {
        return workService.getTree(workId, name,type,pNodeId);
    }

    @ApiOperation(value = "获取T3业务树：叶子结点带权限的")
    @GetMapping("/{workId}/getT3Tree")
    public Object getT3Tree(@PathVariable String workId, String name, String pNodeId) {
        return workService.getTree(workId, name,"T3",pNodeId);
    }

    @ApiOperation(value = "任务管理：获取用户业务树")
    @GetMapping("/{workId}/getTreeByUser")
    public Object getTreeByUser(@PathVariable String workId, String name) {
        return workService.getTreeByUser(workId, name);
    }

    @ApiOperation(value = "任务管理：获取业务节点")
    @GetMapping("/getMinWorks")
    public List<JSONObject> getMinWorks(@RequestParam String workId, String userId, String nodeId) {
        return workTaskService.getMinWorks(workId, userId, nodeId);
    }

    @ApiOperation(value = "数据选取：获取地质体对象类型统计结果")
    @GetMapping("/getGeoTypeCount")
    public Object getGeoTypeCount(String workId) {
        return geoService.getGeoTypeCount(workId);
    }

    @ApiOperation(value = "数据选取：获取地质体对象树")
    @GetMapping("/getGeoObject")
    public Object getGeoObject(String workId, String name, String type) {
        return geoService.getGeoObject(workId, name,type);
    }

    @ApiOperation(value = "成果浏览：获取成果列表")
    @GetMapping("/getDocument")
    public Object getDocument(
            @ApiParam(name = "workId", value = "工作ID", required = false) @RequestParam(name = "workId", defaultValue="") String workId,
            @ApiParam(name = "userId", value = "项目成员,多个id用,号分割", required = false) @RequestParam(name = "userId", defaultValue="") String userId,
            @ApiParam(name = "dataStatus", value = "审核状态", required = false) @RequestParam(name = "status", defaultValue="") String status,
            @ApiParam(name = "startTime", value = "开始时间", required = false) @RequestParam(name="startTime", defaultValue="") Date startTime,
            @ApiParam(name = "endTime", value = "结束时间", required = false) @RequestParam(name="endTime", defaultValue="") Date endTime
    ) {
        return workService.getDocument(workId, userId, status, startTime, endTime);
    }

    @ApiOperation(value = "数据选取：根据地质构造树节点获取其下子数据")
    @GetMapping("/getTreeChild")
    public Object getTreeChild(@ApiParam(name = "realValue", value = "取节点数据的 PROJECT_ID 字段")
                               @RequestParam String realValue,
                               @ApiParam(name = "type", value = "地质体类型")
                               @RequestParam(required = false) String type,
                               @ApiParam(name = "name", value = "名称模糊查询")
                               @RequestParam(required = false) String name) {
        return geoService.getChildData(realValue, type, name);
    }

    @ApiOperation(value = "判断工作是否存在")
    @PostMapping("/isExist")
    public boolean isExist(@ApiParam("模板ID") @RequestParam String template,
                           @ApiParam("地质体类型") @RequestParam String geoType,
                           @ApiParam("地质体对象") @RequestBody GeoVo[] geoVos) {
        return workService.isExist(template, geoType, geoVos);

    }

    @ApiOperation(value = "获取当前用户组织机构")
    @GetMapping("/getDataRegion")
    public String getDataRegion() {
        return SpringManager.getCurrentUser().getDataRegion();
    }

    @ApiOperation(value = "查看节点研究是否已经开始")
    @GetMapping("/{workId}/isNodeStart")
    public boolean isNodeStart(@PathVariable String workId, String nodeId) {
        return workNodeService.isNodeStart(workId, nodeId);
    }

    @ApiOperation(value = "节点开始")
    @GetMapping("/{workId}/startNode")
    public boolean startNode(@PathVariable String workId, String nodeId) {
        boolean success = true;
        try {
            workNodeService.startNode(workId, nodeId);
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    @ApiOperation(value = "根据模板查询工作", tags = "面向其它项目的API分组")
    @GetMapping("/getWorkByTemplate")
    public Object getWorkByTemplate(@ApiParam(name = "templateId", value = "模板ID，多个用逗号分隔", required = true)
                                    @RequestParam String templateId,
                                    @ApiParam(name = "withStatus", value = "是否查询状态", required = true)
                                            boolean withStatus) {
        return workService.getWorkByTemplate(templateId, withStatus);
    }

    @ApiOperation(value = "获取工作对象")
    @GetMapping("/{workId}/getGeo")
    public Object getGeo(@PathVariable String workId) {
        return workService.getGeo(workId);
    }

    @ApiOperation(value = "获取工作节点", tags = "面向其它项目的API分组")
    @GetMapping("/{workId}/workNodes")
    public Object workNodes(@PathVariable String workId) {
        return workService.workNodes(workId);
    }

    @ApiOperation(value = "获取工作节点对象")
    @GetMapping("/{workId}/{nodeId}/{dataSets}/getDataObject")
    public Object getDataObject(@PathVariable String workId, @PathVariable String nodeId, @PathVariable String dataSets) {
        return workService.getDataObject(workId, nodeId, dataSets);
    }

    @GetMapping("/test")
    public Object test(String id) {
        return geoService.list(new QueryWrapper<Geo>().eq("work_id", id));
    }

}
