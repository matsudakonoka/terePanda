package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.pojo.vo.ChargeUserVo;
import com.cnpc.epai.core.workscene.service.TreeService;
import com.cnpc.epai.core.workscene.service.WorkNavigateTreeNodeService;
import com.cnpc.epai.core.workscene.service.WorkSceneTaskService;
import com.cnpc.epai.core.workscene.service.WorkService;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.SrWorkNode;
import com.cnpc.epai.core.worktask.domain.SrWorkTask;
import com.cnpc.epai.core.worktask.domain.SrWorkMsg;
import com.cnpc.epai.core.worktask.repository.AuthorityRepository;
import com.cnpc.epai.core.worktask.repository.SrWorkNodeRepository;
import com.cnpc.epai.core.worktask.repository.SrWrokTaskRepository;
import com.cnpc.epai.core.worktask.repository.SrWorkMsgRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SrWorkTaskServiceImpl implements SrWorkTaskService {

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Autowired
    SrWorkMsgRepository srWorkMsgRepository;

    @Autowired
    SrWrokTaskRepository srWrokTaskRepository;

    @Autowired
    SrWorkNodeRepository srWorkNodeRepository;

    @Autowired
    WorkObjectService workObjectService;

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    private TreeService treeService;
    @Autowired
    private WorkSceneTaskService workSceneTaskService;
    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;
    @Autowired
    private WorkService workService;
    //首页-业务场景筛选列表
    @Override
    public JSONArray getScenesTemplate(HttpServletRequest request) {
        String userId = SpringManager.getCurrentUser().getUserId();
        List<SrWorkMsg> byChargeUser = srWorkMsgRepository.findByChargeUser(userId);
        ArrayList<SrWorkMsg> collect = byChargeUser.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrWorkMsg::getTemplateId))), ArrayList::new));
        JSONArray objects = new JSONArray();
        for (SrWorkMsg workMsg : collect) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("templateId",workMsg.getTemplateId());
            jsonObject1.put("templateName",workMsg.getTemplateName());
            objects.add(jsonObject1);
        }
        return objects;
    }

    //首页-工作列表筛选、搜索按钮
    @Override
    public Page<SrWorkTask> getWorkList(Pageable page, JSONObject data) {
        String userId = SpringManager.getCurrentUser().getUserId();
        String templateId = data.getString("templateId");
        String taskName = data.getString("taskName");
        String sfbg = data.getString("sfbg");
        String workids="";
        if(StringUtils.equals(sfbg,"Y")){
            QueryWrapper<Work> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("report_template_id");
            queryWrapper.ne("report_template_id","");
            queryWrapper.eq("template_id",templateId);
            List<Work> worklist = workService.list(queryWrapper);
            for(int i=0;i<worklist.size();i++){
                if(i==0){
                    workids = workids + worklist.get(i).getWorkId();
                }else{
                    workids = workids +","+ worklist.get(i).getWorkId();
                }
            }
        }
        String finalWorkids = workids;
        Specification<SrWorkTask> spec = new Specification<SrWorkTask>() {
            @Override
            public Predicate toPredicate(Root<SrWorkTask> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("chargeUserId"),userId));
                if (!StringUtils.isEmpty(finalWorkids)) {
                    Predicate templateIds = root.get("workId").in(finalWorkids.split(","));
                    predicates.add(templateIds);
                }
                if (!StringUtils.isEmpty(templateId)) {
                    Predicate templateIds = root.get("templateId").in(templateId.split(","));
                    predicates.add(templateIds);
                }
                if (!StringUtils.isEmpty(taskName)) {
                    predicates.add(criteriaBuilder.like(root.get("taskName"), "%" + taskName + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<SrWorkTask> all = srWrokTaskRepository.findAll(spec, page);
        for (SrWorkTask workTask : all) {
            workTask.setGeoType(srWorkMsgRepository.findOne(workTask.getWorkId()).getGeoType());
        }
        return all;
    }

    /**
     *根据sortSequence字段对集合里的children降序排序
     * @param list 要排序的集合
     */
    public void sortResultsTree(List<Map<String, Object>> list){

        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (Integer) o1.get("sortSequence") - (Integer)o2.get("sortSequence");
            }
        });

        for (Map<String, Object> map : list) {

            Set<String> keys = map.keySet();

            for (String key : keys) {
                if (key.equals("children")){
                    sortResultsTree((List<Map<String, Object>>)map.get(key));
                }
            }
        }
    }

    //工作查看-工作成果导航&&工作成果导航关键词搜索
    @Override
    public Object getResultsTree(HttpServletRequest request, JSONObject data) {
        String resultKeyWords = data.getString("ResultKeyWords");
        String taskId = data.getString("taskId");
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //String treeId = data.getString("treeId");

        if (StringUtils.isEmpty(resultKeyWords)){
            //1、查询当前任务
            //SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
            //String[] srWorkNodes = workTask.getTreeNodeIds().split(",");

            //2、查询当前任务实例树
//            List<Tree> trees = getTree(request, workTask.getWorkId());
            List<Map<String, Object>> tree0 =treeService.getTree2(workTask.getWorkId(), null);
            //对tree0进行排序
            sortResultsTree(tree0);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",null);
            jsonObject.put("msg","操作成功！");
            jsonObject.put("flag",true);
            jsonObject.put("result",tree0);
            jsonObject.put("jumpUrl",null);
            return jsonObject;
//            List<Tree> trees= JSON.parseArray(JSON.toJSONString(tree0),Tree.class);
//            for (String srWorkNode : srWorkNodes) {
//                for (Tree tree : trees) {
//                    Tree treeByNodeId = getTreeByNodeId(tree, srWorkNode);
//                    if (treeByNodeId != null){
//                        treeByNodeId.setDisplay(true);
//                        String pnodeId = treeByNodeId.getPnodeId();
//                        if (org.apache.commons.lang.StringUtils.isNotEmpty(pnodeId)){
//                            setY(pnodeId,tree);
//                        }
//                    }
//                }
//            }
//            return trees;
        }else{
            QueryWrapper<WorkNavigateTreeNode> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tree_id",workTask.getWorkId());
            queryWrapper.eq("node_type","WORKUNIT");
            queryWrapper.like("node_name",resultKeyWords);

            List<WorkNavigateTreeNode> workTreeNodelist = workNavigateTreeNodeService.list(queryWrapper);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",null);
            jsonObject.put("msg","操作成功！");
            jsonObject.put("flag",true);
            jsonObject.put("result",workTreeNodelist);
            jsonObject.put("jumpUrl",null);
            return jsonObject;
            //1、查询当前任务
//        String srWorkNodes = workTask.getTreeNodeIds();
//        JSONArray node = findNode(request, workTask.getWorkId(), ResultKeyWords, srWorkNodes);
//        return node;
        }
    }

    //匹配节点，新增是否展示
    void setY(String pnodeId,Tree tree){
        Tree treeByNodeId = getTreeByNodeId(tree, pnodeId);
        treeByNodeId.setDisplay(true);
        if (treeByNodeId.getPnodeId()!=null){
            setY(treeByNodeId.getPnodeId(),tree);
        }
    }

    //节点关键词搜索
    JSONArray findNode(HttpServletRequest request, String treeId, String ResultKeyWords, String srWorkNodes){
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/findnode?name="+ResultKeyWords+"&nodeIds="+srWorkNodes);
        //serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/findnode?name="+ResultKeyWords+"&nodeIds="+srWorkNodes);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, String.class);
        String body = ex.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);

        System.out.println(jsonObject);

        return jsonObject.getJSONArray("result");
    }

    //工作查看-工作成果导航-数据集状态显示
    @Override
    public List<Tree> getResultsTreeStatus(HttpServletRequest request, JSONObject data) {
        String taskId = data.getString("taskId");
        String treeNodeId = data.getString("treeNodeId");
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());

        Tree tree1 = new Tree();
        for (Tree tree : trees) {
            Tree treeByNodeId = getTreeByNodeId(tree, treeNodeId);
            if (treeByNodeId != null){
                tree1 = treeByNodeId;
                break;
            }
        }

        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), null, tree1.getSourceNodeId(), null, null, null, null, null, null);
        ArrayList<Tree> returnList = new ArrayList<>();
        if (objectByUser.size()==0){
            for (Tree child : tree1.getChildren()) {
                returnList.add(child);
            }
            return returnList;
        }

        for (Tree child : tree1.getChildren()) {
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                if (child.getNodeId().equals(srTaskTreeData.getDatasetId())){
                    if (srTaskTreeData.getDataStatus().equals("已完成") || srTaskTreeData.getDataStatus().equals("已提交") ||srTaskTreeData.getDataStatus().equals("未完成") ||srTaskTreeData.getDataStatus().equals("已驳回")){
                        returnList.add(child);
                    }
                    else {
                        child.setStatus(true);
                        returnList.add(child);
                    }
                }
            }
        }
        ArrayList<Tree> collect = returnList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Tree::getNodeId))), ArrayList::new));
        return collect;
    }
    //工作查看-工作详情
    @Override
    public JSONObject getWorkInfoList(HttpServletRequest request, JSONObject data, Pageable page) {
        String taskId = data.getString("taskId");
        String workId = data.getString("workId");
        String treeNodeId = data.getString("treeNodeId");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                predicates.add(criteriaBuilder.equal(root.get("workId"),workId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);
//        添加权限人名称
        Map<String, WorkTask> workTaskMap = new HashMap<>();
        QueryWrapper<WorkTask> workTaskQueryWrapper = new QueryWrapper<>();
        workTaskQueryWrapper.eq("work_id",workId);
        List<WorkTask> listtask = workSceneTaskService.list(workTaskQueryWrapper);
        for(WorkTask obj : listtask){
            workTaskMap.put(workTask.getChargeUserId(),obj);
        }
        for (SrWorkNode workNode : workNodes) {
//            workNode.setUserName(SpringManager.getCurrentUser().getDisplayName());
            String[] charges = workNode.getChargeLatest().split(",");
            String UserName="";
            for (String charge : charges) {
                WorkTask Taskobj = workTaskMap.get(charge);
                if (Taskobj != null) {
                    if(StringUtils.isEmpty(UserName)){
                        UserName=Taskobj.getChargeUserName();
                    }else{
                        UserName=UserName+","+Taskobj.getChargeUserName();
                    }
                }
            }
            workNode.setUserName(UserName);
        }
        List<Map<String, Object>> tree0 = treeService.getTree2(workTask.getWorkId(), treeNodeId);
        List<Tree> trees = JSONObject.parseArray(JSON.toJSONString(tree0), Tree.class);
        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }

        boolean flag = true;
        for (Tree tree : trees) {
            Tree[] children = tree.getChildren();
            for (Tree child : children) {
                if(child.getNodeType().equals("DATASET")){
                    flag = false;
                }
            }
        }

        if (nodeIds.size()==0 || flag){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (SrWorkNode workNode : workNodes) {
            for (String nodeId : nodeIds) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }
        //2、查询成果数据
        Map<String,List<SrTaskTreeData>> DataMap=new HashMap<>();
        List<SrTaskTreeData> TreeDataAll = workObjectService.getObjectByUser(workTask.getWorkId(), null,null , null, null, null, null,"成果列表", null);
        for (SrTaskTreeData srTaskTreeData : TreeDataAll) {
            String nodeId=srTaskTreeData.getNodeId();
            List<SrTaskTreeData> datalist=DataMap.get(nodeId)==null?new ArrayList<>():DataMap.get(nodeId);
            datalist.add(srTaskTreeData);
            DataMap.put(nodeId,datalist);
        }
        for (SrWorkNode node : nodes) {
            String nodeId=node.getTreeNodeID();
            List<SrTaskTreeData> objectByUser=DataMap.get(nodeId)==null?new ArrayList<>():DataMap.get(nodeId);
            Map<String,Object> map=getcompute(objectByUser);
            node.setWorkProgress(map.get("format")+"");
        }

        JSONObject pageable = pageable(nodes, page.getPageNumber()+1, page.getPageSize());
        return pageable;
    }
    Tree getTreeByNodeId(Tree tree,String treeNodeId){
        Tree tree1 = new Tree();
        if (tree.getNodeId().equals(treeNodeId)){
            tree1 = tree;
            return tree1;
        }else {
            for (Tree child : tree.getChildren()) {
                Tree treeByNodeId = getTreeByNodeId(child, treeNodeId);
                if (treeByNodeId != null){
                    return treeByNodeId;
                }
            }
            return null;
        }
    }

    //获取当前节点下所有节点
    ArrayList<String> getNodeIds(Tree tree,String treeNodeId){
        ArrayList<String> nodeIDList = new ArrayList<>();
        if (tree.getNodeId().equals(treeNodeId)||(StringUtils.isEmpty(treeNodeId) && StringUtils.equals(null,tree.getPnodeId()))){
            if (tree.getChildren() != null){
                for (Tree tree1 : tree.getChildren()) {
                    nodeIDList.add(tree1.getNodeId());
                    ArrayList<String> nodes = getNodes(tree1);
                    nodeIDList.addAll(nodes);
                }
            }
        }else {
            for (Tree child : tree.getChildren()) {
                ArrayList<String> nodeIds = getNodeIds(child, treeNodeId);
                nodeIDList.addAll(nodeIds);
            }
        }
        return nodeIDList;
    }

    //递归获取子节点
    ArrayList<String> getNodes(Tree tree){
        ArrayList<String> nodeIDList = new ArrayList<>();
        if (tree.getChildren() != null){
            for (Tree tree1 : tree.getChildren()) {
                nodeIDList.add(tree1.getNodeId());
                ArrayList<String> nodes = getNodes(tree1);
                nodeIDList.addAll(nodes);
            }
        }
        return nodeIDList;
    }

    //工作查看-任务分析
    @Override
    public Map getResultList(HttpServletRequest request, JSONObject data, Pageable page) {
        HashMap<Object, Object> resultHashMap = new HashMap<>();

        String taskId = data.getString("taskId");
        String projectUser = data.getString("projectUser");
        String treeNodeId = data.getString("treeNodeId");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);
        //2、通过当前节点的treeId 查询 完整的树实例结构
        //List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());
        List<Map<String, Object>> tree0 = treeService.getTree2(workTask.getWorkId(), treeNodeId);
        List<Tree> trees = JSONObject.parseArray(JSON.toJSONString(tree0), Tree.class);

        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }
        if (nodeIds.size()==0){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            for (SrWorkNode workNode : workNodes) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }

        String nids;
        if (nodes.size()>0){
            StringBuffer treeNodeIds = new StringBuffer();
            for (SrWorkNode workNode : nodes) {
                treeNodeIds.append(workNode.getTreeNodeID()+",");
            }
            nids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }else {
            nids = treeNodeId;
        }

        //成果浏览
        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), projectUser, nids, null, null, null, null, "成果列表", null);
        //JSONObject pageable = pageable(objectByUser, page.getPageNumber()+1, page.getPageSize());
        //任务分析-饼状图
        ArrayList<Map> pieChartList = new ArrayList<>();
        float resultNum = objectByUser.size()==0?1:objectByUser.size();
        Map<String,Object> map=getcompute(objectByUser);//统一处理状态逻辑
        float archived = Float.parseFloat(map.get("archived").toString());
        float rejected = Float.parseFloat(map.get("rejected").toString());
        float undone = Float.parseFloat(map.get("undone").toString());
        float passed = Float.parseFloat(map.get("passed").toString());
        float submitted = Float.parseFloat(map.get("submitted").toString());
        float finish = Float.parseFloat(map.get("finish").toString());
        //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        HashMap<String, String> archivedMap = new HashMap<>();
        archivedMap.put("name","archived");
        archivedMap.put("value",decimalFormat.format(archived / resultNum));
        HashMap<String, String> rejectedMap = new HashMap<>();
        rejectedMap.put("name","rejected");
        rejectedMap.put("value",decimalFormat.format(rejected / resultNum));
        HashMap<String, String> undoneMap = new HashMap<>();
        undoneMap.put("name","undone");
        undoneMap.put("value",decimalFormat.format(undone / resultNum));
        HashMap<String, String> passedMap = new HashMap<>();
        passedMap.put("name","passed");
        passedMap.put("value",decimalFormat.format(passed / resultNum));
        HashMap<String, String> submittedMap = new HashMap<>();
        submittedMap.put("name","submitted");
        submittedMap.put("value",decimalFormat.format(submitted / resultNum));
        HashMap<String, String> finishMap = new HashMap<>();
        finishMap.put("name","finish");
        finishMap.put("value",decimalFormat.format(finish / resultNum));
        pieChartList.add(undoneMap);//未完成
        pieChartList.add(finishMap);//已完成
        pieChartList.add(submittedMap);//已提交
        pieChartList.add(rejectedMap);//已驳回
        pieChartList.add(passedMap);//审核通过
        pieChartList.add(archivedMap);//已归档

        //任务分析-条形图
        HashMap<Object, ArrayList> BarGraphMap = new HashMap<>();
        //去重getCreateUser
        ArrayList<SrTaskTreeData> collect = objectByUser.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrTaskTreeData::getCreateUser))), ArrayList::new));
        //任务分配人员：
        ArrayList<String> users = new ArrayList<>();
        if(collect.size()>0){
            for (SrTaskTreeData srTaskTreeData : collect) {
                users.add(authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
            }
        }else{
            users.add(workTask.getChargeUserName());
        }
        BarGraphMap.put("users",users);

        //反向对应
        Map<String,Object> reverse=JSONObject.parseObject(JSON.toJSONString(map.get("reverse")));//统一处理状态逻辑
        for (SrTaskTreeData srTaskTreeDa : collect) {
            int archived1 = 0;
            int rejected1 = 0;
            int undone1 = 0;
            int passed1 = 0;
            int submitted1 = 0;
            int finish1 = 0;
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                ArrayList<Integer> integers = new ArrayList<>();
                if (srTaskTreeDa.getCreateUser().equals(srTaskTreeData.getCreateUser())){
                    String dataStatus = srTaskTreeData.getDataStatus();
                    String status=reverse.get(dataStatus)==null?"":reverse.get(dataStatus).toString();
                    switch (status){
                        case "已归档" : archived1++;break;
                        case "已驳回" : rejected1++;break;
                        case "未完成" : undone1++;break;
                        case "审核通过" : passed1++;break;
                        case "已提交" : submitted1++;break;
                        case "已完成" : finish1++;break;
                    }
                }
                integers.add(0,archived1);
                integers.add(1,rejected1);
                integers.add(2,undone1);
                integers.add(3,passed1);
                integers.add(4,submitted1);
                integers.add(5,finish1);
                BarGraphMap.put(authorityRepository.getUserDisplayName(srTaskTreeDa.getCreateUser()),integers);
            }
        }
        //条形图数据转换
        HashMap<Object, Object> BarGraphReturnMap = new HashMap<>();
        BarGraphReturnMap.put("users",users);
        HashMap<Object, Object> archived1Map = new HashMap<>();
        HashMap<Object, Object> rejected1Map = new HashMap<>();
        HashMap<Object, Object> undone1Map = new HashMap<>();
        HashMap<Object, Object> passed1Map = new HashMap<>();
        HashMap<Object, Object> submitted1Map = new HashMap<>();
        HashMap<Object, Object> finish1Map = new HashMap<>();
        ArrayList<Object> stutaList = new ArrayList<>();

        ArrayList<Integer> rejected1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(2);
                rejected1.add(integer1);
            }
        }else{
            rejected1.add(1);
        }
        undone1Map.put("name","未完成");
        undone1Map.put("data",rejected1);

        ArrayList<Integer> finish1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(5);
                finish1.add(integer1);
            }
        }else{
            finish1.add(0);
        }
        finish1Map.put("name","已完成");
        finish1Map.put("data",finish1);

        ArrayList<Integer> submitted1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(4);
                submitted1.add(integer1);
            }
        }else{
            submitted1.add(0);
        }
        submitted1Map.put("name","已提交");
        submitted1Map.put("data",submitted1);

        ArrayList<Integer> undone1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(1);
                undone1.add(integer1);
            }
        }else{
            undone1.add(0);
        }
        rejected1Map.put("name","已驳回");
        rejected1Map.put("data",undone1);

        ArrayList<Integer> passed1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(3);
                passed1.add(integer1);
            }
        }else{
            passed1.add(0);
        }
        passed1Map.put("name","审核通过");
        passed1Map.put("data",passed1);

        ArrayList<Integer> archived1 = new ArrayList<>();
        if(collect.size()>0){
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(0);
                archived1.add(integer1);
            }
        }else{
            archived1.add(0);
        }
        archived1Map.put("name","已归档");
        archived1Map.put("data",archived1);

        stutaList.add(archived1Map);
        stutaList.add(rejected1Map);
        stutaList.add(undone1Map);
        stutaList.add(passed1Map);
        stutaList.add(submitted1Map);
        stutaList.add(finish1Map);
        BarGraphReturnMap.put("series",stutaList);

        resultHashMap.put("pieChart",pieChartList);
        resultHashMap.put("BarGraphMap",BarGraphReturnMap);

        return resultHashMap;
    }

    public Map<String, Object> getcompute(List<SrTaskTreeData> objectByUser) {
        Map<String,Object> map=new HashMap<>();
        //正向对应
        Map<String,Object> positive=new HashMap<>();
        positive.put("未完成","未完成");
        positive.put("已完成","待提交,已完成");
        positive.put("已提交","待审核,审核中,已提交");
        positive.put("审核通过","审核通过");
        positive.put("已驳回","审核退回,已驳回");
        positive.put("已归档","已归档");
        map.put("positive",positive);
        //反向对应
        Map<String,Object> reverse=new HashMap<>();
        reverse.put("未完成","未完成");
        reverse.put("已完成","已完成");
        reverse.put("待提交","已完成");
        reverse.put("已提交","已提交");
        reverse.put("待审核","已提交");
        reverse.put("审核中","已提交");
        reverse.put("审核通过","审核通过");
        reverse.put("审核退回","已驳回");
        reverse.put("已驳回","已驳回");
        reverse.put("已归档","已归档");
        map.put("reverse",reverse);
        float resultNum = objectByUser==null?1:objectByUser.size()>0?objectByUser.size():1;
        float undone = objectByUser==null?1:objectByUser.size()==0?1:0;//未完成
        float archived = 0;//已归档
        float rejected = 0;//已驳回
        float passed = 0;//审核通过
        float submitted = 0;//已提交
        float finish = 0;//已完成
        if(objectByUser!=null){
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                String dataStatus = srTaskTreeData.getDataStatus();
                if(StringUtils.equals(dataStatus,"待提交")||StringUtils.equals(dataStatus,"已完成")){
                    finish++;//已完成
                }else if(StringUtils.equals(dataStatus,"待审核")||StringUtils.equals(dataStatus,"已提交")){
                    submitted++;//已提交
                }else if(StringUtils.equals(dataStatus,"审核中")||StringUtils.equals(dataStatus,"审核通过")){
                    passed++;//审核通过
                }else if(StringUtils.equals(dataStatus,"已驳回")||StringUtils.equals(dataStatus,"审核退回")){
                    rejected++;//已驳回
                }else if(StringUtils.equals(dataStatus,"已归档")){
                    archived++;//已归档
                }
            }
        }
        map.put("undone",undone);
        map.put("archived",archived);
        map.put("rejected",rejected);
        map.put("passed",passed);
        map.put("submitted",submitted);
        map.put("finish",finish);
        double a = finish*0.4+submitted*0.6+rejected*0.6+passed*0.8+archived*1;
//            未完成：最小业务单元研究成果都未做，工作进度打分0%；
//            已完成：最小业务单元中所有研究成果都已完成，工作进度打分40%；
//            已提交：最小业务单元中所有研究成果全部已提交质控申请，工作进度打分60%；
//            已驳回：最小业务单元提交的成果申请全部或部分被驳回，工作进度打分60%；
//            审核通过：最小业务单元中提交的成果全部审核通过，工作进度打分80%；
//            已归档：最小业务单元中提交的所有成果都已归档，工作进度打分100%；
        //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String format = decimalFormat.format(a / resultNum);
        map.put("format",Float.parseFloat(format));
        return map;
    }
    List<Tree> getTree(HttpServletRequest request, String treeId){
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/fulltree");
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization",request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, String.class);
        Map map = JSONObject.parseObject(ex.getBody(), Map.class);
        JSONArray result = JSONObject.parseArray(map.get("result").toString());
        return JSONObject.parseArray(result.toJSONString(), Tree.class);
    }

    //工作查看-成果浏览条件筛选/分页/跳转成果界面
    @Override
    public Map getResultListBy(HttpServletRequest request, JSONObject data, Pageable page) {
        HashMap<Object, Object> resultHashMap = new HashMap<>();

        String taskId = data.getString("taskId");
        String projectUser = data.getString("projectUser");
        String status = data.getString("status");
        Date startTime = data.getDate("startTime");
        Date endTime = data.getDate("endTime");
        String treeNodeId = data.getString("treeNodeId");
        String dataSetId = data.getString("dataSetId");
        String dataType = data.getString("dataType");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);
        //2、通过当前节点的treeId 查询 完整的树实例结构
        List<Map<String, Object>> tree0 = treeService.getTree2(workTask.getWorkId(), treeNodeId);
        List<Tree> trees = JSONObject.parseArray(JSON.toJSONString(tree0), Tree.class);
        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }
        if (nodeIds.size()==0){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            for (SrWorkNode workNode : workNodes) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }

        String nids;
        if (nodes.size()>0){
            StringBuffer treeNodeIds = new StringBuffer();
            for (SrWorkNode workNode : nodes) {
                treeNodeIds.append(workNode.getTreeNodeID()+",");
            }
            nids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }else {
            nids = treeNodeId;
        }
        Map<String,Object> map=getcompute(null);

        //正向对应
        Map<String,Object> positive=JSONObject.parseObject(JSON.toJSONString(map.get("positive")));
        status=positive.get(status)==null?"":positive.get(status).toString();
        //成果浏览
        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), projectUser, nids, status, startTime, endTime, dataSetId, "成果列表",dataType);
        //反向对应
        Map<String,Object> reverse=JSONObject.parseObject(JSON.toJSONString(map.get("reverse")));//统一处理状态逻辑
        for (SrTaskTreeData srTaskTreeData : objectByUser) {
            srTaskTreeData.setCreateUserName(authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
            srTaskTreeData.setRemarks(reverse.get(srTaskTreeData.getDataStatus()).toString());
        }
        JSONObject pageable = pageable(objectByUser, page.getPageNumber()+1, page.getPageSize());
        resultHashMap.put("resultList",pageable);
        return resultHashMap;
    }

    //工作查看-项目成员
    @Override
    public Map getResultListUser(HttpServletRequest request, JSONObject data, Pageable page) {
        HashMap<Object, Object> resultHashMap = new HashMap<>();
        String taskId = data.getString("taskId");
        String treeNodeId = data.getString("treeNodeId");
//任务分配人员：
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);

        //首页点击任务进入工作查看界面，未选中任务节点，返回当前任务所有节点下成果
        if (treeNodeId == null || treeNodeId.length()==0){

            StringBuffer nodeIds = new StringBuffer();
            for (String workNode : srWorkNodes) {
                nodeIds.append(workNode+",");
            }

            //成果浏览
            List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), null, nodeIds.toString().substring(0,nodeIds.length()-1), null, null, null, null, "成果列表", null);

            //成果浏览-项目成员
            ArrayList<Map> projectUserList = new ArrayList<>();
            ArrayList<SrTaskTreeData> collect = objectByUser.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrTaskTreeData::getCreateUser))), ArrayList::new));
            for (SrTaskTreeData srTaskTreeData : collect) {
                HashMap<String, String> user = new HashMap<>();
                user.put("userName",authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
                user.put("userId",srTaskTreeData.getCreateUser());
                projectUserList.add(user);
            }
            resultHashMap.put("projectUserList",projectUserList);

            return resultHashMap;
        }

        //2、通过当前节点的treeId 查询 完整的树实例结构
//        List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());
        List<Map<String, Object>> tree0 = treeService.getTree2(workTask.getWorkId(), treeNodeId);
        List<Tree> trees = JSONObject.parseArray(JSON.toJSONString(tree0), Tree.class);

        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }
        if (nodeIds.size()==0){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            for (SrWorkNode workNode : workNodes) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }

        String nids;
        if (nodes.size()>0){
            StringBuffer treeNodeIds = new StringBuffer();
            for (SrWorkNode workNode : nodes) {
                treeNodeIds.append(workNode.getTreeNodeID()+",");
            }
            nids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }else {
            nids = treeNodeId;
        }

        //成果浏览
        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), null, nids, null, null, null, null, null, null);

        //成果浏览-项目成员
        JSONArray projectUserList = new JSONArray();
        ArrayList<SrTaskTreeData> collect = objectByUser.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrTaskTreeData::getCreateUser))), ArrayList::new));
        for (SrTaskTreeData srTaskTreeData : collect) {
            HashMap<Object, Object> user = new HashMap<>();
            user.put("userName",authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
            user.put("userId",srTaskTreeData.getCreateUser());
            projectUserList.add(user);
        }
        resultHashMap.put("projectUserList",projectUserList);

        return resultHashMap;
    }

    @Override
    public JSONObject getWorkInfoList1(HttpServletRequest request, JSONObject data, Pageable page) {
        String taskId = data.getString("taskId");
        String treeNodeId = data.getString("treeNodeId");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);

        if (treeNodeId == null || treeNodeId.length()==0){
            //2、查询当前任务实例树
            SrWorkMsg workMsg = srWorkMsgRepository.findOne(workTask.getWorkId());
            RestTemplate restTemplate = new RestTemplate();
            StringBuffer serviceName = new StringBuffer();
            serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+workMsg.getInstanceId()+"/fulltree");
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization",request.getHeader("Authorization"));
            HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
            ResponseEntity<String> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, String.class);
            Map map = JSONObject.parseObject(ex.getBody(), Map.class);
            JSONArray result = JSONObject.parseArray(map.get("result").toString());
            List<Tree> trees = JSONObject.parseArray(result.toJSONString(), Tree.class);

            for (SrWorkNode node : workNodes) {
                //3、获取当前节所选节点的所有数据集
                StringBuffer datasetIds = new StringBuffer();
                for (Tree tree : trees) {
                    ArrayList<Tree> dataSetIds = getDataSetIds(tree, node.getTreeNodeID());
                    for (Tree dataSetId : dataSetIds) {
                        datasetIds.append(dataSetId.getNodeId()+",");
                    }
                }
                String dids = datasetIds.toString().substring(0, datasetIds.length() - 1);
                List<JSONObject> jsonObjects = byConditioin(request, workTask.getWorkId(), null, null, null, null, dids, null);
                float resultNum = jsonObjects.size();
                float archived = 0;//已归档
                float rejected = 0;//已驳回
                float undone = 0;//未完成
                float passed = 0;//审核通过
                float submitted = 0;//已提交
                float finish = 0;//已完成
                for (JSONObject srTaskTreeData : jsonObjects) {
                    String dataStatus = srTaskTreeData.getString("audit_state");
                    switch (dataStatus){
                        case "待审核" : archived++;break;
                        case "未通过" : rejected++;break;
                        case "未完成" : undone++;break;
                        case "审核通过" : passed++;break;
                        case "已提交" : submitted++;break;
                        case "审核中" : finish++;break;
                    }
                }
                double a = archived*1+rejected*0.5+undone*0+passed*1+submitted*0.5+finish*0.5;
                //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String format = decimalFormat.format(a / resultNum);
                node.setWorkProgress(format);
            }
            JSONObject pageable = pageable(workNodes, page.getPageNumber()+1, page.getPageSize());
            return pageable;
        }

        //2、查询当前任务实例树
        SrWorkMsg workMsg = srWorkMsgRepository.findOne(workTask.getWorkId());

        //2、通过当前节点的treeId 查询 完整的树实例结构
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+workMsg.getInstanceId()+"/fulltree");
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization",request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, String.class);
        Map map = JSONObject.parseObject(ex.getBody(), Map.class);
        JSONArray result = JSONObject.parseArray(map.get("result").toString());
        List<Tree> trees = JSONObject.parseArray(result.toJSONString(), Tree.class);

        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }

        boolean flag = true;
        for (Tree tree : trees) {
            Tree[] children = tree.getChildren();
            for (Tree child : children) {
                if(child.getNodeType().equals("DATASET")){
                    flag = false;
                }
            }
        }

        if (nodeIds.size()==0 || flag){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (SrWorkNode workNode : workNodes) {
            for (String nodeId : nodeIds) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }

        for (SrWorkNode node : nodes) {
            List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), null, node.getTreeNodeID(), null, null, null, null, null, null);
            float resultNum = objectByUser.size();
            float archived = 0;
            float rejected = 0;
            float undone = 0;
            float passed = 0;
            float submitted = 0;
            float finish = 0;
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                String dataStatus = srTaskTreeData.getDataStatus();
                switch (dataStatus){
                    case "已归档" : archived++;break;
                    case "已驳回" : rejected++;break;
                    case "未完成" : undone++;break;
                    case "审核通过" : passed++;break;
                    case "已提交" : submitted++;break;
                    case "已完成" : finish++;break;
                }
            }
            double a = archived*1+rejected*0.5+undone*0+passed*1+submitted*0.5+finish*0.5;
            //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String format = decimalFormat.format(a / resultNum);
            node.setWorkProgress(format);
        }

        JSONObject pageable = pageable(nodes, page.getPageNumber()+1, page.getPageSize());
        return pageable;
    }

    @Override
    public Map getResultList1(HttpServletRequest request, JSONObject data, Pageable page) {
        HashMap<Object, Object> resultHashMap = new HashMap<>();

        String taskId = data.getString("taskId");
        String projectUser = data.getString("projectUser");
        String treeNodeId = data.getString("treeNodeId");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);

        //首页点击任务进入工作查看界面，未选中任务节点，返回当前任务所有节点下成果
        if (treeNodeId == null || treeNodeId.length()==0){

            StringBuffer nodeIds = new StringBuffer();
            for (String workNode : srWorkNodes) {
                nodeIds.append(workNode+",");
            }

            //成果浏览
            List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), projectUser, nodeIds.toString().substring(0,nodeIds.length()-1), null, null, null, null, null, null);
            JSONObject pageable = pageable(objectByUser, page.getPageNumber()+1, page.getPageSize());

            //成果浏览-项目成员
            ArrayList<Map> projectUserList = new ArrayList<>();
            ArrayList<SrTaskTreeData> collect = objectByUser.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrTaskTreeData::getCreateUser))), ArrayList::new));
            for (SrTaskTreeData srTaskTreeData : collect) {
                HashMap<String, String> user = new HashMap<>();
                user.put("userName",authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
                user.put("userId",srTaskTreeData.getCreateUser());
                projectUserList.add(user);
            }

            //任务分析-饼状图
            ArrayList<Map> pieChartList = new ArrayList<>();
            float resultNum = objectByUser.size();
            float archived = 0;
            float rejected = 0;
            float undone = 0;
            float passed = 0;
            float submitted = 0;
            float finish = 0;
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                String dataStatus = srTaskTreeData.getDataStatus();
                switch (dataStatus){
                    case "已归档" : archived++;break;
                    case "已驳回" : rejected++;break;
                    case "未完成" : undone++;break;
                    case "审核通过" : passed++;break;
                    case "已提交" : submitted++;break;
                    case "已完成" : finish++;break;
                }
            }
            //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            HashMap<String, String> archivedMap = new HashMap<>();
            archivedMap.put("name","archived");
            archivedMap.put("value",decimalFormat.format(archived / resultNum));
            HashMap<String, String> rejectedMap = new HashMap<>();
            rejectedMap.put("name","rejected");
            rejectedMap.put("value",decimalFormat.format(rejected / resultNum));
            HashMap<String, String> undoneMap = new HashMap<>();
            undoneMap.put("name","undone");
            undoneMap.put("value",decimalFormat.format(undone / resultNum));
            HashMap<String, String> passedMap = new HashMap<>();
            passedMap.put("name","passed");
            passedMap.put("value",decimalFormat.format(passed / resultNum));
            HashMap<String, String> submittedMap = new HashMap<>();
            submittedMap.put("name","submitted");
            submittedMap.put("value",decimalFormat.format(submitted / resultNum));
            HashMap<String, String> finishMap = new HashMap<>();
            finishMap.put("name","finish");
            finishMap.put("value",decimalFormat.format(finish / resultNum));
            pieChartList.add(archivedMap);
            pieChartList.add(rejectedMap);
            pieChartList.add(undoneMap);
            pieChartList.add(passedMap);
            pieChartList.add(submittedMap);
            pieChartList.add(finishMap);

            //任务分析-条形图
            HashMap<Object, ArrayList> BarGraphMap = new HashMap<>();

            ArrayList<String> users = new ArrayList<>();
            for (SrTaskTreeData srTaskTreeData : collect) {
                users.add(authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
            }
            BarGraphMap.put("users",users);

            for (SrTaskTreeData srTaskTreeDa : collect) {
                int archived1 = 0;
                int rejected1 = 0;
                int undone1 = 0;
                int passed1 = 0;
                int submitted1 = 0;
                int finish1 = 0;
                for (SrTaskTreeData srTaskTreeData : objectByUser) {
                    ArrayList<Integer> integers = new ArrayList<>();
                    if (srTaskTreeDa.getCreateUser().equals(srTaskTreeData.getCreateUser())){
                        String dataStatus = srTaskTreeData.getDataStatus();
                        switch (dataStatus){
                            case "已归档" : archived1++;break;
                            case "已驳回" : rejected1++;break;
                            case "未完成" : undone1++;break;
                            case "审核通过" : passed1++;break;
                            case "已提交" : submitted1++;break;
                            case "已完成" : finish1++;break;
                        }
                    }
                    integers.add(0,archived1);
                    integers.add(1,rejected1);
                    integers.add(2,undone1);
                    integers.add(3,passed1);
                    integers.add(4,submitted1);
                    integers.add(5,finish1);
                    BarGraphMap.put(authorityRepository.getUserDisplayName(srTaskTreeDa.getCreateUser()),integers);
                }
            }
            //条形图数据转换
            HashMap<Object, Object> BarGraphReturnMap = new HashMap<>();
            BarGraphReturnMap.put("users",users);
            HashMap<Object, Object> archived1Map = new HashMap<>();
            HashMap<Object, Object> rejected1Map = new HashMap<>();
            HashMap<Object, Object> undone1Map = new HashMap<>();
            HashMap<Object, Object> passed1Map = new HashMap<>();
            HashMap<Object, Object> submitted1Map = new HashMap<>();
            HashMap<Object, Object> finish1Map = new HashMap<>();
            ArrayList<Object> stutaList = new ArrayList<>();

            ArrayList<Integer> archived1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(0);
                archived1.add(integer1);
            }
            archived1Map.put("name","已归档");
            archived1Map.put("data",archived1);

            ArrayList<Integer> undone1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(1);
                undone1.add(integer1);
            }
            rejected1Map.put("name","已驳回");
            rejected1Map.put("data",undone1);

            ArrayList<Integer> rejected1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(2);
                rejected1.add(integer1);
            }
            undone1Map.put("name","未完成");
            undone1Map.put("data",rejected1);

            ArrayList<Integer> passed1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(3);
                passed1.add(integer1);
            }
            passed1Map.put("name","审核通过");
            passed1Map.put("data",passed1);

            ArrayList<Integer> submitted1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(4);
                submitted1.add(integer1);
            }
            submitted1Map.put("name","已提交");
            submitted1Map.put("data",submitted1);

            ArrayList<Integer> finish1 = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                ArrayList arrayList = BarGraphMap.get(users.get(i));
                Integer integer1 = (Integer) arrayList.get(5);
                finish1.add(integer1);
            }

            finish1Map.put("name","已完成");
            finish1Map.put("data",finish1);


            stutaList.add(archived1Map);
            stutaList.add(rejected1Map);
            stutaList.add(undone1Map);
            stutaList.add(passed1Map);
            stutaList.add(submitted1Map);
            stutaList.add(finish1Map);
            BarGraphReturnMap.put("series",stutaList);

            resultHashMap.put("pieChart",pieChartList);
            resultHashMap.put("BarGraphMap",BarGraphReturnMap);

            return resultHashMap;
        }

        //2、通过当前节点的treeId 查询 完整的树实例结构
        List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());

        //3、获取当前节所选节点的所有子节点
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<String> list = getNodeIds(tree, treeNodeId);
            nodeIds.addAll(list);
        }
        if (nodeIds.size()==0){
            nodeIds.add(treeNodeId);
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<SrWorkNode> nodes = new ArrayList<>();
        for (String nodeId : nodeIds) {
            for (SrWorkNode workNode : workNodes) {
                if (workNode.getTreeNodeID().equals(nodeId)){
                    nodes.add(workNode);
                }
            }
        }

        String nids;
        if (nodes.size()>0){
            StringBuffer treeNodeIds = new StringBuffer();
            for (SrWorkNode workNode : nodes) {
                treeNodeIds.append(workNode.getTreeNodeID()+",");
            }
            nids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }else {
            nids = treeNodeId;
        }

        //成果浏览
        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(workTask.getWorkId(), projectUser, nids, null, null, null, null, null, null);
        JSONObject pageable = pageable(objectByUser, page.getPageNumber()+1, page.getPageSize());

        //成果浏览-项目成员
        JSONArray projectUserList = new JSONArray();
        ArrayList<SrTaskTreeData> collect = objectByUser.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SrTaskTreeData::getCreateUser))), ArrayList::new));
        for (SrTaskTreeData srTaskTreeData : collect) {
            HashMap<Object, Object> user = new HashMap<>();
            user.put("userName",authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
            user.put("userId",srTaskTreeData.getCreateUser());
            projectUserList.add(user);
        }

        //任务分析-饼状图
        ArrayList<Map> pieChartList = new ArrayList<>();
        float resultNum = objectByUser.size();
        float archived = 0;
        float rejected = 0;
        float undone = 0;
        float passed = 0;
        float submitted = 0;
        float finish = 0;
        for (SrTaskTreeData srTaskTreeData : objectByUser) {
            String dataStatus = srTaskTreeData.getDataStatus();
            switch (dataStatus){
                case "已归档" : archived++;break;
                case "已驳回" : rejected++;break;
                case "未完成" : undone++;break;
                case "审核通过" : passed++;break;
                case "已提交" : submitted++;break;
                case "已完成" : finish++;break;
            }
        }
        //DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        HashMap<String, String> archivedMap = new HashMap<>();
        archivedMap.put("name","archived");
        archivedMap.put("value",decimalFormat.format(archived / resultNum));
        HashMap<String, String> rejectedMap = new HashMap<>();
        rejectedMap.put("name","rejected");
        rejectedMap.put("value",decimalFormat.format(rejected / resultNum));
        HashMap<String, String> undoneMap = new HashMap<>();
        undoneMap.put("name","undone");
        undoneMap.put("value",decimalFormat.format(undone / resultNum));
        HashMap<String, String> passedMap = new HashMap<>();
        passedMap.put("name","passed");
        passedMap.put("value",decimalFormat.format(passed / resultNum));
        HashMap<String, String> submittedMap = new HashMap<>();
        submittedMap.put("name","submitted");
        submittedMap.put("value",decimalFormat.format(submitted / resultNum));
        HashMap<String, String> finishMap = new HashMap<>();
        finishMap.put("name","finish");
        finishMap.put("value",decimalFormat.format(finish / resultNum));
        pieChartList.add(archivedMap);
        pieChartList.add(rejectedMap);
        pieChartList.add(undoneMap);
        pieChartList.add(passedMap);
        pieChartList.add(submittedMap);
        pieChartList.add(finishMap);

        //任务分析-条形图
        HashMap<Object, ArrayList> BarGraphMap = new HashMap<>();

        ArrayList<String> users = new ArrayList<>();
        for (SrTaskTreeData srTaskTreeData : collect) {
            users.add(authorityRepository.getUserDisplayName(srTaskTreeData.getCreateUser()));
        }
        BarGraphMap.put("users",users);

        for (SrTaskTreeData srTaskTreeDa : collect) {
            int archived1 = 0;
            int rejected1 = 0;
            int undone1 = 0;
            int passed1 = 0;
            int submitted1 = 0;
            int finish1 = 0;
            for (SrTaskTreeData srTaskTreeData : objectByUser) {
                ArrayList<Integer> integers = new ArrayList<>();
                if (srTaskTreeDa.getCreateUser().equals(srTaskTreeData.getCreateUser())){
                    String dataStatus = srTaskTreeData.getDataStatus();
                    switch (dataStatus){
                        case "已归档" : archived1++;break;
                        case "已驳回" : rejected1++;break;
                        case "未完成" : undone1++;break;
                        case "审核通过" : passed1++;break;
                        case "已提交" : submitted1++;break;
                        case "已完成" : finish1++;break;
                    }
                }
                integers.add(0,archived1);
                integers.add(1,rejected1);
                integers.add(2,undone1);
                integers.add(3,passed1);
                integers.add(4,submitted1);
                integers.add(5,finish1);
                BarGraphMap.put(authorityRepository.getUserDisplayName(srTaskTreeDa.getCreateUser()),integers);
            }
        }
        //条形图数据转换
        HashMap<Object, Object> BarGraphReturnMap = new HashMap<>();
        BarGraphReturnMap.put("users",users);
        HashMap<Object, Object> archived1Map = new HashMap<>();
        HashMap<Object, Object> rejected1Map = new HashMap<>();
        HashMap<Object, Object> undone1Map = new HashMap<>();
        HashMap<Object, Object> passed1Map = new HashMap<>();
        HashMap<Object, Object> submitted1Map = new HashMap<>();
        HashMap<Object, Object> finish1Map = new HashMap<>();
        ArrayList<Object> stutaList = new ArrayList<>();

        ArrayList<Integer> archived1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(0);
            archived1.add(integer1);
        }
        archived1Map.put("name","已归档");
        archived1Map.put("data",archived1);

        ArrayList<Integer> undone1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(1);
            undone1.add(integer1);
        }
        rejected1Map.put("name","已驳回");
        rejected1Map.put("data",undone1);

        ArrayList<Integer> rejected1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(2);
            rejected1.add(integer1);
        }
        undone1Map.put("name","未完成");
        undone1Map.put("data",rejected1);

        ArrayList<Integer> passed1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(3);
            passed1.add(integer1);
        }
        passed1Map.put("name","审核通过");
        passed1Map.put("data",passed1);

        ArrayList<Integer> submitted1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(4);
            submitted1.add(integer1);
        }
        submitted1Map.put("name","已提交");
        submitted1Map.put("data",submitted1);

        ArrayList<Integer> finish1 = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            ArrayList arrayList = BarGraphMap.get(users.get(i));
            Integer integer1 = (Integer) arrayList.get(5);
            finish1.add(integer1);
        }

        finish1Map.put("name","已完成");
        finish1Map.put("data",finish1);


        stutaList.add(archived1Map);
        stutaList.add(rejected1Map);
        stutaList.add(undone1Map);
        stutaList.add(passed1Map);
        stutaList.add(submitted1Map);
        stutaList.add(finish1Map);
        BarGraphReturnMap.put("series",stutaList);

        resultHashMap.put("pieChart",pieChartList);
        resultHashMap.put("BarGraphMap",BarGraphReturnMap);

        return resultHashMap;
    }

    @Override
    public Map getResultListBy1(HttpServletRequest request, JSONObject data, Pageable page) {
        HashMap<Object, Object> resultHashMap = new HashMap<>();

        String taskId = data.getString("taskId");
        String projectUser = data.getString("projectUser");
        String status = data.getString("status");
        Date startTime = data.getDate("startTime");
        Date endTime = data.getDate("endTime");
        String treeNodeId = data.getString("treeNodeId");
        String dataSetId = data.getString("dataSetId");
        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);

        //首页点击任务进入工作查看界面，未选中任务节点，返回当前任务所有节点下成果
        //todo 将taskid带上
        if (treeNodeId == null || treeNodeId.length()==0){
            List<JSONObject> jsonObjects;
            if (projectUser == null || projectUser.length()==0){
                projectUser = SpringManager.getCurrentUser().getUserId();
                jsonObjects = byConditioin(request, workTask.getWorkId(), projectUser, startTime, endTime, status, dataSetId, null);
            }else {
                jsonObjects = byConditioin(request, workTask.getWorkId(), projectUser, startTime, endTime, status, dataSetId, null);
            }
            JSONObject pageable = pageable(jsonObjects, page.getPageNumber()+1, page.getPageSize());
            resultHashMap.put("resultList",pageable);
            return resultHashMap;
        }

        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);

        //3、通过当前节点的treeId 查询 完整的树实例结构
        List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());

        ArrayList<String> dataSets = new ArrayList<>();
        for (Tree tree : trees) {
            for (SrWorkNode workNode : workNodes) {
                System.out.println(workNode.getTreeNodeID()+"/////////////");
                ArrayList<Tree> dataSetIds = getDataSetIds(tree, workNode.getTreeNodeID());
                for (Tree setId : dataSetIds) {
                    System.out.println(setId);
                    dataSets.add(setId.getNodeId());
                }
            }
        }

        //4、获取当前节所选节点的所有数据集
        ArrayList<String> ids = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<Tree> list = getDataSetIds(tree, treeNodeId);
            for (Tree tree1 : list) {
                ids.add(tree1.getNodeId());
            }
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<String> dataids = new ArrayList<>();
        for (String ids1 : ids) {
            for (String ids2 : dataSets) {
                if (ids1.equals(ids2)){
                    dataids.add(ids1);
                }
            }
        }

        String dids;
        StringBuffer treeNodeIds = new StringBuffer();
        for (String s : dataids) {
            treeNodeIds.append(s+",");
        }
        if (dataids==null || dataids.size()==0){
            dids = null;
        }else {
            dids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }
        //6成果浏览
        List<JSONObject> jsonObjects;
        if (projectUser == null || projectUser.length()==0){
            projectUser = SpringManager.getCurrentUser().getUserId();
            //todo 将taskid带上
            jsonObjects = byConditioin(request, workTask.getWorkId(), projectUser, startTime, endTime, status, dids, null);
        }else {
            jsonObjects = byConditioin(request, workTask.getWorkId(), projectUser, startTime, endTime, status, dids, null);
        }

        JSONObject pageable = pageable(jsonObjects, page.getPageNumber()+1, page.getPageSize());
        resultHashMap.put("resultList",pageable);
        return resultHashMap;
    }

    //获取当前节点下所有数据集
    ArrayList<Tree> getDataSetIds(Tree tree,String treeNodeId){
        ArrayList<Tree> trees = new ArrayList<>();
        if (tree.getNodeId().equals(treeNodeId)){
            if (tree.getChildren() != null){
                for (Tree tree1 : tree.getChildren()) {
                    if (tree1.getNodeType().equals("DATASET")){
                        trees.add(tree1);
                    }else {
                        ArrayList<Tree> trees1 = getNodess(tree1);
                        trees.addAll(trees1);
                    }
                }
            }
        }else {
            for (Tree child : tree.getChildren()) {
                ArrayList<Tree> nodeIds = getDataSetIds(child, treeNodeId);
                trees.addAll(nodeIds);
            }
        }
        return trees;
    }

    //递归获取
    ArrayList<Tree> getNodess(Tree tree){
        ArrayList<Tree> trees = new ArrayList<>();
        if (tree.getChildren() != null){
            for (Tree tree1 : tree.getChildren()) {
                if (tree1.getNodeType().equals("DATASET")){
                    trees.add(tree1);
                }else {
                    ArrayList<Tree> trees1 = getNodess(tree1);
                    trees.addAll(trees1);
                }
            }
        }
        return trees;
    }

    @Override
    public Map getResultListUser1(HttpServletRequest request, JSONObject data, Pageable page) {
        @Setter
        @Getter
        class User{
            String userId;
            String userName;
        }
        HashMap<Object, Object> resultHashMap = new HashMap<>();
        String taskId = data.getString("taskId");
        String treeNodeId = data.getString("treeNodeId");

        //1、查询当前任务
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);

        //首页点击任务进入工作查看界面，未选中任务节点，返回当前任务所有节点下成果
        //todo 将taskid带上
        if (treeNodeId == null || treeNodeId.length()==0){
            ArrayList<User> projectUserList = new ArrayList<>();
            List<JSONObject> jsonObjects = byConditioin(request, workTask.getWorkId(), null, null, null, null, null, null);
            for (JSONObject document : jsonObjects) {
                User user1 = new User();
                user1.setUserId(document.getString("create_user_id"));
                user1.setUserName(document.getString("create_user"));
                projectUserList.add(user1);
            }
            ArrayList<User> collect = projectUserList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(User::getUserId))), ArrayList::new));
            resultHashMap.put("projectUserList",collect);

            return resultHashMap;
        }

        //2、获取当前任务下所有节点
        String[] srWorkNodes = workTask.getTreeNodeIds().split(",");
        Specification<SrWorkNode> spec = new Specification<SrWorkNode>() {
            @Override
            public Predicate toPredicate(Root<SrWorkNode> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (srWorkNodes.length > 0) {
                    Predicate templateIds = root.get("treeNodeID").in(srWorkNodes);
                    predicates.add(templateIds);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrWorkNode> workNodes = srWorkNodeRepository.findAll(spec);

        //3、通过当前节点的treeId 查询 完整的树实例结构
        List<Tree> trees = getTree(request, srWorkMsgRepository.findOne(workTask.getWorkId()).getInstanceId());

        ArrayList<String> dataSets = new ArrayList<>();
        for (Tree tree : trees) {
            for (SrWorkNode workNode : workNodes) {
                ArrayList<Tree> dataSetIds = getDataSetIds(tree, workNode.getTreeNodeID());
                for (Tree setId : dataSetIds) {
                    dataSets.add(setId.getNodeId());
                }
            }
        }

        //4、获取当前节所选节点的所有数据集
        ArrayList<String> ids = new ArrayList<>();
        for (Tree tree : trees) {
            ArrayList<Tree> list = getDataSetIds(tree, treeNodeId);
            for (Tree tree1 : list) {
                ids.add(tree1.getNodeId());
            }
        }

        //将用户的所有任务，和当前节点下的所有节点匹配，获取当前节点下属于该用户的任务列表
        ArrayList<String> dataids = new ArrayList<>();
        for (String ids1 : ids) {
            for (String ids2 : dataSets) {
                if (ids1.equals(ids2)){
                    dataids.add(ids1);
                }
            }
        }

        String dids;
        StringBuffer treeNodeIds = new StringBuffer();
        for (String s : dataids) {
            treeNodeIds.append(s+",");
        }
        if (dataids==null || dataids.size()==0){
            dids = null;
        }else {
            dids = treeNodeIds.toString().substring(0, treeNodeIds.length() - 1);
        }

        //todo 将taskid带上
        List<JSONObject> jsonObjects= byConditioin(request, workTask.getWorkId(), null, null, null, null, dids, null);
        //成果浏览-项目成员
        ArrayList<User> projectUserList = new ArrayList<>();
        for (JSONObject document : jsonObjects) {
            User user1 = new User();
            user1.setUserId(document.getString("create_user_id"));
            user1.setUserName(document.getString("create_user"));
            projectUserList.add(user1);
        }
        ArrayList<User> collect = projectUserList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(User::getUserId))), ArrayList::new));
        resultHashMap.put("projectUserList",collect);
        return resultHashMap;
    }

    @Override
    public List<JSONObject> getResultPage(HttpServletRequest request, JSONObject data, Pageable page) {
        String taskId = data.getString("taskId");
        SrWorkTask workTask = srWrokTaskRepository.findOne(taskId);
        String dataSetId = data.getString("dataSetId");
        List<JSONObject> jsonObjects = searchData(request, workTask.getWorkId(), dataSetId);
        return jsonObjects;
    }

    @Override
    public Map getFrontPageResult(HttpServletRequest request, Pageable page) {
        String userId = SpringManager.getCurrentUser().getUserId();
        List<SrTaskTreeData> objectByUser = workObjectService.getObjectByUser(null, userId, null, null, null, null, null, null, null);
        JSONObject pageable = pageable(objectByUser, page.getPageNumber()+1, page.getPageSize());
        return pageable;
    }

    List<JSONObject> byConditioin(HttpServletRequest request, String projectId, String userId, Date startTime, Date endTime, String dataStatus, String datasetId, String taskId){
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/document/"+projectId+"/byCondition/?userId="+userId+"&startDate="+startTime+"&endDate="+endTime+"&status="+dataStatus+"&dsId="+datasetId+"&taskId="+taskId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        String s = restTemplate.postForObject(serviceName.toString(), httpEntity, String.class);
        JSONObject jsonObject = JSONArray.parseObject(s);
        String content = jsonObject.getString("content");
        List<JSONObject> jsonObjects = JSONArray.parseArray(content,JSONObject.class);
        return jsonObjects;
    }

    List<JSONObject> searchData(HttpServletRequest request, String projectId, String dataSetId){
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/dataset/project/"+projectId+"/"+dataSetId+"/searchdata?isHighest=false");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        String s = restTemplate.postForObject(serviceName.toString(), httpEntity, String.class);
        JSONObject jsonObject = JSONArray.parseObject(s);
        String content = jsonObject.getString("content");
        List<JSONObject> jsonObjects = JSONArray.parseArray(content,JSONObject.class);
        return jsonObjects;
    }

    @Override
    public List<JSONObject> searchData(String projectId, String datasetIds, String wellNames, HttpServletRequest request) {
        String[] wNames = wellNames.split(",");
        ArrayList<JSONObject> returnlist = new ArrayList<>();
        for (String wName : wNames) {
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            RestTemplate restTemplate = new RestTemplate();
            StringBuffer serviceName = new StringBuffer();

            serviceName.append("http://"+ServerAddr+"/core/dataset/batch/udb/searchdata?projectId="+projectId+"&datasetIds="+datasetIds+"&wellNames="+wName+"");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.set("Authorization",request.getHeader("Authorization"));
            HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
            String s = restTemplate.postForObject(serviceName.toString(), httpEntity, String.class);
            JSONArray jsonArray = JSONArray.parseArray(s);
            List<JSONObject> jsonObjects = jsonArray.toJavaList(JSONObject.class);
            for (JSONObject jsonObject : jsonObjects) {
                returnlist.add(jsonObject);
            }
        }
        return returnlist;
    }

    /**
     * list 分页
     * @param list 结果集
     * @param page 第 page 页
     * @param size 页大小
     * @return
     */
    private JSONObject pageable(List list, int page, int size) {
        int total = list.size();
        int pageSize = size;

        int t = total / pageSize;
        int totalPages = t == 0 ? 1 :
                (total % pageSize == 0 ? t : t + 1);


        int startIndex = pageSize * (page - 1);

        JSONObject result = new JSONObject();
        result.put("total", total);
        result.put("currentPage", page);
        result.put("size", size);
        if (startIndex > total - 1) {
            result.put("content", new ArrayList<>());
            return result;
        }
        if (page < totalPages) {
            int endIndex = startIndex + pageSize;
            result.put("content", list.subList(startIndex, endIndex));
            return result;
        }
        result.put("content", list.subList(startIndex, total));
        return result;
    }

    public static void main(String[] args) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://www.pcep.cloud/core/document/ACTIJD100002253/byCondition/?userId=evtXEIvEljOKry9pzUqfArjx");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoiQ1JQQWRtaW5fSkQiLCJ1c2VyX2lkIjoiNzc0Z2lraHg1cmc3Y2N3ajVraGx4cDZlIiwidXNlcl9uYW1lIjoiQ1JQQWRtaW5fSkQiLCJzY29wZSI6WyJvcGVuaWQiXSwib3JnYW5pemF0aW9uIjoiT1JHQUpEMTAwMDAwMDAwIiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJleHAiOjE2MzYzMzcwNDcsImRpc3BsYXlfbmFtZSI6Iuezu-e7n-euoeeQhuWRmCIsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjMzNzQ1MDQ3MjMxLCJqdGkiOiI0Nzc4YzIyMi0wYTAxLTQwMTQtYTI0NS03ZDJhNWU5MWYxMWEiLCJjbGllbnRfaWQiOiJ3ZWJhcHAifQ.klSO36zXGSRsVvbhNGMCk6rf3K5bUmCTkq6X8DBGUBmyy6nkqVSKCE7AvfHlf1L6g70aOLsAt_-Rwr2KDj0foNfhqEhuTMhfQdyKbwRKYL56IqrWbYFvGEepSE9R1R8D13ebfkRWgHrTgh8gCDKbaybwlMlCzkP9eXjuKS5X2CaYys4GPTDMATbNxpIwmNttGNhSdp2W4Kov4vC11Xxpe6nfdJuGSgfXflyd-kBo02LdGRj5QwRV8AMeDSnAK0vILuFToTtQO9Ko1tl4eJq_wAAfgZ2TOF24oBKZOTS71HYYv1sUVOkk8zLUII-gB6sWKKaKApeNhk6kbG54MkMFQw");
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        String s = restTemplate.postForObject(serviceName.toString(), httpEntity, String.class);
        JSONObject jsonObject = JSONArray.parseObject(s);
        String content = jsonObject.getString("content");
        List<JSONObject> wellList = JSONArray.parseArray(content,JSONObject.class);
        System.out.println(wellList.get(0));

    }
}


@Getter
@Setter
@ToString
class Template{
    String treeId;
    //String isTemplate;
    String templateName;
    String templateLevel;
    String sourceTemplateId;
    String treeType;
    //String purpose;
    //String purposeId;
    //String dataRegion;
    //String remarks;
    //String bsflag;
    //String createUser;
    //String createDate;
    //String updateUser;
    //String updateDate;
}

@Getter
@Setter
@ToString
class Tree{
    String nodeId;
    String treeId;
    String sourceNodeId;
    String nodeName;
    String nodeType;
    String nodeIcon;
    String targetId;
    String sortSequence;
    String remarks;
    Tree[] children;
    String pnodeId;
    String attribute;
    boolean display;
    boolean status;
}
