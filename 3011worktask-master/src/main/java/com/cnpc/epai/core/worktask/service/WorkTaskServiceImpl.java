package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.template.service.TreeCondiDto;
import com.cnpc.epai.common.template.service.TreeJsonDto;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.common.util.InOutProcessor;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.worktask.configuration.ThreadPoolConfig;
import com.cnpc.epai.core.worktask.domain.*;
import com.cnpc.epai.core.worktask.repository.*;
import com.cnpc.epai.research.tool.domain.Tool;
import com.cnpc.epai.research.tool.domain.ToolType;
/*import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;*/
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


import javax.annotation.Resource;
import javax.persistence.criteria.*;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author 王淼
 * @version 1.0.0
 * @Description: 任务服务实现
 * @date 2018/4/20
 */
@Service
public class WorkTaskServiceImpl extends TreeServiceImplResearch implements WorkTaskService {
    @Autowired
    WorkTaskRepository workTaskRepository;

    @Autowired
    WorkTaskLogRepository workTaskLogRepository;

    @Autowired
    WorkTaskAssignRepository workTaskAssignRepository;

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    DataSetRepository dataSetRepository;

    @Autowired
    ToolRepository toolRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    WorkTaskDataSetRepository workTaskDataSetRepository;

    @Autowired
    WorkTaskToolRepository workTaskToolRepository;

    @Autowired
    WorkTaskSoftwareRepository workTaskSoftwareRepository;

    @Autowired
    SatellitesRepository satellitesRepository;

    @Autowired
    ProjectUserSoftwareRepository projectUserSoftwareRepository;

    @Autowired
    WorkTaskBusinessNodeRepository workTaskBusinessNodeRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RestMetadataRepository metadataRepository;

    @Resource(name = "synchronousDataExecutorPool")
    private ExecutorService executorService;

    @Value("${epai.domainhost:www.dev.pcep.cloud}")
    String host;

    RestTemplate restTemplate = new RestTemplate();

    Map<String, String> worktask_status = null;

    public WorkTaskServiceImpl() {
        worktask_status = new HashMap<String, String>();
        worktask_status.put("1", "未响应");
        worktask_status.put("2", "进行中");
        worktask_status.put("3", "拒绝");
        worktask_status.put("4", "已提交");
        worktask_status.put("5", "已完成");
        worktask_status.put("6", "已终止");
        worktask_status.put("7", "审核未通过");
    }

    /**
     * 获取所有任务
     *
     * @param page
     * @return 任务分页
     */
    @Override
    public Page<WorkTask> findByBsflag(Pageable page, String condition) {
        Map<String, String> conditionMap = JSON.parseObject(condition, Map.class);
        Page<WorkTask> projects = workTaskRepository.findAll(new Specification<WorkTask>() {
            @Override
            public Predicate toPredicate(Root<WorkTask> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                Predicate where = null;
                if (conditionMap != null) {
                    for (Map.Entry<String, String> entry : conditionMap.entrySet()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if ("startDate_start".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.greaterThanOrEqualTo(root.get("startDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("startDate_end".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.lessThanOrEqualTo(root.get("startDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("endDate_start".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.greaterThanOrEqualTo(root.get("endDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("endDate_end".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.lessThanOrEqualTo(root.get("endDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("assignUserId".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            String[] value = entry.getValue().split(",");
                            where = root.get("workTaskAssigns").get("userId").in(value);
                        } else if ("taskName".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            where = cb.like(root.get("taskName"), "%" + entry.getValue() + "%");
                        } else {
                            String[] value = entry.getValue().split(",");
                            if (value.length > 1) {
                                where = root.get(entry.getKey()).in(value);
                            } else {
                                if (!StringUtils.isEmpty(value[0])) {
                                    where = cb.equal(root.get(entry.getKey()), value[0]);
                                }
                            }
                        }
                        System.out.println("key= " + entry.getKey() + " and value= "
                                + entry.getValue());
                        if (where != null) {
                            predicateList.add(where);
                        }
                    }
                }
                criteriaQuery.distinct(true);

                //关联查询项目工作室 关联查询可用项目工作室的任务。
                Join<WorkTask, Project> projectJoin = root.join("project", JoinType.LEFT);
                predicateList.add(cb.equal(projectJoin.get("bsflag"), "N"));


                predicateList.add(cb.equal(root.get("bsflag"), "N"));
                return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, page);
//        parseWorkTask(p);
        return projects;
    }

    public List<WorkTask> findByLevel2List(String condition) {
        Map<String, String> conditionMap = JSON.parseObject(condition, Map.class);
        List<WorkTask> projects = workTaskRepository.findAll(new Specification<WorkTask>() {
            @Override
            public Predicate toPredicate(Root<WorkTask> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                Predicate where = null;
                if (conditionMap != null) {
                    for (Map.Entry<String, String> entry : conditionMap.entrySet()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if ("startDate_start".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.greaterThanOrEqualTo(root.get("startDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("startDate_end".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.lessThanOrEqualTo(root.get("startDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("endDate_start".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.greaterThanOrEqualTo(root.get("endDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("endDate_end".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            Date date = null;
                            try {
                                date = sdf.parse(entry.getValue());
                                where = cb.lessThanOrEqualTo(root.get("endDate"), date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if ("assignUserId".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            String[] value = entry.getValue().split(",");
                            where = root.get("workTaskAssigns").get("userId").in(value);
                        } else if ("taskName".equalsIgnoreCase(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                            where = cb.like(root.get("taskName"), "%" + entry.getValue() + "%");
                        } else {
                            String[] value = entry.getValue().split(",");
                            if (value.length > 1) {
                                where = root.get(entry.getKey()).in(value);
                            } else {
                                if (!StringUtils.isEmpty(value[0])) {
                                    where = cb.equal(root.get(entry.getKey()), value[0]);
                                }
                            }
                        }


                        System.out.println("key= " + entry.getKey() + " and value= "
                                + entry.getValue());
                        if (where != null) {
                            predicateList.add(where);
                        }
                    }
                }
                criteriaQuery.distinct(true);

                //关联查询项目工作室 关联查询可用项目工作室的任务。
                Join<WorkTask, Project> projectJoin = root.join("project", JoinType.LEFT);
                predicateList.add(cb.equal(projectJoin.get("bsflag"), "N"));
                predicateList.add(cb.isNotNull(root.get("planId")));

                predicateList.add(cb.equal(root.get("bsflag"), "N"));
                return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        });
//        parseWorkTask(p);
        return projects;
    }

    @Override
    public Page<WorkTask> findPersonTask(Pageable page, String condition) {
        Page<WorkTask> personWork = findByBsflag(page, condition);
        List<WorkTask> list = personWork.getContent();
//        String pid = list.stream().map(pro -> pro.getProjectId()).distinct().collect(Collectors.joining(","));
//        List<Map<String,String>> projectList = projectRepository.findByids(pid);
//        Map<String,String> projectMap = projectList.stream().collect(Collectors.toMap(pro -> pro.get("id"),pro -> pro.get("name")));
        list.stream().forEach(workTask -> {
//            workTask.setProjectName(projectMap.get(workTask.getProjectId()));
//            workTask.setCreateUserName(authorityRepository.getUserDisplayName(workTask.getCreateUser()));
            if (workTask.getProject() != null) {
                workTask.setProjectName(workTask.getProject().getName());
            }
            workTask.setCreateUserName(authorityRepository.getUserDisplayName(workTask.getCreateUser()));
        });
        return personWork;
    }

    @Override
    public Map<String, Integer> countWorkTask(String projectIds) {
        List<String> list = Arrays.asList(projectIds.split(","));
        Map<String, Integer> countMap = new HashMap<>();
        List<Object[]> countList = workTaskRepository.countAllByProjectIds(list);
        for (Object[] obj : countList) {
            countMap.put((String) obj[0], Integer.parseInt("" + obj[1]));
        }
        return countMap;
    }

    @Override
    public Page<WorkTask> findByProject(String workroomId, String status, Pageable page) {
        Page<WorkTask> p = null;
        if (StringUtils.isEmpty(status)) {
            p = workTaskRepository.findByProjectId(workroomId, "N", page);
        } else {
            p = workTaskRepository.findByProjectIdAndStatus(workroomId, Arrays.asList(status.split(",")), page);
        }
//
//        for (WorkTask wt : p.getContent()) {
//            parseAssignUser(wt);
//        }
        return p;
    }

    /**
     * 目前已经改成worktask直接关联worktaskassign 不采用这种方式
     * 不建议使用
     *
     * @param p
     */
    private void parseWorkTask(Page<WorkTask> p) {
        for (WorkTask wt : p.getContent()) {
            parseAssignUser(wt);
        }
    }

    /**
     * 目前已经改成worktask直接关联worktaskassign 不采用这种方式
     * 不建议使用
     */
    private void parseAssignUser(WorkTask wt) {
        if (wt.getAttribute() == null) {
            wt.setAttribute(new HashMap<String, Object>());
        }
        Map<String, Object> map = wt.getAttribute();

        List<WorkTaskAssign> list = workTaskAssignRepository.findByWorkTaskId(wt.getId());

        Map<String, String> userMap = null;
        List<Map<String, String>> userList = new ArrayList<>();
        for (WorkTaskAssign assign : list) {
            userMap = new HashMap<>();
            userMap.put("userId", assign.getUserId());
            userMap.put("userName", authorityRepository.getUserDisplayName(assign.getUserId()));
            userList.add(userMap);
        }
        map.put("assignUser", userList);
    }


    public void findUpdataBusinessNode(String json, List<WorkTaskBusinessNode> nodeList, List<String> nodeIdList) {
        for (WorkTaskBusinessNode node : nodeList) {
            if (json.contains(node.getId())) {
                nodeIdList.add(node.getId());
            }
        }
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

        List<WorkTaskDataSet> businessNodeDataSetList = new ArrayList<WorkTaskDataSet>();
        if (!nodeIds.isEmpty()) {
            businessNodeDataSetList = workTaskDataSetRepository.findByTaskId(taskId);
        }

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
        for (WorkTaskDataSet workTaskDataSet : businessNodeDataSetList) {
            workTaskDataSetRepository.delete(workTaskDataSet.getId());
            workTaskDataSetRepository.flush();
        }
        for (WorkTaskBusinessNode workTaskBusinessNode : nodeList) {
            workTaskBusinessNodeRepository.delete(workTaskBusinessNode.getId());
            workTaskBusinessNodeRepository.flush();
        }
//        workTaskDataSetRepository.delete(businessNodeDataSetList);
//        workTaskDataSetRepository.flush();
//        workTaskBusinessNodeRepository.delete(nodeList);
//        workTaskBusinessNodeRepository.flush();
    }

    private void copyBusinessNode(WorkTaskBusinessNode oldNode, WorkTaskBusinessNode newNode) {
        newNode.setId(ShortUUID.randomUUID());
        newNode.setName(oldNode.getName());
        newNode.setBsFlag("N");
        newNode.setProjectId(oldNode.getProjectId());
        newNode.setTaskId(oldNode.getId());
        newNode.setIsStandard("N");
        newNode.setIsStartUse("Y");

        newNode.setCreateDate(new Date());
        newNode.setCreateUser(SpringManager.getCurrentUser().getUserId());
        newNode.setUpdateDate(new Date());
        newNode.setUpdateUser(SpringManager.getCurrentUser().getUserId());
    }

    public void saveBusinessNode(String taskId, List<Map<String, Object>> list) {
        if (taskId == null) {
            return;
        }
        WorkTask task = workTaskRepository.findOne(taskId);

        if (task == null) {
            return;
        }

        Map<String, List> resultMap = new HashMap<String, List>();
        List businessNodeDataSetList = new ArrayList();
        List<WorkTaskBusinessNode> businessNodeList = new ArrayList<WorkTaskBusinessNode>();
        resultMap.put("businessNodeDataSetList", businessNodeDataSetList);

        //遍历根节点Map<String,Object> map : list
        Map<String, Object> map = null;

        WorkTaskBusinessNode newBnode = null;
//        String businessNodeName = "";
        for (int i = 0; i < list.size(); i++) {
            WorkTaskBusinessNode oldNode = null;
            map = list.get(i);
//            if (!StringUtils.isEmpty(map.get("name"))) {
//                businessNodeName = (String) map.get("name");
//            }

            //保存业务节点关系   业务节点和模板的关系 业务节点和父节点的关系
            if (!StringUtils.isEmpty(map.get("id"))) {
                oldNode = workTaskBusinessNodeRepository.findOne((String) map.get("id"));
            }
            WorkTaskBusinessNode bNode = null;
            //新建的业务节点
            if (oldNode == null) {
                bNode = new WorkTaskBusinessNode();
                bNode.setName((String) map.get("text"));
                bNode.setProjectId(task.getProjectId());
                bNode.setTaskId(task.getId());
                bNode.setBsFlag("N");
                bNode.setShowOrder(i);
                bNode.setIsStartUse("Y");
                bNode.setIsStandard("N");

                bNode.setCreateDate(new Date());
                bNode.setCreateUser(SpringManager.getCurrentUser().getUserId());
                bNode.setUpdateDate(new Date());
                bNode.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                //如果是标准节点需要拷贝一份
            } else if (oldNode.getIsStandard().equalsIgnoreCase("Y")) {
                //新建业务节点
                newBnode = new WorkTaskBusinessNode();
                copyBusinessNode(oldNode, newBnode);
                newBnode.setShowOrder(i);
                bNode = newBnode;
            } else {
//                continue;
                oldNode.setShowOrder(i);
                bNode = oldNode;

                bNode.setUpdateDate(new Date());
                bNode.setUpdateUser(SpringManager.getCurrentUser().getUserId());
            }

            Object dataSetListObj = map.get("dataSetList");
            if (dataSetListObj != null && dataSetListObj instanceof List) {
                List<Map<String, String>> dataSetList = (List) dataSetListObj;
                SrMetaDataset dataSet = null;
                Map<String, String> dataSetMap = null;
                for (int j = 0; j < dataSetList.size(); j++) {
                    dataSetMap = dataSetList.get(j);
                    dataSet = dataSetRepository.findOne(dataSetMap.get("id"));
                    WorkTaskDataSet bnDs = new WorkTaskDataSet();
                    bnDs.setWorkTaskBusinessNode(bNode);
                    bnDs.setShowOrder(j);
                    bnDs.setDataSet(dataSet);
                    bnDs.setProjectId(task.getProjectId());
                    bnDs.setTaskId(task.getId());

                    bnDs.setCreateDate(new Date());
                    bnDs.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    bnDs.setUpdateDate(new Date());
                    bnDs.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                    //3 保存数据集和业务节点的关系表
                    if (dataSet != null) {
                        businessNodeDataSetList.add(bnDs);
                    }
                }
            }
            Object childrenObj = map.get("children");
            if (childrenObj != null && childrenObj instanceof List) {
                List<Map<String, Object>> nodeList = (List) childrenObj;
                if (nodeList != null) {
                    addBusinessNode(task.getProjectId(), taskId, nodeList, resultMap, bNode);
                }
            }
            businessNodeList.add(bNode);
        }
        workTaskBusinessNodeRepository.save(businessNodeList);
        workTaskBusinessNodeRepository.flush();
        workTaskDataSetRepository.save(businessNodeDataSetList);
        workTaskDataSetRepository.flush();

    }

    private List genList(List list) {
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    public void addBusinessNode(String projectId, String tasktId, List<Map<String, Object>> list,
                                Map<String, List> resultMap, WorkTaskBusinessNode parentNode) {
        List businessNodeDataSetList = genList(resultMap.get("businessNodeDataSetList"));

        //遍历根节点Map<String,Object> map : list
        Map<String, Object> map = null;

        WorkTaskBusinessNode newBnode = null;

//        String businessNodeName = "";
        for (int i = 0; i < list.size(); i++) {
            WorkTaskBusinessNode oldNode = null;
            map = list.get(i);

            Map attributes = (Map) map.get("attributes");
            if ("true".equalsIgnoreCase(attributes.get("isElement").toString())) {
                continue;
            }

//            if (!StringUtils.isEmpty(map.get("name"))) {
//                businessNodeName = (String) map.get("name");
//            }
            //保存业务节点关系   业务节点和模板的关系 业务节点和父节点的关系
            if (!StringUtils.isEmpty(map.get("id"))) {
                oldNode = workTaskBusinessNodeRepository.findOne((String) map.get("id"));
            }
            newBnode = new WorkTaskBusinessNode();
            WorkTaskBusinessNode bNode = null;
            //新建的业务节点
            if (oldNode == null) {
                bNode = new WorkTaskBusinessNode();
                bNode.setName((String) map.get("text"));
                bNode.setProjectId(projectId);
                bNode.setTaskId(tasktId);
                bNode.setShowOrder(i);
                bNode.setBsFlag("N");
                bNode.setIsStartUse("Y");
                bNode.setIsStandard("N");

                bNode.setCreateDate(new Date());
                bNode.setCreateUser(SpringManager.getCurrentUser().getUserId());
                bNode.setUpdateDate(new Date());
                bNode.setUpdateUser(SpringManager.getCurrentUser().getUserId());

                //如果是标准节点需要拷贝一份
            } else if (oldNode.getIsStandard().equalsIgnoreCase("Y")) {
                //新建业务节点
                copyBusinessNode(oldNode, newBnode);
                newBnode.setShowOrder(i);
                bNode = newBnode;
            } else {
                oldNode.setShowOrder(i);
                bNode = oldNode;
                bNode.setUpdateDate(new Date());
                bNode.setUpdateUser(SpringManager.getCurrentUser().getUserId());
            }
            bNode.setParent(parentNode);
            if (parentNode.getChildren() != null) {
                parentNode.getChildren().add(bNode);
            } else {
                List childrenNode = new ArrayList();
                childrenNode.add(bNode);
                parentNode.setChildren(childrenNode);
            }

            //1. copy业务节点保存
            if (map.get("dataSetList") != null && map.get("dataSetList") instanceof List) {
                List<Map<String, String>> dataSetList = (List) map.get("dataSetList");
                WorkTaskDataSet bnDs = null;
                SrMetaDataset dataSet = null;
                Map<String, String> dataSetMap = null;
                for (int j = 0; j < dataSetList.size(); j++) {
                    dataSetMap = dataSetList.get(j);
                    bnDs = new WorkTaskDataSet();
                    dataSet = dataSetRepository.findOne(dataSetMap.get("id"));
                    bnDs.setWorkTaskBusinessNode(bNode);
                    bnDs.setTaskId(tasktId);
                    bnDs.setProjectId(projectId);
                    bnDs.setDataSet(dataSet);
                    bnDs.setShowOrder(j);

                    bnDs.setCreateDate(new Date());
                    bnDs.setCreateUser(SpringManager.getCurrentUser().getUserId());
                    bnDs.setUpdateDate(new Date());
                    bnDs.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                    if (dataSet != null) {
                        //3 保存数据集和业务节点的关系表
                        businessNodeDataSetList.add(bnDs);
                    }
                }
            }

            if (map.get("children") != null && map.get("children") instanceof List) {
                List<Map<String, Object>> nodeList = (List) map.get("children");
                if (nodeList != null) {
                    addBusinessNode(projectId, tasktId, nodeList, resultMap, bNode);
                }
            }
        }

    }

    /**
     * 创建场景任务 后台调用
     *
     * @return
     */
    @Override
    @Transactional
    public WorkTask createSceneWorktask(WorkTask workTask, String userIds) throws BusinessException {
        String exceptionStr = "";
        if (StringUtils.isEmpty(workTask.getTaskType())) {
            exceptionStr += "请指定当前任务是成果任务还是场景任务。";
        }
        if (StringUtils.isEmpty(workTask.getProjectId())) {
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

        return save(workTask, userIds);
    }


    public void deleteTools(String taskId) {
        workTaskToolRepository.deleteByWorkTaskId(taskId);
    }

    public void deleteApplications(String taskId) {
        workTaskSoftwareRepository.deleteByWorkTaskId(taskId);
    }


    //如果是编辑的 需要清空数据
    @Override
    @Transactional
    public WorkTask createWorktask(WorkTask workTask,
                                   String userIds,
                                   String nodes,
                                   String toolIds,
                                   String softwareIds) throws BusinessException {
        //1.校验各种参数
        if (workTask == null) {
            throw new BusinessException("-1", "当前用户不具有操作权限。");
        }

        String exceptionStr = "";
        if (StringUtils.isEmpty(workTask.getTaskType())) {
            exceptionStr += "请指定当前任务是成果任务还是场景任务。";
        }
        if (StringUtils.isEmpty(workTask.getProjectId())) {
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
            String taskId = workTask.getId();
            String tools[] = toolIds.split(",");
            String softwares[] = softwareIds.split(",");

            //获取库里已经存在的业务结点
            List<WorkTaskBusinessNode> nodeList = workTaskBusinessNodeRepository.findByTaskId(taskId);
            List<String> nodeIdList = new ArrayList<String>();
            //确定更新的业务结点（区别新增的）
            findUpdataBusinessNode(nodes, nodeList, nodeIdList);


            deleteTools(taskId);
            deleteApplications(taskId);

            List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.parse(nodes);
            deleteNodes(taskId, nodeIdList);

            if (list != null && !list.isEmpty()) {
                saveBusinessNode(taskId, list);
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
                workTaskTool.setProjectId(workTask.getProjectId());
                workTaskTool.setTaskId(workTask.getId());
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

                workTaskSoftware.setProjectId(workTask.getProjectId());
                workTaskSoftware.setTaskId(workTask.getId());
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
    @Transactional
    public WorkTask save(WorkTask workTask, String userIds) {
        String id = workTask.getId();

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
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
            WorkTask w = workTaskRepository.findOne(id);
            workTask.setCreateUser(w.getCreateUser());
            workTask.setCreateDate(w.getCreateDate());
            log.setOperType("7"); //修改
            clearWorkTaskInfo(id);
        } else {
            workTask.setCreateUser(SpringManager.getCurrentUser().getUserId());
            workTask.setCreateDate(new Date());
            log.setOperType("1"); //指派
        }
        workTask.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        workTask.setUpdateDate(new Date());
        workTask.setBsflag("N");
        workTask.setCurrentState("1"); //未响应
        workTaskRepository.save(workTask);

        log.setWorkTask(workTask);
        workTaskLogRepository.save(log);

        WorkTaskAssign assign = null;

        List<WorkTaskAssign> list = new ArrayList();
        for (String userId : userIdList) {
            assign = new WorkTaskAssign();
            assign.setBsflag("N");
            assign.setCreateDate(new Date());
            assign.setCreateUser(SpringManager.getCurrentUser().getUserId());
//            assign.setIsManager("");
            assign.setProjectId(workTask.getProjectId());
            assign.setTaskResponse("");
            assign.setUserId(userId);
            assign.setWorkTask(workTask);
            list.add(assign);
        }
        workTaskAssignRepository.save(list);
        return workTask;
    }

    @Override
    public WorkTask findById(String id) {
        WorkTask task = workTaskRepository.findOne(id);
//        parseAssignUser(task);
        return task;
    }

    @Override
    public Page<WorkTask> findByProjectIdAndUserId(String projectId, String userId, String status, Pageable page) {
        Page<WorkTask> p = null;
        if (StringUtils.isEmpty(status)) {
            p = workTaskAssignRepository.findByProjectIdAndUserId(projectId, userId, page);
        } else {
            p = workTaskAssignRepository.findByProjectIdAndUserIdAndStatus(projectId, userId, Arrays.asList(status.split(",")), page);
        }

//        for (WorkTask wt : p.getContent()) {
//            parseAssignUser(wt);
//        }
        return p;
//        String userType = proRepository.findUserType(projectId,userId);
//        //项目经理获取所有任务列表
//        if ("M".equals(userType)){
//            return findByProject(projectId,page);
//            //其他角色获取指派给自己的任务列表
//        }else{
//            Page p = null;
//            if (StringUtils.isEmpty(status)) {
//                p = workTaskAssignRepository.findByProjectIdAndUserId(projectId,userId,page);
//            }else{
//                p = workTaskAssignRepository.findByProjectIdAndUserIdAndStatus(projectId,userId,Arrays.asList(status.split(",")),page);
//            }
//             parseWorkTask(p);
//             return p;
//        }
    }

    @Override
    public String answerAssignment(String workTaskId, String opr, String oprContent) throws BusinessException {
        String currentUserId = SpringManager.getCurrentUser().getUserId();

        List<WorkTaskAssign> list = workTaskAssignRepository.findByWorkTaskIdAndUserId(workTaskId, currentUserId);

        if (list.isEmpty()) {
            throw new BusinessException("-1", "当前用户不具有操作权限。");
        }
        //1. 判断是否需要操作 已经拒绝的不用再操作
        WorkTask workTask = list.get(0).getWorkTask();
        WorkTaskAssign workTaskAssign = list.get(0);

        //只能是未响应的任务才可以
        if (!workTask.getCurrentState().equals("1")) {
            //任务已经被拒绝
            if (workTask.getCurrentState().equals("3")) {
                return "-1";
                //任务状态异常
            } else {
                return "-2";
            }
        }

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        //操作内容
        log.setOperContent(oprContent);
        log.setUserId(currentUserId);
        log.setWorkTask(workTask);


        workTaskAssign.setUpdateDate(new Date());
        workTaskAssign.setUpdateUser(currentUserId);

        workTask.setUpdateDate(new Date());
        workTask.setUpdateUser(currentUserId);
        //同意
        if ("pass".equals(opr)) {
            log.setOperType("2"); //接受指派
            workTaskAssign.setTaskResponse("Y");
            workTaskAssignRepository.save(workTaskAssign);
            if (isAllPass(workTask.getId())) {
                workTask.setCurrentState("2"); //进行中
                //增加启动任务标签
                labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "NEW_TASK");
                workTaskRepository.save(workTask);
            }
        } else if ("refuse".equals(opr)) {
            workTask.setCurrentState("3"); //拒绝
            log.setOperType("3"); //拒绝指派
            workTaskAssign.setTaskResponse("N");
            workTaskAssignRepository.save(workTaskAssign);
            workTaskRepository.save(workTask);
        } else {
            //操作类型异常
            return "-3";
        }
        workTaskLogRepository.save(log);
        return "1";
    }

    /**
     * 结束任务
     *
     * @param workTaskId
     * @return
     * @throws BusinessException
     */
    @Override
    @Transactional
    public String taskOver(String workTaskId) throws BusinessException {
        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId();
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

        workTask.setCurrentState("6");
        workTask.setUpdateDate(new Date());
        workTask.setUpdateUser(currentUserId);
        workTaskRepository.save(workTask);

        labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "STOP_TASK");
        return "1";
    }

    /**
     * 删除任务
     *
     * @param workTaskId
     * @return
     * @throws BusinessException
     */
    @Override
    public String deleteTask(String workTaskId) throws BusinessException {
        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId();
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        log.setOperContent("删除任务");
        log.setUserId(currentUserId);
        log.setWorkTask(workTask);
        // 9代表删除
        log.setOperType("8");


        workTask.setBsflag("Y"); //删除任务
        workTask.setUpdateDate(new Date());
        workTask.setUpdateUser(currentUserId);

        workTaskRepository.save(workTask);
        workTaskLogRepository.save(log);

        labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "STOP_TASK");
        return "1";
    }


    /**
     * 延期任务
     *
     * @param workTaskId
     * @param endDate
     * @return
     * @throws BusinessException
     */
    @Override
    public String delay(String workTaskId, Date endDate) throws BusinessException {
        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId();
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

        if (endDate == null) {
            throw new BusinessException("-2", "请填写任务延期日期。");
        }

        if (workTask.getEndDate() != null && endDate != null) {
            if (workTask.getEndDate().getTime() == endDate.getTime()) {
                return "2";
            }
            if (workTask.getEndDate().getTime() > endDate.getTime()) {
                throw new BusinessException("-1", "任务不可提前结束。");
            } else {
                //todo  1. 站内提醒执行人 任务延期
                //2. 新增日志
                WorkTaskLog log = new WorkTaskLog();
                log.setBsflag("N");
                log.setCreateDate(new Date());
                log.setCreateUser(SpringManager.getCurrentUser().getUserId());
                log.setUpdateDate(new Date());
                log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                log.setOperTime(new Date());
                log.setProjectId(workTask.getProjectId());
                String content = String.format("任务结束日期由%s变更为%s", workTask.getEndDate().toLocaleString(), endDate.toLocaleString());
                log.setOperContent(content);
                log.setUserId(currentUserId);
                log.setWorkTask(workTask);
                // 10代表延期
                log.setOperType("10");

                workTask.setEndDate(endDate);
                workTask.setUpdateUser(currentUserId);
                workTask.setUpdateDate(new Date());

                workTaskRepository.save(workTask);
                workTaskLogRepository.save(log);

                labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "EDIT_TASK");

            }
        } else {
            return "-1";
        }
        return "1";
    }

    /**
     * 催办任务
     *
     * @param workTaskId
     * @param endDate
     * @return
     * @throws BusinessException
     */
    @Override
    public String reminders(String workTaskId, Date endDate) throws BusinessException {
        //1. todo 发送站内提醒

        //2. 新建催办日志
        String currentUserId = SpringManager.getCurrentUser().getUserId();

        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        log.setOperContent("");
        log.setUserId(currentUserId);
        log.setWorkTask(workTask);
        // 9代表催办
        log.setOperType("9");

        workTaskLogRepository.save(log);

        //3. 如果endDate有值 更新任务的终止时间

        if (endDate != null) {
            workTask.setEndDate(endDate);
            workTask.setUpdateDate(new Date());
            workTask.setUpdateUser(currentUserId);
            workTaskRepository.save(workTask);
            labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "EDIT_TASK");
        }

        return "1";
    }

    /**
     * 提交任务 并关联成果
     *
     * @param workTaskId
     * @param treeDataIds
     * @return
     * @throws BusinessException
     */
    @Override
    public String submitTask(String workTaskId, String treeDataIds) throws BusinessException {
        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId(); //SpringManager.getCurrentUser().getUserId();
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

//        if (StringUtils.isEmpty(treeDataIds)) {
//            throw new BusinessException("-2", "请填写关联成果。");
//        }

        if (!StringUtils.isEmpty(treeDataIds)) {
            //1. 关联成果 调用佳琪的服务 更新服务
            documentRepository.relatedAchievement(workTask.getProjectId(), workTask.getId(), treeDataIds.split(","));

        }

        //2. 日志记录
        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        //记录关联的文件id
        log.setOperContent(treeDataIds);
        log.setUserId(currentUserId);
        log.setWorkTask(workTask);
        // 4 关联成果
        log.setOperType("4");
        workTaskLogRepository.save(log);

        //代表已经提交任务
        workTask.setCurrentState("4");
        workTask.setUpdateDate(new Date());
        workTask.setUpdateUser(currentUserId);
        workTaskRepository.save(workTask);

        labelRepository.updateLableStatus(workTask.getProjectId(), workTask.getId(), "STOP_TASK");

        return "1";
    }

    public void clearWorkTaskInfo(String workTaskId) {
        //1.清空日志表
        workTaskLogRepository.deleteByWorkTaskId(workTaskId);
        //2.清空任务分配表
        workTaskAssignRepository.deleteByWorkTaskId(workTaskId);

        //3.清空数据集、常用工具、专业软件
        workTaskDataSetRepository.deleteByWorkTaskId(workTaskId);
        workTaskToolRepository.deleteByWorkTaskId(workTaskId);
        workTaskSoftwareRepository.deleteByWorkTaskId(workTaskId);
    }

    @Override
    @Transactional
    public String rebootWorkTask(WorkTask workTask, String userIds) throws BusinessException {
        String currentUserId = SpringManager.getCurrentUser().getUserId();
        if (workTask == null && StringUtils.isEmpty(workTask.getId())) {
            throw new BusinessException("-1", "任务不存在。");
        }
        String id = workTask.getId();

        //1.清空日志表 保留审核不通过的日志
        workTaskLogRepository.deleteByWorkTaskIdExceptOperType(id, "6");
        //2.清空任务分配表
        workTaskAssignRepository.deleteByWorkTaskId(id);

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(currentUserId);
        log.setUpdateDate(new Date());
        log.setUpdateUser(currentUserId);
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        log.setOperContent("");
        log.setUserId(currentUserId);
        log.setOperType("1"); //指派


        WorkTask w = workTaskRepository.findOne(id);
        workTask.setCreateUser(w.getCreateUser());
        workTask.setCreateDate(w.getCreateDate());


        workTask.setUpdateUser(currentUserId);
        workTask.setUpdateDate(new Date());
        workTask.setBsflag("N");
        workTask.setCurrentState("1"); //未响应
        workTaskRepository.save(workTask);
        log.setWorkTask(workTask);
        workTaskLogRepository.save(log);

        WorkTaskAssign assign = null;
        String users[] = userIds.split(",");
        List<WorkTaskAssign> list = new ArrayList();
        for (String userId : users) {
            assign = new WorkTaskAssign();
            assign.setBsflag("N");
            assign.setCreateDate(new Date());
            assign.setCreateUser(currentUserId);
//            assign.setIsManager("");
            assign.setProjectId(workTask.getProjectId());
            assign.setTaskResponse("");
            assign.setUserId(userId);
            assign.setWorkTask(workTask);
            list.add(assign);
        }
        workTaskAssignRepository.save(list);
        return "1";
    }

    @Override
    @Transactional
    public String audit(String workTaskId, String oprType) throws BusinessException {
        WorkTask workTask = workTaskRepository.findOne(workTaskId);
        String currentUserId = SpringManager.getCurrentUser().getUserId(); //SpringManager.getCurrentUser().getUserId();
        if (workTask == null) {
            throw new BusinessException("-1", "任务不存在。");
        }

        WorkTaskLog log = new WorkTaskLog();
        log.setBsflag("N");
        log.setCreateDate(new Date());
        log.setCreateUser(SpringManager.getCurrentUser().getUserId());
        log.setUpdateDate(new Date());
        log.setUpdateUser(SpringManager.getCurrentUser().getUserId());
        log.setOperTime(new Date());
        log.setProjectId(workTask.getProjectId());
        log.setOperContent("");
        log.setUserId(currentUserId);
        log.setWorkTask(workTask);

        if (StringUtils.isEmpty(oprType)) {
            throw new BusinessException("-1", "请输入审核结果");
        }

        if ("pass".equals(oprType)) {
            //1.关联的成果标称审核通过
            documentRepository.audit(workTask.getProjectId(), workTask.getId(), "2", "");
            //2. 审核通过日志
            log.setOperType("5");
            //3. 任务状态已完成
            workTask.setCurrentState("5");
        } else if ("refuse".equals(oprType)) {
//            documentRepository.audit(workTask.getProjectId(), workTask.getId(), "3", "");
            log.setOperType("6");
            workTask.setCurrentState("7");
        } else {
            throw new BusinessException("-2", "审核结果只能是通过或者拒绝。");
        }
        workTask.setUpdateUser(currentUserId);
        workTask.setUpdateDate(new Date());

        workTaskLogRepository.save(log);
        workTaskRepository.save(workTask);
        return "1";
    }

    @Override
    public List<WorkTaskLog> findWorkTaskLog(String workTaskId, String userId, String status) throws BusinessException {

        if (StringUtils.isEmpty(workTaskId)) {
            throw new BusinessException("-1", "任务不存在。");
        }

        List<WorkTaskLog> list = workTaskLogRepository.findAll(new Specification<WorkTaskLog>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                if (!StringUtils.isEmpty(userId)) {
                    predicateList.add(cb.equal(root.get("userId"), userId));
                }

                if (!StringUtils.isEmpty(status)) {
                    predicateList.add(cb.equal(root.get("operType"), status));
                }
                predicateList.add(cb.equal(root.get("workTask").get("id"), workTaskId));
                return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        });

        return list;
    }

    private boolean isAllPass(String workTaskId) {
        List<WorkTaskAssign> userList = workTaskAssignRepository.findByWorkTaskId(workTaskId);
        for (WorkTaskAssign assign : userList) {
            if (!"Y".equals(assign.getTaskResponse())) {
                return false;
            }
        }
        return true;
    }

    private boolean isWorkTaskUser(String workTaskId, String userId) {
        List<WorkTaskAssign> list = workTaskAssignRepository.findByWorkTaskIdAndUserId(workTaskId, userId);
        if (!list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Object> taskProgressStatisticalByCurrentState(String projectId) {
        return workTaskRepository.findGroupByCurrentState(projectId, "N");
    }

    private List genList(int listSize) {
        List result = new ArrayList();
        for (int i = 0; i < listSize; i++) {
            result.add(0);
        }
        return result;
    }

    /**
     * 生成任务状态统计数据
     *
     * @param listSize
     * @return
     */
    private List<Map<String, Object>> genProgressInfo(int listSize) {
        List result = new ArrayList();

        Map map1 = new HashMap();
        map1.put("name", worktask_status.get("1"));
        map1.put("data", genList(listSize));
        result.add(map1);

        Map map2 = new HashMap();
        map2.put("name", worktask_status.get("2"));
        map2.put("data", genList(listSize));
        result.add(map2);

        Map map3 = new HashMap();
        map3.put("name", worktask_status.get("3"));
        map3.put("data", genList(listSize));
        result.add(map3);

        Map map4 = new HashMap();
        map4.put("name", worktask_status.get("4"));
        map4.put("data", genList(listSize));
        result.add(map4);

        Map map5 = new HashMap();
        map5.put("name", worktask_status.get("5"));
        map5.put("data", genList(listSize));
        result.add(map5);

        Map map6 = new HashMap();
        map6.put("name", worktask_status.get("6"));
        map6.put("data", genList(listSize));
        result.add(map6);

        Map map7 = new HashMap();
        map7.put("name", worktask_status.get("7"));
        map7.put("data", genList(listSize));
        result.add(map7);

        Map map8 = new HashMap();
        map8.put("name", "总任务");
        map8.put("data", genList(listSize));
        result.add(map8);

        return result;
    }

    @Override
    public Map<String, List> taskProgressStatisticalByUser(String projectId) {
        Map<String, List> result = new HashMap<>();
        result.put("user", new ArrayList());


        List<Object[]> userTaskStateList = workTaskRepository.countUserTaskState(projectId);

        Map<String, List<String>> user_state = new HashMap();


        for (Object[] objArr : userTaskStateList) {
            if (user_state.containsKey(objArr[0])) {
                user_state.get("" + objArr[0]).add(objArr[1] + "_" + objArr[2]);
            } else {
                List stateCountList = new ArrayList();
                stateCountList.add(objArr[1] + "_" + objArr[2]);
                user_state.put("" + objArr[0], stateCountList);
            }
        }

        List<Map<String, Object>> progressInfoList = genProgressInfo(user_state.size());
        result.put("progressInfo", progressInfoList);

        int count = 0;
        for (Map.Entry<String, List<String>> entry : user_state.entrySet()) {
            result.get("user").add(authorityRepository.getUserDisplayName(entry.getKey()));
            List<String> stateCountList = entry.getValue();
            int countTask = 0;

            for (String str : stateCountList) {
                for (Map<String, Object> map : progressInfoList) {
                    String[] arr = str.split("_");
                    if (map.get("name").equals(worktask_status.get(arr[0]))) {
                        List l = (List) map.get("data");
                        l.set(count, Integer.parseInt(arr[1]));
                        countTask += Integer.parseInt(arr[1]);
                        break;
                    }
                }
            }

            Map<String, Object> sumMap = progressInfoList.get(progressInfoList.size() - 1);
            List sumList = (List) sumMap.get("data");
            sumList.set(count, countTask);
            count++;
        }
        return result;
    }

    @Override
    public Map<String, Object> findCountByProjectId(String projectId) {
        Map<String, Object> countMap = new HashMap<>();
        List<Object[]> countList = workTaskRepository.findByprojectIdForStatus(projectId);
        for (Object[] obj : countList) {
            countMap.put((String) obj[0], obj[1]);
        }
        return countMap;
    }


    @Override
    public List<TreeJsonDto> getToolFullTree(String worktaskId) {
        List<Tool> toolList = workTaskToolRepository.findToolsByTaskId(worktaskId);
        List<ToolType> tooltypeList = toolRepository.findToolTypeTree();

        Map<String, ToolType> map = new HashMap<>();

//        for (ToolType type : tooltypeList){
//            map.put(type.getIcon(),type);
//            if (type.getChildren() != null && !type.getChildren().isEmpty()) {
//                for (ToolType type_inner : type.getChildren()) {
//                    type_inner.setName("123");
//                    map.put(type_inner.getIcon(),type_inner);
//                }
//            }
//        }
        parseListToolTypeToMap(tooltypeList, map);
        ToolType tooType = null;
        for (Tool tool : toolList) {
            String toolTypeId = tool.getToolType().getId();
            if (map.containsKey(toolTypeId)) {
                tooType = map.get(toolTypeId);
                if (tooType.getElements() != null && !tooType.getElements().isEmpty()) {
                    tooType.getElements().add(tool);
                } else {
                    List<Tool> list = new ArrayList<>();
                    list.add(tool);
                    tooType.setElements(list);
                }
            }

        }

        List<ToolType> resultList = new ArrayList<ToolType>(tooltypeList);
        toolTypeClean(resultList);

        List<TreeJsonDto> ret = new ArrayList<>();
        toolTypeChildren(resultList, ret);

        return ret;
    }

    private void toolTypeChildren(List<ToolType> toolTypelist, List<TreeJsonDto> dtoList) {
        TreeJsonDto dto = null;
        for (ToolType type : toolTypelist) {
            dto = new TreeJsonDto();
            dto.setIconCls(type.getIcon());
            dto.setId(type.getId());
            //dto.setChecked();
            dto.setState("open");
            dto.setText(type.getName());
            dto.setChildren(new ArrayList<TreeJsonDto>());

            if (type.getChildren() != null && !type.getChildren().isEmpty()) {
                dtoList.add(dto);
                toolTypeChildren(type.getChildren(), dto.getChildren());
            }

            if (type.getElements() != null && !type.getElements().isEmpty()) {
                List<Tool> tools = type.getElements();
                List<TreeJsonDto> elements = new ArrayList<>();
                TreeJsonDto e = null;

                if (tools.size() > 0) {
                    if (!dtoList.contains(dto)) {
                        dtoList.add(dto);
                    }
                }

                for (Tool tool : tools) {
                    e = new TreeJsonDto();
                    e.setText(tool.getName());
                    e.setId(tool.getId());
                    e.setState("open");
                    e.setChecked(false);
                    e.setIconCls(tool.getIcon());
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("configParam", tool.getConfigParam());
                    attributes.put("toolPath", tool.getToolPath());
                    attributes.put("icon", tool.getIcon());
                    e.setAttributes(attributes);
                    elements.add(e);
                }
                dto.getChildren().addAll(elements);
            }
        }
    }

    private void toolTypeClean(List<ToolType> list) {

        Iterator<ToolType> it = list.iterator();
        while (it.hasNext()) {
            ToolType type = it.next();
            if (type.getChildren() != null && !type.getChildren().isEmpty()) {
                toolTypeClean(type.getChildren());
                //清除无子节点的父节点
                if ((type.getElements() == null || type.getElements().isEmpty()) && (type.getChildren() == null || type.getChildren().isEmpty())) {
                    it.remove();
                }
            }
            if (type.getChildren() == null && type.getElements().isEmpty()) {
                it.remove();
            }
        }
    }

    private void parseListToolTypeToMap(List<ToolType> tooltypeList, Map<String, ToolType> map) {
        for (ToolType type : tooltypeList) {
            map.put(type.getId(), type);
            if (type.getChildren() != null && !type.getChildren().isEmpty()) {
                parseListToolTypeToMap(type.getChildren(), map);
            }
        }
    }


    private void updateTypeList(List<Map<String, Object>> resultlist, Map<String, Object> map, List<Map> deskTopSatellite) {
        if (!"本地软件".equals("" + map.get("softName"))) {
            findSatellite(deskTopSatellite, map);
        }

        for (Map<String, Object> app : resultlist) {
            if (app.get("name").equals(map.get("softName"))) {
                List<Map<String, Object>> appList = null;
                if (app.get("softwareList") != null) {
                    appList = (List<Map<String, Object>>) app.get("softwareList");
                    appList.add(map);
                } else {
                    appList = new ArrayList<>();
                    appList.add(map);
                }
                return;
            }
        }
        Map<String, Object> app = new HashMap<>();
        app.put("id", map.get("softName").toString() + "_" + map.get("softName").toString());
        app.put("name", map.get("softName").toString());

        List<Map<String, Object>> softwareList = new ArrayList<>();
        softwareList.add(map);
        app.put("softwareList", softwareList);
        resultlist.add(app);

    }

    /**
     * 匹配专业软件树和桌面云
     * <p>
     * 桌面云中的softname和ip同时相等的时候匹配，
     * 匹配的同时将当前记录在桌面云列表进行删除，
     * 如果桌面云的softname有多个，删除当前的，保留剩余的，直到最后一个被匹配才删除此系统的桌面云。
     * <p>
     * 桌面云列表剩下的就是未匹配的记录。再后期处理下，将softname中的记录进行扩展记录条数。用于前台展示
     *
     * @param satelliteList 桌面云列表
     * @param software      专业软件
     */
    private void findSatellite(List<Map> satelliteList, Map<String, Object> software) {
        Iterator it = satelliteList.iterator();
        while (it.hasNext()) {
            Map<String, Object> desktop = (Map<String, Object>) it.next();
            String destop_softname = desktop.get("softname").toString();

            //测试数据
//            if (destop_softname.indexOf(""+software.get("softName")) >= 0) {
//                desktop.put("ip","10.88.110.201");
//            }

            if (software.get("hostip").equals(desktop.get("ip")) && destop_softname.indexOf("" + software.get("softName")) >= 0) {
                //清除已经匹配的软件名称,为最后的生成的树做准备
                List<String> destopSoftnameListSplit = Arrays.asList(destop_softname.split(","));
                List<String> destopSoftnameList = new ArrayList<>();
                for (String str : destopSoftnameListSplit) {
                    if (!"".equals(str) && !software.get("softName").toString().equals(str)) {
                        destopSoftnameList.add(str);
                    }
                }

                desktop.put("softname", String.join(",", destopSoftnameList));

                software.put("desktop_system", desktop.get("xitong"));
                software.put("desktop_pass", desktop.get("pass"));
                software.put("desktop_port", desktop.get("duankou"));
                software.put("desktop_ip", desktop.get("ip"));
                software.put("desktop_name", desktop.get("name"));

                if (destopSoftnameList.size() == 0) {
                    it.remove();
                }
                return;
            }
        }

    }

    @Override
    public List<ProjectUserSoftware> getSoftwareAccount(String projectId, String satelliteId) {
        String userId = SpringManager.getCurrentUser().getUserId();
        return projectUserSoftwareRepository.findBySatelliteIdAndProjectIdAndUserIdAndBsflag(satelliteId, projectId, userId, "N");
    }

    /**
     * 获取系统配置的卫星端
     *
     * @return
     */
    private List<Map> getDeskTopSatellite() {
//        String url = "http://softwaredesktop.pcep.cloud/RestService/DGzhuomian?uid=a01ldfh5yth6cda8qg602u5j";
        String url = "http://softwaredesktop.pcep.cloud/RestService/DGzhuomian?uid=" + SpringManager.getCurrentUser().getUserId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String token = details.getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.add("Authorization", "Bearer " + token);
        HttpEntity httpEntity = new HttpEntity(headers);
        RestTemplate rt = new RestTemplate();

        String str = rt.exchange(url, HttpMethod.GET, httpEntity, String.class).getBody();
        System.out.println(str);
        List<Map> list = JSON.parseArray(str, Map.class);

//        List<Map<String,Object>> resultList = new ArrayList<>();
//        for (Map map : list) {
//            List<Map<String,Object>> weixingList = (List<Map<String, Object>>) map.get("weixing");
//            Map<String,Object> result = null;
//            for (Map<String,Object> weixingMap : weixingList) {
//                result = new HashMap<>();
//                result.put("desktop_pass",map.get("pass"));
//                result.put("desktop_port",map.get("duankou"));
//                result.put("desktop_ip",map.get("ip"));
//                result.put("desktop_name",map.get("name"));
//                result.put("desktop_system",map.get("xitong"));
//
//                result.put("satelliteName",weixingMap.get("Id"));
//                result.put("id",weixingMap.get("Id"));
//                result.put("hostIP",weixingMap.get("hostIP"));
//                result.put("softName",weixingMap.get("softName"));
////                result.putAll(map);
////                result.remove("weixing");
//                resultList.add(result);
//            }
//        }

//        Map<String, Object> result = null;
//        result = new HashMap<>();
//        result.put("pass", "pass");
//        result.put("duankou", "8080");
//        result.put("ip", "10.88.110.201");
//        result.put("name", "wang");
//        result.put("xitong", "windows");
//
//        result.put("satelliteName", "abc");
//        result.put("id", "GeoEast");
//        result.put("hostIP", "123");
//        result.put("softname", "GeoEast");
//        result.put("enable", "false");
//        list.add(result);
        return list;
    }

    @Autowired
    ApplicationRepository applicationRepository;

    @Override
    public List getWorkTaskApplicationTree(String id, String eoCode, String showSoftwareAccount, boolean isDesktop) {
        List<Map<String, Object>> list = getWorkTaskApplicationLeaf(id, eoCode, showSoftwareAccount, isDesktop);
        String dataRegion = workTaskRepository.findDataRegion(id);
        List<TreeJsonDto> treeList = applicationRepository.getApplicationTree(dataRegion);

        genAppTree(treeList, list);

        addCloudAppTreeNode(treeList, list);

        return treeList;

    }

    public void addCloudAppTreeNode(List<TreeJsonDto> tree, List<Map<String, Object>> cloudAppLeafNodes) {
        Map<String, List<TreeJsonDto>> cloudAppMap = new LinkedHashMap<>();
        TreeJsonDto desktopNodeChildren = null;
        for (Map<String, Object> map : cloudAppLeafNodes) {
            //cloudApp_rootName 在匹配软件列表时填充进云平台软件列表 标识为某一个云桌面
            if (map.containsKey("cloudApp_rootName")) {
                String cloudApp_rootName = "" + map.get("cloudApp_rootName");
                desktopNodeChildren = new TreeJsonDto();
                desktopNodeChildren.setId("" + map.get("id"));
                desktopNodeChildren.setText(map.get("id"));
                desktopNodeChildren.setState("");
                Map attribures = new HashMap();
                attribures.put("isElement", true);

                map.put("desktop_clouded", true);
                attribures.put("element", map);
                desktopNodeChildren.setAttributes(attribures);
                if (cloudAppMap.containsKey(cloudApp_rootName)) {
                    cloudAppMap.get(cloudApp_rootName).add(desktopNodeChildren);
                } else {
                    List<TreeJsonDto> cloudAppChildrenList = new ArrayList<>();
                    cloudAppChildrenList.add(desktopNodeChildren);
                    cloudAppMap.put(cloudApp_rootName, cloudAppChildrenList);
                }
            }
        }
        TreeJsonDto cloudAppNode = null;
        for (Map.Entry<String, List<TreeJsonDto>> entry : cloudAppMap.entrySet()) {
            String name = entry.getKey();
            List<TreeJsonDto> children = entry.getValue();
            if (children != null && !children.isEmpty()) {
                cloudAppNode = new TreeJsonDto();
                cloudAppNode.setState("open");
                cloudAppNode.setText(name);
                cloudAppNode.setId(name);
                Map cloudAppNodeAttr = new HashMap();
                cloudAppNodeAttr.put("isElement", false);
                cloudAppNode.setAttributes(cloudAppNodeAttr);
                cloudAppNode.setChildren(children);
                tree.add(cloudAppNode);
            }
        }
    }


    private Map containsSalellite(List<Map<String, Object>> salelliteInfoList, String salelliteInfoId) {
        if (StringUtils.isEmpty(salelliteInfoId)) {
            return null;
        }
        for (Map<String, Object> map : salelliteInfoList) {
            if (salelliteInfoId.equals(map.get("id"))) {
                return map;
            }
        }
        return null;
    }

    /**
     * 对叶子节点（数据集）查询根数据集，并将根数据集挂在原有叶子节点的位置，
     * 并带上原有节点，保留数据集路径轨迹。
     *
     * @param tree
     */
    public void genAppTree(List<TreeJsonDto> tree, List<Map<String, Object>> saleliteInfoList) {
        Iterator<TreeJsonDto> it = tree.iterator();
        while (it.hasNext()) {
            TreeJsonDto dto = it.next();
            dto.setState("open");
            if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
                genAppTree(dto.getChildren(), saleliteInfoList);
                if (dto.getChildren() == null || dto.getChildren().isEmpty()) {
                    it.remove();
                }
            } else if (dto.getAttributes() != null && dto.getAttributes() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) dto.getAttributes();
                //判断当前节点是否为数据集类型,是叶子节点，并且Attributes中的isElement为true为数据集
                if ((boolean) map.get("isElement")) {
                    Map salelliteInfoMap = containsSalellite(saleliteInfoList, dto.getId());
                    if (salelliteInfoMap == null) {
                        it.remove();
                    } else {
                        //用专业软件的服务返回的对象覆盖数据库中查询出的对象，
                        // 因为eocode可能会导致服务取出来的值与数据库中的不一致，以服务未准。
                        if (map.get("element") instanceof Map) {
                            map.put("element", salelliteInfoMap);
                        }
                    }
                } else {
                    it.remove();
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getWorkTaskApplicationLeaf(String id, String eoCode, String showSoftwareAccount, boolean isDesktop) {
        List<Map<String, Object>> list = getWorkTaskApplication(id, eoCode, showSoftwareAccount, isDesktop);

        List result = new ArrayList();

        for (Map<String, Object> map : list) {
            result.addAll((Collection) map.get("softwareList"));
        }

        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                if (o1.containsKey("cloudApp_rootName") || o2.containsKey("cloudApp_rootName")) {
                    return 0;
                } else {
                    String id1 = "" + o1.get("id");
                    String id2 = "" + o2.get("id");
                    return id1.compareTo(id2);
                }
            }
        });
        return result;
    }

    /**
     * 匹配专业软件树和桌面云
     * <p>
     * 桌面云中的softname和ip同时相等的时候匹配，
     * 匹配的同时将当前记录在桌面云列表进行删除，
     * 如果桌面云的softname有多个，删除当前的，保留剩余的，直到最后一个被匹配才删除此系统的桌面云。
     * <p>
     * 桌面云列表剩下的就是未匹配的记录。再后期处理下，将softname中的记录进行扩展记录条数。用于前台展示
     *
     * @param cloudAppObj 云平台软件
     * @param software    专业软件
     */
    private void matchSatellite(JSONObject cloudAppObj, Map<String, Object> software) {
        String rootName = cloudAppObj.getString("name");
        Iterator it = ((List<JSONObject>) cloudAppObj.get("softwareList")).iterator();
        while (it.hasNext()) {
            Map<String, Object> cloudApp = (Map<String, Object>) it.next();
            String destop_softname = cloudApp.get("softwareName").toString();

            if (!StringUtils.isEmpty(software.get("hostip")) && software.get("hostip").equals(cloudApp.get("softwareIp")) && destop_softname.indexOf("" + software.get("softName")) >= 0) {
                software.put("desktop_system", cloudApp.get("osType"));
                software.put("desktop_pass", cloudApp.get("softwarePassword"));
                software.put("desktop_port", cloudApp.get("softwarePort"));
                software.put("desktop_ip", cloudApp.get("softwareIp")); //最终展示页面用此字段作为判断是否是云桌面的依据
                software.put("desktop_name", cloudApp.get("softwareName"));
                software.put("desktop_openUrl", cloudApp.get("openUrl"));
                return;
            }
        }

    }

    /**
     * 匹配专业软件树和桌面云，并构建树结构，向卫星端中增加云桌面部分属性
     *
     * @param resultlist
     * @param satelliteMap 专业软件
     * @param cloudApp     云平台软件列表
     */
    private void adaptCloudApp(List<Map<String, Object>> resultlist, Map<String, Object> satelliteMap, JSONObject cloudApp) {
        if (!"本地软件".equals("" + satelliteMap.get("softName"))) {
            matchSatellite(cloudApp, satelliteMap);
        }

        for (Map<String, Object> app : resultlist) {
            if (app.get("name").equals(satelliteMap.get("softName"))) {
                List<Map<String, Object>> appList = null;
                if (app.get("softwareList") != null) {
                    appList = (List<Map<String, Object>>) app.get("softwareList");
                    appList.add(satelliteMap);
                } else {
                    appList = new ArrayList<>();
                    appList.add(satelliteMap);
                }
                return;
            }
        }
        Map<String, Object> app = new HashMap<>();
        app.put("id", satelliteMap.get("softName").toString() + "_" + satelliteMap.get("softName").toString());
        app.put("name", satelliteMap.get("softName").toString());

        List<Map<String, Object>> softwareList = new ArrayList<>();
        softwareList.add(satelliteMap);
        app.put("softwareList", softwareList);
        resultlist.add(app);

    }

    /**
     * 20210427 向云桌面中增加当前油田卫星端部分信息
     *
     * @param allsatellites
     * @param cloudAppObj
     */
    public void addSatellitesAttributeToCloudApp(List<Map<String, Object>> allsatellites, JSONObject cloudAppObj) {
        Iterator it = ((List<JSONObject>) cloudAppObj.get("softwareList")).iterator();
        JSONObject attributes = null;
        while (it.hasNext()) {
            Map<String, Object> cloudApp = (Map<String, Object>) it.next();
            String destop_softname = cloudApp.get("softwareName").toString();
            attributes = new JSONObject();
            attributes.put("serviceUrl", null);
            attributes.put("satelliteId", null);
            attributes.put("isSendable", null);
            attributes.put("sendableRange", null);
            attributes.put("isDownload", null);
            attributes.put("downloadRange", null);
            for (Map<String, Object> map : allsatellites) {
                if (destop_softname.equals(map.get("satelliteName"))) {
                    attributes.put("serviceUrl", map.get("serviceUrl"));
                    attributes.put("satelliteId", map.get("satelliteId"));
                    attributes.put("isSendable", map.get("isSendable"));
                    attributes.put("sendableRange", map.get("sendableRange"));
                    attributes.put("isDownload", map.get("isDownload"));
                    attributes.put("downloadRange", map.get("downloadRange"));
                    break;
                }
            }
            cloudApp.put("asiAttributes", attributes);
        }
    }

    @Override
    public List<Map<String, Object>> getWorkTaskApplication(String id, String eoCode, String showSoftwareAccount, boolean isDesktop) {
        return getWorkTaskApplication(id, eoCode, showSoftwareAccount, isDesktop, true);
    }

    public List<Map<String, Object>> getWorkTaskApplication(String id, String eoCode, String showSoftwareAccount, boolean isDesktop, boolean isAutoAdapteCloudApp) {
        String dataRegion = workTaskRepository.findDataRegion(id);
        List<WorkTaskSoftware> list = workTaskSoftwareRepository.findByTaskId(id);
        List<Map<String, Object>> satellites = new ArrayList<>();

        List<Map<String, Object>> allsatellites = satellitesRepository.findList(eoCode, dataRegion);

        for (WorkTaskSoftware appProject : list) {
            List<ProjectUserSoftware> projectUserSoftwareList = null;
            for (Map<String, Object> map : allsatellites) {
                if (appProject.getSoftwareId().equals(map.get("satelliteId"))) {
                    if (!StringUtils.isEmpty(showSoftwareAccount) && "Y".equals(showSoftwareAccount)) {
                        projectUserSoftwareList = getSoftwareAccount(id, "" + map.get("satelliteId"));
                        map.put("accountList", projectUserSoftwareList);
                    }
                    satellites.add(map);
                    break;
                }
            }
        }


        List<Map<String, Object>> resultlist = new ArrayList<>();

        List<JSONObject> cloudPlatformAppList = null;

        if (isDesktop) {
            try {
                cloudPlatformAppList = applicationRepository.formatCloudPlatformAppList(null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cloudPlatformAppList != null && !cloudPlatformAppList.isEmpty()) {
                if (isAutoAdapteCloudApp) {
                    for (JSONObject obj : cloudPlatformAppList) {
                        for (Map<String, Object> map : satellites) {
                            adaptCloudApp(resultlist, map, obj);
                        }
                        addSatellitesAttributeToCloudApp(allsatellites, obj);
                    }
                }
                resultlist.addAll(cloudPlatformAppList);
            } else {
                JSONObject obj = new JSONObject();
                obj.put("id", "satellites");
                obj.put("name", "satellites");
                obj.put("softwareList", satellites);
                resultlist.addAll(new ArrayList<>(Arrays.asList(obj)));
            }
        } else {
            JSONObject obj = new JSONObject();
            obj.put("id", "satellites");
            obj.put("name", "satellites");
            obj.put("softwareList", satellites);
            resultlist.addAll(new ArrayList<>(Arrays.asList(obj)));
        }

//        if (deskTopSatellite.size() > 0) {
//            Map<String,Object> desktop = new HashMap<>();
//            desktop.put("id","软件云桌面");
//            desktop.put("name","软件云桌面");
//            desktop.put("softwareList",updateDeskTopSatellite(deskTopSatellite));
//            resultlist.add(desktop);
//        }

        return resultlist;
    }

    private List<Map> updateDeskTopSatellite(List<Map> list) {
        List<Map> result = new ArrayList<>();
        for (Map map : list) {
            String[] arrSoftname = map.get("softname").toString().split(",");
            Map resultMap = null;
            for (String name : arrSoftname) {
                resultMap = new HashMap();
                resultMap.put("softName", name);
                resultMap.put("desktop_system", map.get("xitong"));
                resultMap.put("desktop_pass", map.get("pass"));
                resultMap.put("desktop_port", map.get("duankou"));
                resultMap.put("desktop_ip", map.get("ip"));
                resultMap.put("desktop_name", map.get("name"));

                resultMap.put("desktop_clouded", true);

                resultMap.put("id", name + "(云)");
                resultMap.put("satelliteName", name + "(云)");
                resultMap.put("name", name + "(云)");

                result.add(resultMap);
            }
        }
        return result;
    }

//    @Override
//    public List<SrMetaDataset> getDataset(String worktaskId){
//        return workTaskDataSetRepository.findDatasetByTaskId(worktaskId);
//    }

    @Override
    public List<SrMetaDataset> getDataset(String taskId) {
        String str = "select * from sr_meta_dataset b where b.IS_DISPLAY = 'Y' and b.BSFLAG = 'N' and b.dataset_id in (select distinct dataset_id\n"
                + "from ( with RECURSIVE cte as (select a.dataset_id,cast(a.dataset_name as varchar(100)) dataset_name,\n"
                + "a.p_dataset_id from sr_meta_dataset a where a.dataset_id in(select distinct dataset_id\n"
                + "from sr_project_task_dataset f where f.task_id ='" + taskId + "') union all \n"
                + "select b.dataset_id,cast(c.dataset_name ||'>' ||b.dataset_name as varchar(100)) as dataset_name,b.p_dataset_id\n"
                + "from sr_meta_dataset b inner join cte c on b.dataset_id = c.p_dataset_id )\n"
                + "select * from cte where not exists(select * from sr_meta_dataset  where b.view_code is null and tree_id is null and\n"
                + "dataset_id = cte.dataset_id)) ee) order by convert_to(b.dataset_name, 'GBK')";
//        String str = "  select * from sr_meta_dataset b where b.IS_DISPLAY='Y' and b.BSFLAG='N' and b.dataset_id in (select distinct  dataset_id from (\n" +
//                "with RECURSIVE cte as (select a.dataset_id,cast(a.dataset_name as varchar(100)) dataset_name,a.p_dataset_id from sr_meta_dataset a where\n" +
//                "a.dataset_id in (select distinct dataset_id from SR_PROJECT_WORKROOM_TREE f  where f.workroom_id='"+projectId+"'  )\n" +
//                "union all\n" +
//                "select b.dataset_id,cast(c.dataset_name||'>'||b.dataset_name as varchar(100)) as dataset_name,b.p_dataset_id from sr_meta_dataset b inner join cte c on  b.dataset_id=c.p_dataset_id)\n" +
//                "select * from cte) ee) order by convert_to(b.dataset_name,'GBK')";

        List<SrMetaDataset> datasetList = jdbcTemplate.query(str, new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int i) throws SQLException {
                SrMetaDataset d = new SrMetaDataset();
                d.setId(rs.getString("dataset_id"));
                d.setPDatasetId(rs.getString("p_dataset_id"));
                d.setName(rs.getString("DATASET_NAME"));
                d.setCode(rs.getString("DATASET_CODE"));
                d.setBsflag(rs.getString("BSFLAG"));
                d.setCreateDate(rs.getDate("CREATE_DATE"));
                d.setCreateUser(rs.getString("CREATE_USER"));
                d.setDatasetType(rs.getString("DATASET_TYPE"));
                d.setEoCode(rs.getString("EO_CODE"));
                d.setFileTitleFormat(rs.getString("FILE_TITLE_FORMAT"));
                d.setIconCls(rs.getString("ICON"));
                d.setIsBaseDataset(rs.getString("IS_BASE_DATASET"));
                d.setIsDisplay(rs.getString("IS_DISPLAY"));
                d.setIsDrillDown(rs.getString("IS_DRILL_DOWN"));
                d.setIsValid(rs.getString("IS_VALID"));
                d.setRemarks(rs.getString("REMARKS"));
                d.setSortingCondition(rs.getString("SORTING_CONDITION"));
                d.setTreeId(rs.getString("TREE_ID"));
                d.setType(rs.getString("TYPE"));
                d.setUpdateDate(rs.getDate("UPDATE_DATE"));
                d.setUpdateUser(rs.getString("UPDATE_USER"));
                return d;
            }
        });
        return datasetList;

//        List<SrMetaDataset> list = projectBusinessNodeDataSetRepository.findDataSetByProjectIdDistinct(projectId);
//        List<SrMetaDataset> parentList = new ArrayList<>();
//        SrMetaDataset parent = null;
//        for (SrMetaDataset m : list) {
//            parent = dataSetRepository.findRoot(m.getId());
//            if (parent != null && !parentList.contains(parent) && !parent.getId().equals(m.getId())) {
//                parentList.add(parent);
//            }
//        }
//        if (parentList.size() > 0) {
//            list.addAll(parentList);
//        }
//        return list;
    }


    public List<WorkTaskBusinessNode> findBusinessNodeByTaskIdAndParentIsNull(String id) {
        return workTaskBusinessNodeRepository.findBusinessNodeByTaskIdAndParentIsNull(id);
    }

    @Override
//    @Cacheable(cacheNames = "ProjectFulltree", key = "#p1 + T(com.alibaba.fastjson.JSON).toJSONString(#p0)")
    public List<TreeJsonDto> getBusinessNodeFullTree(TreeCondiDto[] treeCategoryPamra,
                                                     String taskId, String dataRegion) {
        //依靠前台的initParams和QueryParams是否包含projectId来区分是否是第一次请求
        List<TreeJsonDto> ret = null;
        if (!StringUtils.isEmpty(taskId)) {

            InOutProcessor<TreeJsonDto, TreeJsonDto> processor = new InOutProcessor<TreeJsonDto, TreeJsonDto>() {
                @Override
                public TreeJsonDto process(TreeJsonDto treeJsonDto) throws Exception {
                    Map attributes = (Map) treeJsonDto.getAttributes();
                    if ((boolean) attributes.get("isElement")) {
                        SrMetaDataset dataSet = (SrMetaDataset) attributes.get("element");
                        if (dataSet != null && "N".equals(dataSet.getBsflag()) && "Y".equals(dataSet.getIsDisplay())) {
                            return treeJsonDto;
                        } else {
                            return null;
                        }
                    }
                    return treeJsonDto;
                }
            };

            List<WorkTaskBusinessNode> list = findBusinessNodeByTaskIdAndParentIsNull(taskId);
            TreeCondiDto[] condition = new TreeCondiDto[list.size()];
            TreeCondiDto dto = null;
            WorkTaskBusinessNode businessNode = null;
            for (int i = 0; i < list.size(); i++) {
                dto = new TreeCondiDto();
                businessNode = list.get(i);
                dto.setRepositoryName("workTaskBusinessNodeRepository");
                dto.setTreeCode(businessNode.getId());
                dto.setComplex(true);
                dto.setEfId("id");
                dto.setNeedAttr(true);
                dto.setContainSelf(true);
                dto.setEfText("name");
                condition[i] = dto;
                condition[i].setProcessor(processor);
            }
            ret = fullTree(condition, 0, 0);
        } else {
            ret = new ArrayList<TreeJsonDto>();
        }
        genDatasetList(dataRegion);
        genDataSetParent(ret);
        return ret;
    }


    @Override
    public List<WorkTaskBusinessNode> getNodesByName(String taskId, String name, String type) {
        if (type.equals("eq")) {
            return workTaskBusinessNodeRepository.findByTaskIdAndName(taskId, name);
        } else {
            return workTaskBusinessNodeRepository.findByTaskIdAndNameLike(taskId, "%" + name + "%");
        }

    }

    @Override
    public String renameBusinessNode(String businessNodeId, String name) {
        WorkTaskBusinessNode node = workTaskBusinessNodeRepository.findOne(businessNodeId);
        if (node == null) {
            //节点不存在
            return "-1";
        } else {
            node.setName(name);
            node.setUpdateDate(new Date());
            node.setUpdateUser(SpringManager.getCurrentUser().getUserId());
            workTaskBusinessNodeRepository.save(node);
            return "1";
        }
    }


    @Override
    public List<SrMetaDataset> getDatasetTree(String taskId, String dataRegion) {
        List<String> datasetIdList = workTaskDataSetRepository.findDataSetByTaskIdDistinct(taskId);
        return dataSetRepository.findTree(org.apache.commons.lang3.StringUtils.join(datasetIdList, ","), dataRegion);
    }

    public String sceneTaskSync(String taskId, Map<String, Object> param) throws Exception {
        long startTime = System.currentTimeMillis();
        //查询任务
        WorkTask task = this.findById(taskId);

        String userIds = task.getWorkTaskAssigns().stream().map(i -> i.getUserId()).collect(Collectors.joining(","));

        List<SrMetaDataset> oldDsList = getDataset(taskId);
        List<Map<String, Object>> datasetList = new ArrayList<>();
        datasetList.addAll(((List<Map<String, Object>>) param.get("dataSet")));
        List<String> dsIds = new ArrayList<>();
        datasetList.stream().forEach(i -> {
            i.put("name", i.get("text"));
            dsIds.add(i.get("id").toString());
        });
        List<Map<String, Object>> dsList = oldDsList.stream().filter(i -> !dsIds.contains(i.getId())).map(i -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", i.getId());
            map.put("text", i.getName());
            return map;
        }).collect(Collectors.toList());
        datasetList.addAll(dsList);
//        System.out.println(datasetList);
        //集合排序
        Collections.sort(datasetList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(map1.get("text").toString(), map2.get("text").toString());
            }
        });
//        System.out.println(datasetList);
//        根据任务id获取树结构
        getBusinessNodeFullTree(null, taskId, null);

        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("id", ShortUUID.randomUUID());
        nodeMap.put("text", "数据集");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("isElement", false);
        nodeMap.put("attributes", attributes);

        nodeMap.put("dataSetList", datasetList);

        //获取库里已经存在的业务结点
        List<WorkTaskBusinessNode> nodeList = workTaskBusinessNodeRepository.findByTaskId(taskId);
        List<String> nodeIdList = new ArrayList<String>();
        //确定更新的业务结点（区别新增的）
        String nodes = JSONObject.toJSONString(Arrays.asList(nodeMap));
        findUpdataBusinessNode(nodes, nodeList, nodeIdList);

        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.parse(nodes);
        deleteNodes(taskId, nodeIdList);
        if (list != null && !list.isEmpty()) {
            saveBusinessNode(taskId, list);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String token = details.getTokenValue();

        //TODO 批量同步数据逻辑，异步调用
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // 异步同步数据
                dataSetRepository.syncIndex(task.getProjectId(), taskId, param, token);
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;
                System.out.println(Thread.currentThread().getName() + "异步线程同步数据耗时" + time);
            }
        });
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        System.out.println(Thread.currentThread().getName() + "主线程耗时" + time);
        return "1";
    }

    @Override
    public String saveFullTreeDataByTask(String taskId, String dataRegion, String nodes) throws Exception {
        try {
            WorkTask task = this.findById(taskId);
            //項目id
            String projectId = task.getProjectId();
            //获取库里存在节点
            List<WorkTaskBusinessNode> taskBusinessNodes = workTaskBusinessNodeRepository.findByTaskId(taskId);

            //获取之前节点数据集信息
            List<String> datasetIds = new ArrayList<>();
            for (WorkTaskBusinessNode taskBusinessNode : taskBusinessNodes) {
                List<WorkTaskDataSet> rels = taskBusinessNode.getRels();
                if (rels.size() > 0) {
                    for (WorkTaskDataSet rel : rels) {
                        datasetIds.add(rel.getDataSet().getId());
                    }
                }
            }
            //保存节点信息
            List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.parse(nodes);
            List<String> ids = new ArrayList<String>();
            for (Map<String, Object> map : list) {
                List<Map<String, Object>> datasetList = (List<Map<String, Object>>) map.get("dataSetList");
                for (Map<String, Object> objectMap : datasetList) {
                    ids.add(objectMap.get("id").toString());
                }
            }
            if (datasetIds.size() > 0) {
                if (ids.size() > 0) {
                    //删除更新之后不包含数据集id的索引数据
                    for (String datasetId : datasetIds) {
                        if (!ids.contains(datasetId)) {
//                            dataSetRepository.delete(projectId,datasetId,taskId);
                            SrMetaDataset dataset = dataSetRepository.findOne(datasetId);
                            if("结构化".equals(dataset.getType())|| "分析化验结构化".equals(dataset.getType())) {
                                //查询该任务需要删除数据集crp数据
                                List<Map<String, Object>> data = dataSetRepository.searchData(projectId, datasetId, taskId, new PageRequest(0, Integer.MAX_VALUE));
                                List<String> dsids = new ArrayList<String>();
                                for (Map<String, Object> d : data) {
                                    dsids.add(d.get("DSID").toString());
                                }
                                //删除数据集crp数据
                                if (dsids.size()>0){
                                    String result = metadataRepository.crpDelete(projectId, dataset.getViewCode(), dsids);
                                }
                            }
                        }
                    }
                }
            }
            //节点id
            List<String> nodeIdList = new ArrayList<String>();
            findUpdataBusinessNode(nodes, taskBusinessNodes, nodeIdList);
            deleteNodes(taskId, nodeIdList);
            if (list != null && !list.isEmpty()) {
                saveBusinessNode(taskId, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1";
    }


    public void sentMessAndTantu(WorkTask workTask, String userIds) {
        Map mapProject = JSONObject.parseObject(JSONObject.toJSONString(getProject(workTask.getProjectId())), Map.class);
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
            mapTantu.put("routing", "http://" + host + "/front/research/platform/index.html?projectId=" + workTask.getProjectId() + "&portalcontain=title&projectType=workroom");
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
