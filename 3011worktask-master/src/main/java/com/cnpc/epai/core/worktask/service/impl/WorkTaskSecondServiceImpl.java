package com.cnpc.epai.core.worktask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.worktask.domain.*;
import com.cnpc.epai.core.worktask.mapper.*;
import com.cnpc.epai.core.worktask.pojo.*;
import com.cnpc.epai.core.worktask.repository.*;
import com.cnpc.epai.core.worktask.service.WorkTaskSecondService;
import com.cnpc.epai.core.worktask.service.WorkTaskServiceImpl;
import com.cnpc.epai.research.tool.domain.Tool;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkTaskSecondServiceImpl implements WorkTaskSecondService {
    //一期 业务处理
    @Autowired
    WorkTaskServiceImpl taskServiceImpl;
    //二期应用视图与任务关系
    @Autowired
    SrProjectTaskDatasetMapper taskDatasetMapper;

    @Autowired
    SrTaskMgrMapper taskMgrMapper;

    @Autowired
    SrTaskLogMapper logMapper;

    @Autowired
    SrTaskAssignMapper assignMapper;

    @Autowired
    SrProjectTaskSoftwareMapper taskSoftwareMapper;

    @Autowired
    SrProjectTaskToolMapper taskToolMapper;

    @Autowired
    SrPresetSceneTaskMapper sceneTaskMapper;

    @Autowired
    DatasetTreeRepository treeRepository;
    @Autowired
    SceneRepository sceneRepository;
    //任务
    @Autowired
    WorkTaskRepository workTaskRepository;
    //任务日志
    @Autowired
    WorkTaskLogRepository workTaskLogRepository;
    //任务人员分配
    @Autowired
    WorkTaskAssignRepository workTaskAssignRepository;
    // 节点
    @Autowired
    WorkTaskBusinessNodeRepository workTaskBusinessNodeRepository;
    //常用工具服务
    @Autowired
    ToolRepository toolRepository;
    //任务常用工具
    @Autowired
    WorkTaskToolRepository workTaskToolRepository;
    //任务专业软件
    @Autowired
    WorkTaskSoftwareRepository workTaskSoftwareRepository;

    @Autowired
    LabelRepository labelRepository;

    RestTemplate restTemplate = new RestTemplate();

    @Value("${epai.domainhost:www.dev.pcep.cloud}")
    String host;


    @Override
    public SrTaskMgr createWorktask(SrTaskMgr workTask, String userIds, String nodes, String toolIds, String softwareIds) throws BusinessException {
        //1.校验各种参数
        if (workTask == null) {
            throw new BusinessException("-1", "当前用户不具有操作权限。");
        }

        String exceptionStr = "";
        if (StringUtils.isEmpty(workTask.getTaskType())) {
            exceptionStr += "请指定当前任务是成果任务还是场景任务。";
        }
        if (StringUtils.isEmpty(workTask.getWorkroomId())) {
            exceptionStr += "请指定项目。";
        }
        if (StringUtils.isEmpty(workTask.getTaskName())) {
            exceptionStr += "请填写任务名称。";
        }
        if (workTask.getStartDate() == null) {
            exceptionStr += "请填写开始日期。";
        }
        if (workTask.getEndDate() == null) {
            exceptionStr += "请填写结束日期。";
        }

        if (workTask.getEndDate().getTime() < workTask.getStartDate().getTime()) {
            exceptionStr += "结束日期只能在开始日期之后。";
        }

        if (!StringUtils.isEmpty(exceptionStr)) {
            throw new BusinessException("-10", exceptionStr);
        }


        save(workTask, userIds);
        //场景任务
        if ("SCENE".equals(workTask.getTaskType())) {
            String taskId = workTask.getTaskId();
            String tools[] = toolIds.split(",");
            String softwares[] = softwareIds.split(",");

            //获取库里已经存在的业务结点
            List<WorkTaskBusinessNode> nodeList = workTaskBusinessNodeRepository.findByTaskId(taskId);
            List<String> nodeIdList = new ArrayList<String>();
            //确定更新的业务结点（区别新增的）
            taskServiceImpl.findUpdataBusinessNode(nodes, nodeList, nodeIdList);


            taskServiceImpl.deleteTools(taskId);
            taskServiceImpl.deleteApplications(taskId);

            List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.parse(nodes);
            //二期修改新数据集
            deleteNodes(taskId, nodeIdList);

            if (list != null && !list.isEmpty()) {
                taskServiceImpl.saveBusinessNode(taskId, list);
            }

            //过滤掉重复id
            Set<String> toolIdList = new HashSet<>(Arrays.asList(tools));
            Set<String> softwareIdList = new HashSet<>(Arrays.asList(softwares));

            WorkTaskSoftware workTaskSoftware = null;
            WorkTaskTool workTaskTool = null;

            Tool tool = null;
            List<WorkTaskTool> workTaskToolList = new ArrayList<>();
            for (String id : toolIdList) {
                workTaskTool = new WorkTaskTool();
                tool = toolRepository.findOne(id);
                if (tool == null) {
                    continue;
                }
                workTaskTool.setBsflag("N");
                workTaskTool.setProjectId(workTask.getWorkroomId());
                workTaskTool.setTaskId(workTask.getTaskId());
                workTaskTool.setTool(tool);

                workTaskTool.setCreateDate(new Date());
                workTaskTool.setCreateUser(SpringManager.getCurrentUser().getUserId());
                workTaskTool.setUpdateDate(new Date());
                workTaskTool.setUpdateUser(SpringManager.getCurrentUser().getUserId());

                workTaskToolList.add(workTaskTool);
            }
            workTaskToolRepository.save(workTaskToolList);

            List<WorkTaskSoftware> workTaskSoftwareList = new ArrayList<>();
            for (String id : softwareIdList) {
                workTaskSoftware = new WorkTaskSoftware();

                workTaskSoftware.setProjectId(workTask.getWorkroomId());
                workTaskSoftware.setTaskId(workTask.getTaskId());
                workTaskSoftware.setBsflag("N");
                workTaskSoftware.setSoftwareId(id);

                workTaskSoftware.setCreateDate(new Date());
                workTaskSoftware.setCreateUser(SpringManager.getCurrentUser().getUserId());
                workTaskSoftware.setUpdateDate(new Date());
                workTaskSoftware.setUpdateUser(SpringManager.getCurrentUser().getUserId());

                workTaskSoftwareList.add(workTaskSoftware);
            }
            workTaskSoftwareRepository.save(workTaskSoftwareList);
        }

        //分派任务后发送短信和坦途待办消息
        sentMessAndTantu(workTask, userIds);

        return workTask;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SrTaskMgr createSceneWorktask(Map<String, Object> map) throws BusinessException {
        Map<String, Object> taskMap = (Map<String, Object>) map.get("workTask");
        String userIds = map.get("userIds").toString();
        SrTaskMgr workTask = getSrTaskMgr(taskMap);
        //1.校验各种参数
        if (taskMap == null) {
            throw new BusinessException("-1", "当前用户不具有操作权限。");
        }
        String exceptionStr = "";
        if (StringUtils.isEmpty(workTask)) {
            exceptionStr += "请指定当前任务是成果任务还是场景任务。";
        }
        if (StringUtils.isEmpty(workTask.getWorkroomId())) {
            exceptionStr += "请指定项目。";
        }
        if (StringUtils.isEmpty(workTask.getTaskName())) {
            exceptionStr += "请填写任务名称。";
        }
        if (workTask.getStartDate() == null) {
            exceptionStr += "请填写开始日期。";
        }
        if (workTask.getEndDate() == null) {
            exceptionStr += "请填写结束日期。";
        }

        if (workTask.getEndDate().getTime() < workTask.getStartDate().getTime()) {
            exceptionStr += "结束日期只能在开始日期之后。";
        }

        if (!StringUtils.isEmpty(exceptionStr)) {
            throw new BusinessException("-10", exceptionStr);
        }

        workTask = save(workTask, userIds);
        //场景任务
        if ("SCENE".equals(workTask.getTaskType())) {
            String taskId = workTask.getTaskId();
//            String sceneClass = map.get("sceneClass").toString();
            //业务导航树
//        if ("SCENEBUSINESS".equals(sceneClass)) {
            List<Map<String, Object>> businessNodes = (List<Map<String, Object>>) map.get("businessNodes");
            //  调用3004保存业务导航树
            treeRepository.saveSceneBusinessTree(taskId, "SCENEBUSINESS", businessNodes);
            //删除专业专业,常用工具
            taskServiceImpl.deleteTools(taskId);
            taskServiceImpl.deleteApplications(taskId);
            String toolIds = "";
            String softwareIds = "";
            if (!StringUtils.isEmpty(map.get("toolIds"))) {
                toolIds = map.get("toolIds").toString();
            }
            if (!StringUtils.isEmpty(map.get("softwareIds"))) {
                softwareIds = map.get("softwareIds").toString();
            }
            if (!StringUtils.isEmpty(map.get("toolIds"))) {
                String tools[] = toolIds.split(",");
                //过滤掉重复id
                Set<String> toolIdList = new HashSet<>(Arrays.asList(tools));
                WorkTaskTool workTaskTool = null;
                Tool tool = null;
                List<WorkTaskTool> workTaskToolList = new ArrayList<>();
                for (String id : toolIdList) {
                    workTaskTool = new WorkTaskTool();
                    tool = toolRepository.findOne(id);
                    if (tool == null) {
                        continue;
                    }
                    workTaskTool.setBsflag("N");
                    workTaskTool.setProjectId(workTask.getWorkroomId());
                    workTaskTool.setTaskId(workTask.getTaskId());
                    workTaskTool.setTool(tool);

                    workTaskTool.setCreateDate(new Date());
                    workTaskTool.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    workTaskTool.setUpdateDate(new Date());
                    workTaskTool.setUpdateUser(SpringManager.getCurrentUser().getUserId());

                    workTaskToolList.add(workTaskTool);
                }
                workTaskToolRepository.save(workTaskToolList);
            }
            if (!StringUtils.isEmpty(map.get("softwareIds"))) {
                String softwares[] = softwareIds.split(",");
                //过滤掉重复id
                Set<String> softwareIdList = new HashSet<>(Arrays.asList(softwares));
                WorkTaskSoftware workTaskSoftware = null;

                List<WorkTaskSoftware> workTaskSoftwareList = new ArrayList<>();
                for (String id : softwareIdList) {
                    workTaskSoftware = new WorkTaskSoftware();

                    workTaskSoftware.setProjectId(workTask.getWorkroomId());
                    workTaskSoftware.setTaskId(workTask.getTaskId());
                    workTaskSoftware.setBsflag("N");
                    workTaskSoftware.setSoftwareId(id);

                    workTaskSoftware.setCreateDate(new Date());
                    workTaskSoftware.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    workTaskSoftware.setUpdateDate(new Date());
                    workTaskSoftware.setUpdateUser(SpringManager.getCurrentUser().getUserId());

                    workTaskSoftwareList.add(workTaskSoftware);
                }
                workTaskSoftwareRepository.save(workTaskSoftwareList);
            }
//         }
            //数据管理树
//            if ("SCENEMANAGEMENT".equals(sceneClass)) {
            List<Map<String, Object>> managementNodes = (List<Map<String, Object>>) map.get("managementNodes");
            //  调用3004保存数据管理树
            treeRepository.saveSceneBusinessTree(taskId, "SCENEMANAGEMENT", managementNodes);
            //删除保存预置场景与任务
            SrPresetSceneTaskExample example = new SrPresetSceneTaskExample();
            SrPresetSceneTaskExample.Criteria criteria = example.createCriteria();
            criteria.andBsflagEqualTo("N");
            criteria.andTaskIdEqualTo(workTask.getTaskId());
            sceneTaskMapper.deleteByExample(example);
            //保存场景和任务关联
            String sceneIds = map.get("sceneIds").toString();
            if (!StringUtils.isEmpty(sceneIds)) {
                //去重
                List<String> sceneIdList = Arrays.asList(sceneIds.split(",")).stream().distinct().collect(Collectors.toList());
                SrPresetSceneTask sceneTask = new SrPresetSceneTask();

                for (String sceneId : sceneIdList) {
                    sceneTask.setPresetSceneTaskId(ShortUUID.randomUUID());
                    sceneTask.setTaskId(workTask.getTaskId());
                    sceneTask.setSceneId(sceneId);
                    sceneTask.setWorkroomId(workTask.getWorkroomId());
                    sceneTask.setBsflag("N");
                    sceneTask.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    sceneTask.setCreateDate(new Date());
                    sceneTaskMapper.insert(sceneTask);
                }
            }
            //TODO 对象树与任务关系
//            }
        }
        //分派任务后发送短信和坦途待办消息
        sentMessAndTantu(workTask, userIds);

        return workTask;
    }

    @Override
    public List<Map<String,Object>> getPresetSceneList(String workroomId, String taskId) {
        SrPresetSceneTaskExample example = new SrPresetSceneTaskExample();
        SrPresetSceneTaskExample.Criteria criteria = example.createCriteria();
        criteria.andBsflagEqualTo("N");
        criteria.andTaskIdEqualTo(taskId);
        criteria.andWorkroomIdEqualTo(workroomId);
        List<SrPresetSceneTask> list = sceneTaskMapper.selectByExample(example);
        List<String> sceneIdList = list.stream().map(i -> i.getSceneId()).distinct().collect(Collectors.toList());
        //查询场景列表
        String sceneIds = String.join(",", sceneIdList);
        List<Map<String,Object>>  sceneList = sceneRepository.getSceneList(sceneIds);
        return sceneList;
    }

    @Override
    public String submitTask(String taskId, String fileIds) throws BusinessException{
        SrTaskMgr task = taskMgrMapper.selectByPrimaryKey(taskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId();
        //校验任务是否存在
        if (task == null) {
            throw new BusinessException("-1", "任务不存在。");
        }
        //index表关联任务id
       treeRepository.getIndexList(task.getWorkroomId(), fileIds,taskId);
        //2. 日志记录
        SrTaskLog log = new SrTaskLog();
        log.setTaskLogId(ShortUUID.randomUUID());
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setWorkroomId(task.getWorkroomId());
        //记录关联的文件id
        log.setOperContent(fileIds);
        log.setUserId(currentUserId);
        log.setTaskId(taskId);
        // 4 关联成果
        log.setOperType("4");
        logMapper.insert(log);


        //代表已经提交任务
        task.setCurrentState("4");
        task.setUpdateDate(new Date());
        task.setUpdateUser(currentUserId);

        taskMgrMapper.updateByPrimaryKey(task);

        labelRepository.updateLableStatus(task.getWorkroomId(), task.getTaskId(), "STOP_TASK");

        return "1";
    }

    @NotNull
    private SrTaskMgr getSrTaskMgr(Map<String, Object> taskMap) {
        SrTaskMgr workTask = new SrTaskMgr();
        if (!StringUtils.isEmpty(taskMap.get("taskId"))) {
            workTask.setTaskId(taskMap.get("taskId").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("workroomId"))) {
            workTask.setWorkroomId(taskMap.get("workroomId").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("datasetId"))) {
            workTask.setDatasetId(taskMap.get("datasetId").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("taskName"))) {
            workTask.setTaskName(taskMap.get("taskName").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("currentState"))) {
            workTask.setCurrentState(taskMap.get("currentState").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("workType"))) {
            workTask.setWorkType(taskMap.get("workType").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("startDate"))) {
            workTask.setStartDate(getDate(taskMap.get("startDate").toString()));
        }
        if (!StringUtils.isEmpty(taskMap.get("endDate"))) {
            workTask.setEndDate(getDate(taskMap.get("endDate").toString()));
        }
        if (!StringUtils.isEmpty(taskMap.get("bsflag"))) {
            workTask.setBsflag(taskMap.get("bsflag").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("remarks"))) {
            workTask.setRemarks(taskMap.get("remarks").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("deleteDate"))) {
            workTask.setDeleteDate(getDate(taskMap.get("deleteDate").toString()));
        }
        if (!StringUtils.isEmpty(taskMap.get("taskType"))) {
            workTask.setTaskType(taskMap.get("taskType").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("planId"))) {
            workTask.setPlanId(taskMap.get("planId").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("createUser"))) {
            workTask.setPlanId(taskMap.get("createUser").toString());
        }
        if (!StringUtils.isEmpty(taskMap.get("createDate"))) {
            workTask.setCreateDate(getDate(taskMap.get("createDate").toString()));
        }
        return workTask;
    }

    private Date getDate(String d) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public SrTaskMgr save(SrTaskMgr workTask, String userIds) {
        String id = workTask.getTaskId();

        SrTaskLog log = new SrTaskLog();
        log.setTaskLogId(ShortUUID.randomUUID());
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setWorkroomId(workTask.getWorkroomId());
        log.setOperContent("");
        log.setUserId(SpringManager.getCurrentUser().getUserId());

        String users[] = userIds.split(",");
        if (users.length > 1) {
            workTask.setWorkType("1");
        } else if (users.length == 1) {
            workTask.setWorkType("0");
        }

        //过滤掉重复id
        Set<String> userIdList = new HashSet<>(Arrays.asList(users));


        if (!StringUtils.isEmpty(id)) {
//            WorkTask w = workTaskRepository.findOne(id);
            SrTaskMgr w = taskMgrMapper.selectByPrimaryKey(id);
            workTask.setCreateUser(w.getCreateUser());
            workTask.setCreateDate(w.getCreateDate());
            log.setOperType("7"); //修改
            clearWorkTaskInfo(id);
            log.setTaskId(id);
        } else {
            workTask.setTaskId(ShortUUID.randomUUID());
            workTask.setCreateUser(SpringManager.getCurrentUser().getUserId());
            workTask.setCreateDate(new Date());
            log.setTaskId(workTask.getTaskId());
            log.setOperType("1"); //指派
        }
        workTask.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        workTask.setUpdateDate(new Date());
        workTask.setBsflag("N");
        workTask.setCurrentState("1"); //未响应
        //二期任务保存
//        workTaskRepository.save(workTask);
        if (!StringUtils.isEmpty(id)) {
            taskMgrMapper.updateByPrimaryKey(workTask);
        } else {
            taskMgrMapper.insert(workTask);
        }

//        log.setWorkTask(workTask);

//        workTaskLogRepository.save(log);
        //二期日志保存
        logMapper.insert(log);

        SrTaskAssign assign = null;
//        List<SrTaskAssign> list = new ArrayList();
        for (String userId : userIdList) {
            assign = new SrTaskAssign();
            assign.setTaskAssignId(ShortUUID.randomUUID());
            assign.setBsflag("N");
            assign.setCreateDate(new Date());
            assign.setCreateUser(SpringManager.getCurrentUser().getUserId());
//            assign.setIsManager("");
            assign.setWorkroomId(workTask.getWorkroomId());
            assign.setTaskResponse("");
            assign.setUserId(userId);
            if (!StringUtils.isEmpty(id)) {
                assign.setTaskId(id);
            } else {
                assign.setTaskId(workTask.getTaskId());
            }

//            list.add(assign);
            //二期保存任务人员
            assignMapper.insert(assign);
        }
//        workTaskAssignRepository.save(list);

        return workTask;
    }

    public void clearWorkTaskInfo(String workTaskId) {
        //1.二期清空日志表
//        workTaskLogRepository.deleteByWorkTaskId(workTaskId);
        SrTaskLogExample logExample = new SrTaskLogExample();
        SrTaskLogExample.Criteria logCriteria = logExample.createCriteria();
        logCriteria.andBsflagEqualTo("N");
        logCriteria.andTaskIdEqualTo(workTaskId);
        logMapper.deleteByExample(logExample);
        //2.二期清空任务分配表
//        workTaskAssignRepository.deleteByWorkTaskId(workTaskId);
        SrTaskAssignExample assignExample = new SrTaskAssignExample();
        SrTaskAssignExample.Criteria assignCriteria = assignExample.createCriteria();
        assignCriteria.andBsflagEqualTo("N");
        assignCriteria.andTaskIdEqualTo(workTaskId);
        assignMapper.deleteByExample(assignExample);
        //3.清空数据集、常用工具、专业软件
//        workTaskDataSetRepository.deleteByWorkTaskId(workTaskId);
        //清空删除数据集
        SrProjectTaskDatasetExample example = new SrProjectTaskDatasetExample();
        SrProjectTaskDatasetExample.Criteria criteria = example.createCriteria();
        criteria.andBsflagEqualTo("N");
        criteria.andTaskIdEqualTo(workTaskId);
        taskDatasetMapper.deleteByExample(example);
        //二期清空常用工具
        SrProjectTaskToolExample toolExample = new SrProjectTaskToolExample();
        SrProjectTaskToolExample.Criteria toolCriteria = toolExample.createCriteria();
        toolCriteria.andBsflagEqualTo("N");
        toolCriteria.andTaskIdEqualTo(workTaskId);
        taskToolMapper.deleteByExample(toolExample);
//        workTaskToolRepository.deleteByWorkTaskId(workTaskId);
        //二期清空专业软件
        SrProjectTaskSoftwareExample softwareExample = new SrProjectTaskSoftwareExample();
        SrProjectTaskSoftwareExample.Criteria softwareCriteria = softwareExample.createCriteria();
        softwareCriteria.andBsflagEqualTo("N");
        softwareCriteria.andTaskIdEqualTo(workTaskId);
        taskSoftwareMapper.deleteByExample(softwareExample);
//        workTaskSoftwareRepository.deleteByWorkTaskId(workTaskId);
    }


    /**
     * 对于更新的业务结点不进行删除
     *
     * @param taskId
     * @param isExistNodeIdList
     */
    public void deleteNodes(String taskId, List<String> isExistNodeIdList) {
        List<WorkTaskBusinessNode> nodeList = workTaskBusinessNodeRepository.findByTaskId(taskId);

        List<String> nodeIds = new ArrayList<>();
        for (WorkTaskBusinessNode node : nodeList) {
            nodeIds.add(node.getId());
        }

//        List<SrProjectTaskDataset> businessNodeDataSetList = new ArrayList<SrProjectTaskDataset>();
//        businessNodeDataSetList = taskDatasetMapper.selectByExample(example);

        Iterator<WorkTaskBusinessNode> it = nodeList.iterator();
        while (it.hasNext()) {
            WorkTaskBusinessNode node = it.next();
            for (String str : isExistNodeIdList) {
                if (node.getId().equals(str)) {
                    //todo 关联了多个同样的业务节点 ，二次删除时会报错
                    try {
                        it.remove();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

//        workTaskDataSetRepository.delete(businessNodeDataSetList);
////        workTaskDataSetRepository.flush();
        //删除
        if (!nodeIds.isEmpty()) {
//            businessNodeDataSetList = workTaskDataSetRepository.findByTaskId(taskId);
            SrProjectTaskDatasetExample example = new SrProjectTaskDatasetExample();
            SrProjectTaskDatasetExample.Criteria criteria = example.createCriteria();
            criteria.andTaskIdEqualTo(taskId);
            criteria.andBsflagEqualTo("N");
            taskDatasetMapper.deleteByExample(example);
        }
        workTaskBusinessNodeRepository.delete(nodeList);
        workTaskBusinessNodeRepository.flush();
    }

    public void sentMessAndTantu(SrTaskMgr workTask, String userIds) {
        Map mapProject = JSONObject.parseObject(JSONObject.toJSONString(getProject(workTask.getWorkroomId())), Map.class);
        //精细油藏类及塔里木油田 发送短信
        if ("TL".equals(mapProject.get("dataRegion")) && "ATSVZB100013067".equals(mapProject.get("activityMajType"))) {
            String org = "";
            LinkedHashMap jsonObject = (LinkedHashMap) getUserOrg().get("result_data");
            List json = (List) jsonObject.get("list");
            Map mapJson = (Map) json.get(0);
            org = (String) mapJson.get("org_name");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("appCode", "5fee3636-c481-4b2d-9d1b-c2a90f5108d7");
            map.put("appName", "精细油藏描述");
            map.put("content", "");
            String[] strArray = userIds.split(",");
            List<String> accountList = new ArrayList<>();
            for (int i = 0; i < strArray.length; i++) {
                accountList.add(getAccountId(strArray[i]));
            }
            //String[] strArray = {"shiwl.ptr"};
            map.put("receiveId", accountList.toArray());
            String sendId = getAccountId(SpringManager.getCurrentUser().getUserId());
            map.put("sendId", sendId);
            map.put("sendName", SpringManager.getCurrentUser().getDisplayName());
            map.put("title", "您好，您有一条待办消息：\n" +
                    "关于" + mapProject.get("name") + "-" + workTask.getTaskName() + "任务，由" + org + "*" + SpringManager.getCurrentUser().getDisplayName() + "*发起。\n" +
                    "请登录坦途协同工作平台，尽快处理。");
            map.put("ip", "11.234.68.11");
            try {
                String url = "http://messagecenter-server.tlm-shared-components.tlm.pcep.cloud:8080/sys/msg/sendSMS";
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(type);
                headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
                headers.set("Authorization", "Bearer " + TokenUtil.getToken());

                String appCode = restTemplate.exchange(url.toString(), HttpMethod.POST, new HttpEntity<>(map, headers), String.class).getBody();
            } catch (Exception e) {
                throw new RuntimeException("发送信息失败");
            }
            //坦途待办
            Map<String, Object> mapTantu = new HashMap<String, Object>();
            mapTantu.put("appCode", "5fee3636-c481-4b2d-9d1b-c2a90f5108d7");
            mapTantu.put("appName", "精细油藏描述");
            mapTantu.put("content", "");
            mapTantu.put("receiveId", accountList.toArray());
            mapTantu.put("sendId", sendId);
            mapTantu.put("sendName", SpringManager.getCurrentUser().getDisplayName());
            mapTantu.put("title", "您好，您有一条来自项目工作室待办消息，请尽快处理");
            mapTantu.put("routing", "http://www.pcep.cloud/front/research/platform/index.html?projectId=" + workTask.getWorkroomId() + "&portalcontain=title&projectType=workroom");
            mapTantu.put("businessId", "1884279b-a74f-4f6a-8f31-d22378749a2e");
            mapTantu.put("businessType", 1);
            mapTantu.put("businessDocument", "");
            mapTantu.put("functionCode", "");
            mapTantu.put("functionName", "");
            mapTantu.put("scheme", "");

            try {
                String url = "http://messagecenter-server.tlm-shared-components.tlm.pcep.cloud:8080/sys/msg/sendInternal";
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(type);
                headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
                headers.set("Authorization", "Bearer " + TokenUtil.getToken());

                String appCode = restTemplate.exchange(url.toString(), HttpMethod.POST, new HttpEntity<>(mapTantu, headers), String.class).getBody();
            } catch (Exception e) {
                throw new RuntimeException("发送待办信息失败");
            }
        }

    }

    //获取项目信息
    private JSONObject getProject(String projectId) {
        RestTemplate restTemplate = new RestTemplate();
        //host = "www.crp.tlm.pcep.cloud";
        JSONObject projectInfo = new JSONObject();
        //获取相应项目信息
        try {
            // 请求url
            String url = "http://" + host + "/research/project/" + projectId + "/getinfo";

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization", "Bearer " + TokenUtil.getToken());

            // 使用RestTemplate请求其他服务并通过url传参
            projectInfo = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("获取项目信息失败");
        }
        return projectInfo;
    }

    //获取用户组织信息
    private JSONObject getUserOrg() {
        JSONObject jsonObject = new JSONObject();
        String serverHost = null;
        if ("www.crp.tlm.pcep.cloud".equals(host)) {
            serverHost = "www.pcep.cloud";
        } else {
            serverHost = host;
        }
        try {
            JSONObject result = getCurrentUser();
            //http://www.pcep.cloud/sys/masterdata/org/batch?current_user_id=vwegh8lw7u6346i1g32odenp&search_type=ORGIDS&search_value=ORGATL110001171
            String url = "http://" + serverHost + "/sys/masterdata/org/batch?current_user_id=" + SpringManager.getCurrentUser().getUserId() + "&search_type=ORGIDS&search_value=" + result.getString("belongOrg");

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization", "Bearer " + TokenUtil.getToken());

            jsonObject = restTemplate.exchange(url.toString(), HttpMethod.POST, new HttpEntity<>("", headers), JSONObject.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("获取当前用户组织信息失败");
        }
        return jsonObject;
    }

    //获取当前用户信息
    private JSONObject getCurrentUser() {
        JSONObject jsonObject = new JSONObject();
        try {
            String url = "http://" + host + "/sys/user/currentuser";
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization", "Bearer " + TokenUtil.getToken());

            jsonObject = restTemplate.exchange(url.toString(), HttpMethod.GET, new HttpEntity<>("", headers), JSONObject.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("获取当前用户信息失败");
        }
        return jsonObject;
    }

    //获取AccountId
    private String getAccountId(String userId) {
        JSONObject jsonObject = new JSONObject();
        try {
            // 请求url
            String url = "http://usercenter.tlm.pcep.cloud/sys/user/detail/" + userId;

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization", "Bearer " + TokenUtil.getToken());

            // 使用RestTemplate请求其他服务并通过url传参
            jsonObject = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("获取塔里木用户信息失败");
        }
        LinkedHashMap sendIds = (LinkedHashMap) jsonObject.get("data");
        String id = (String) sendIds.get("accountId");
        return id;
    }
}
