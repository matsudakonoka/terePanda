package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.commom.StringConvert;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.entity.WorkNode;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.mapper.WorkTaskMapper;
import com.cnpc.epai.core.workscene.pojo.vo.AssignVo;
import com.cnpc.epai.core.workscene.pojo.vo.ChargeUserVo;
import com.cnpc.epai.core.workscene.pojo.vo.ReportTemplateVo;
import com.cnpc.epai.core.workscene.service.TreeService;
import com.cnpc.epai.core.workscene.service.WorkNodeService;
import com.cnpc.epai.core.workscene.service.WorkSceneTaskService;
import com.cnpc.epai.core.workscene.service.WorkService;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cnpc.epai.core.workscene.commom.StringConvert.*;

@Service
public class WorkSceneTaskServiceImpl extends ServiceImpl<WorkTaskMapper, WorkTask> implements WorkSceneTaskService {

    @Autowired
    private TreeService treeService;

    @Autowired
    private WorkService workService;

    @Autowired
    private WorkNodeService workNodeService;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Override
    @Transactional
    public void assignWork(String workId, AssignVo[] assignVos) throws Exception {
        /**
         * 任务存在, 修改任务节点集，修改工作节点最新负责人
         * 任务不存在，创建任务，修改工作节点最新负责人
         */
        Work work = workService.getById(workId);
        //则通过接口获取负责人信息
        List<ChargeUserVo> ResponsibleUserList = new ArrayList<>();
        String userIdArr[] = work.getChargeUser().split(",");
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
        ChargeUserVo currentUserInfoVo = null;
        Request request = null;
        Call call = null ;
        Response execute = null;
        ResponseBody body = null;
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        for (String userId:userIdArr){
            currentUserInfoVo = new ChargeUserVo();
            //当前登录用户不是工作负责人
            request = new Request.Builder().get()
                    .header("Authorization","Bearer "+ TokenUtil.getToken())
                    .url("http://" + ServerAddr+"/sys/user/org?userId="+userId)
                    .build();
            call = client.newCall(request);
            execute = call.execute();
            body = execute.body();
            String string = body.string();
            jsonArray = JSON.parseArray(string);
            if(jsonArray.isEmpty()){
                throw new Exception("未获取到工作负责人信息！");
            }
            jsonObject = jsonArray.getJSONObject(0);
            if(StringUtils.isNotEmpty(jsonObject.getString("displayName"))){
                currentUserInfoVo.setChargeUserId(userId);
                currentUserInfoVo.setChargeUserName(jsonObject.getString("displayName"));
            }else{
                throw new Exception("未获取到工作负责人信息！");
            }
            ResponsibleUserList.add(currentUserInfoVo);
        }
        Map<String, Map<String, Object>> tasks = new HashMap<>();

        for (AssignVo assign : assignVos) {
            String treeNodeId = assign.getTreeNodeId();
            Date startTime = assign.getStartTime();
            Date endTime = assign.getEndTime();
            String recommend = assign.getRecommend();
            ChargeUserVo[] chargeUsers = assign.getChargeUser();
            if (chargeUsers == null || chargeUsers.length == 0) {
                continue;
            }
            String latestCharge;
            if (chargeUsers.length > 1) {
                latestCharge = convertToString(chargeUsers);//获取所有id
            } else {
                latestCharge = chargeUsers[0].getChargeUserId();
            }
            //如果节点不包含负责人，则自动添加
            List<ChargeUserVo> chargeUserList = new ArrayList<>(Arrays.asList(chargeUsers));
            for (ChargeUserVo UserVo : ResponsibleUserList) {
                if(!latestCharge.contains(UserVo.getChargeUserId())){
                    latestCharge += ","+UserVo.getChargeUserId();
                    chargeUserList.add(UserVo);
                }
            }
            chargeUsers = chargeUserList.toArray(new ChargeUserVo[chargeUserList.size()]);

            // 修改工作节点最新负责人
            UpdateWrapper<WorkNode> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("charge_latest", latestCharge)
                    .set("start_time", startTime)
                    .set("end_time", endTime)
                    .set("recommend", recommend)
                    .eq("tree_node_id", treeNodeId).eq("work_id", workId);
            workNodeService.update(updateWrapper);

            for (ChargeUserVo chargeUser : chargeUsers) {
                String id = chargeUser.getChargeUserId();
                String name = chargeUser.getChargeUserName();
                if (tasks.containsKey(id)) {
                    Map<String, Object> map = tasks.get(id);
                    List<String> nodes = (List<String>) map.get("nodes");
                    nodes.add(treeNodeId);
                    tasks.put(id, map);
                } else {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("name", name);
                    List<String> nodes = new ArrayList<>();
                    nodes.add(treeNodeId);
                    map.put("nodes", nodes);
                    tasks.put(id, map);
                }
            }
        }

        //处理人员权限：
        for (Map.Entry<String, Map<String, Object>> entry : tasks.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> map = entry.getValue();
            String name = (String) map.get("name");

            List<String> nodes = (List<String>) map.get("nodes");
            String treeNodes = StringConvert.convertToString(nodes);

            QueryWrapper<WorkTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("work_id", workId).eq("charge_user_id", id);
            WorkTask workTask = getOne(queryWrapper);
            List<WorkTask> newTask = new ArrayList<>();

            if (workTask == null) {
                WorkTask task = WorkTask.builder()
                        .taskName(work.getWorkName())
                        .workId(workId)
                        .templateId(work.getTemplateId())
                        .chargeUserId(id)
                        .chargeUserName(name)
                        .treeNodeIds(treeNodes).build();
                newTask.add(task);
            }
            saveBatch(newTask);
        }
        //获取所有人员权限明细信息
        QueryWrapper<WorkNode> queryWrapper0 = new QueryWrapper<>();
        queryWrapper0.eq("work_id",work.getWorkId());
        List<WorkNode> nodeList = workNodeService.list(queryWrapper0);
        //获取所有人员信息
        List<String> userIds = new ArrayList<>();
        for(WorkNode workNode : nodeList){
            String[] split = workNode.getChargeLatest().split(",");
            userIds.addAll(new ArrayList<>(Arrays.asList(split)));
        }
        //获取所有人员权限
        QueryWrapper<WorkTask> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("work_id",work.getWorkId());
        List<WorkTask> taskList = list(taskQueryWrapper);

        List<String> delTaskList = new ArrayList<>();
        for (WorkTask workTask : taskList) {
            //1、获取没有权限人员:并删除
            if(!userIds.contains(workTask.getChargeUserId())){
                delTaskList.add(workTask.getTaskId());
            }
            //2、处理所有人员的权限：删除一个人【当前页面全部权限，整个工作的部分权限】：tasks不存在，数据不准的处理
            List<String> existNodes = new ArrayList<>();
            for(WorkNode workNode : nodeList){
                String sexist = workNode.getChargeLatest();
                if(sexist.contains(workTask.getChargeUserId())){
                    existNodes.add(workNode.getTreeNodeId());
                }
            }
            //节点去重
            List<String> collect = existNodes.stream().distinct().collect(Collectors.toList());
            String ids = convertToString(collect);
            WorkTask wt = WorkTask.builder()
                    .taskId(workTask.getTaskId())
                    .treeNodeIds(ids).build();
            updateById(wt);
        }
        if(!delTaskList.isEmpty()){
            removeByIds(delTaskList);
        }

    }

    @Override
    public List<JSONObject> getMinWorks(String workId, String userId, String nodeId) {
        Work work =workService.getById(workId);
        String treeId = work.getInstanceId();
        List<Map<String, Object>> tree = treeService.getTree2(work.getWorkId(), nodeId);


        // nodeId 到 sourceNodeId 的映射
        Map<String, String> nodeMap = new HashMap<>();

        // 当前节点下的最小业务单元
        Set<String> nodeSet = new HashSet<>();
        if (tree == null) {
            nodeSet.add(nodeId);
        } else {
            searchTreeList(tree, nodeSet, nodeMap);
        }
        // String userId = SpringManager.getCurrentUser().getUserId();
        WorkTask Task = getWorkTask(workId, userId);
        String nodeIds = Task.getTreeNodeIds();
        String[] workNodes = nodeIds.split(",");

        QueryWrapper<WorkNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",workId);
        queryWrapper.in("tree_node_id",Arrays.asList(workNodes));
        List<WorkNode> workNodeList = workNodeService.list(queryWrapper);
        Map<String,WorkNode> workNodeMap = new HashMap<>();
        for(WorkNode workNode : workNodeList){
            workNodeMap.put(workNode.getTreeNodeId(),workNode);
        }

        Map<String,WorkTask> workTaskMap = new HashMap<>();
        QueryWrapper<WorkTask> workTaskQueryWrapper = new QueryWrapper<>();
        workTaskQueryWrapper.eq("work_id",workId);
        List<WorkTask> list = list(workTaskQueryWrapper);
        for(WorkTask workTask : list){
            workTaskMap.put(workTask.getChargeUserId(),workTask);
        }

        List<JSONObject> result = new ArrayList<>();
        for (String node : workNodes) {
            if (nodeSet.contains(node)) {
                /*WorkNode workNode = workNodeService.getOne(new QueryWrapper<WorkNode>().eq("work_id", workId)
                        .eq("tree_node_id", node));*/
                WorkNode workNode = workNodeMap.get(node);
                if (workNode == null) {
                    throw new IllegalStateException("当前工作不存在节点该节点");
                }
                AssignVo assignVo = new AssignVo();
                assignVo.setTreeNodeId(workNode.getTreeNodeId());
                assignVo.setTreeNodeName(workNode.getTreeNodeName());

                String[] charges = workNode.getChargeLatest().split(",");
                List<ChargeUserVo> chargeUserVos = new ArrayList<>();
                for (String charge : charges) {
                    ChargeUserVo chargeUserVo = new ChargeUserVo();
                    chargeUserVo.setChargeUserId(charge);
//                    WorkTask workTask = getTask(workId, charge);
                    WorkTask workTask = workTaskMap.get(charge);
                    if (workTask != null) {
                        chargeUserVo.setChargeUserName(workTask.getChargeUserName());
                        chargeUserVos.add(chargeUserVo);
                    }
                }
                ChargeUserVo[] chargeUsers = new ChargeUserVo[chargeUserVos.size()];
                chargeUserVos.toArray(chargeUsers);
                assignVo.setChargeUser(chargeUsers);
                assignVo.setStartTime(workNode.getStartTime());
                assignVo.setEndTime(workNode.getEndTime());
                assignVo.setRecommend(workNode.getRecommend());

                JSONObject object = (JSONObject) JSONObject.toJSON(assignVo);
                object.put("sourceNodeId", nodeMap.get(node));
                result.add(object);
            }
        }
        return result;
    }

    private void searchTreeList(List<Map<String, Object>> tree, Set<String> nodeSet, Map<String, String> nodeMap) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            String nodeId = (String) node.get("nodeId");
            if (subTree == null || subTree.size() == 0) {
                String nodeType = (String) node.get("nodeType");
                String sourceNodeId = (String) node.get("sourceNodeId");
                if (nodeType != null && nodeType.equals("WORKUNIT")) {
                    nodeSet.add(nodeId);
                    nodeMap.put(nodeId, sourceNodeId);
                }
                continue;
            }
            searchTreeList(subTree, nodeSet, nodeMap);
        }
    }

    @Override
    public WorkTask getWorkTask(String workId, String userId) {
        QueryWrapper<WorkTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id", workId).eq("charge_user_id", userId);
        WorkTask one = getOne(queryWrapper);
        return one;
    }

    @Override
    public Object getNode(String workId, String userId) {
        QueryWrapper<WorkTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("work_id", workId).eq("charge_user_id", userId);
        WorkTask task = getOne(taskWrapper);
        String nodes = task.getTreeNodeIds();
        String[] treeNodes = nodes.split(",");
        QueryWrapper<WorkNode> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("work_id", workId).in("tree_node_id", treeNodes);
        List<WorkNode> list = workNodeService.list(nodeWrapper);
        return list;
    }

    @Override
    public List<WorkTask> getWorkTask(String workId) {
        QueryWrapper<WorkTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id", workId);
        return list(queryWrapper);
    }

    private WorkTask getTask(String workId, String userId) {
        return getOne(new QueryWrapper<WorkTask>()
                .eq("work_id", workId)
                .eq("charge_user_id", userId));
    }
}
