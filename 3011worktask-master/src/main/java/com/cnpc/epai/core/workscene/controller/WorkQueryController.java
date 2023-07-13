package com.cnpc.epai.core.workscene.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.service.*;
import com.cnpc.epai.core.worktask.util.AdjacentWellsReuseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "工作创建分组")
@RestController
@RequestMapping("/core/workQuery")
public class WorkQueryController {

    @Autowired
    private WorkSceneTaskService workTaskService;

    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @ApiOperation(value="T3数据集查询")
    @PostMapping("/getAllTreeNodeIds")
    public ApiResult getAllTreeNodeIds(String workId,String taskId,String treeId,@RequestBody JSONObject object){
        ApiResult apiResult = ApiResult.newInstance();
        try {
            QueryWrapper<WorkNavigateTreeNode> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("node_type","WORKUNIT");
            if(StringUtils.isNotEmpty(treeId)){
                queryWrapper.eq("tree_id",treeId);
            }else{
                queryWrapper.eq("tree_id",treeId);
                if(StringUtils.isEmpty(taskId)){
                    apiResult.setFlag(false);
                    apiResult.setMsg("taskId参数不能为空");
                    return apiResult;
                }
                QueryWrapper<WorkTask> taskWrapper = new QueryWrapper<>();
                taskWrapper.eq("work_id", workId);
                taskWrapper.eq("task_id", taskId);
                List<WorkTask> workTasks = workTaskService.list(taskWrapper);
                if(workTasks.size()==0){
                    apiResult.setFlag(false);
                    apiResult.setMsg("taskId参数查不到数据");
                    return apiResult;
                }
                String node_ids=workTasks.get(0).getTreeNodeIds();
                List<String> nodeIds = Arrays.asList(node_ids.split(","));
                queryWrapper.in("node_id",nodeIds);
            }
            String sourceNodeIds = "";
            List<WorkNavigateTreeNode> treelist = workNavigateTreeNodeService.list(queryWrapper);
            for(int i=0;i<treelist.size();i++){
                WorkNavigateTreeNode tree=treelist.get(i);
                String sourceNodeId=tree.getSourceNodeId();
                if(i==0){
                    sourceNodeIds = sourceNodeIds + sourceNodeId;
                }else{
                    sourceNodeIds = sourceNodeIds +","+ sourceNodeId;
                }
            }
            //T3对应数据集查询
            List<Map<String,Object>> toolList= AdjacentWellsReuseUtil.nodeResources(ServerAddr,sourceNodeIds.split(","));
            String resType0 = object.get("resType")==null?"":object.get("resType").toString();//"result,dataset"
            Map<String,Object> datasetMap = new HashMap<>();
            Map<String,Object> extAttributes = null;
            for (Map nodemap:toolList){
                String treeNodeId=nodemap.get("treeNodeId").toString();
                String resType=nodemap.get("resType").toString();
                String resId=nodemap.get("resId").toString();
                extAttributes = (Map<String,Object>) JSON.parse(nodemap.get("extAttributes").toString());
                extAttributes.put("treeNodeId",treeNodeId);
                extAttributes.put("resType",resType);
                extAttributes.put("resId",resId);
                if(StringUtils.isEmpty(resType0) || resType0.contains(resType)){
                    String datajson = datasetMap.get(treeNodeId)==null?"[]":datasetMap.get(treeNodeId).toString();
                    JSONArray extAttributesList = JSON.parseArray(datajson);
                    extAttributesList.add(extAttributes);
                    datasetMap.put(treeNodeId,extAttributesList);
                }
            }
            apiResult.setResult(datasetMap);
            apiResult.setFlag(true);
            apiResult.setMsg("查询成功！");
            return apiResult;
        } catch (Exception e) {
            e.printStackTrace();
            apiResult.setMsg(e.getMessage());
            apiResult.setFlag(false);
            return apiResult;
        }
    }
}
