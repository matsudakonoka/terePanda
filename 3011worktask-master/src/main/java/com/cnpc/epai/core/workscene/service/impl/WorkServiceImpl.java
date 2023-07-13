package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.common.util.User;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.*;
import com.cnpc.epai.core.workscene.mapper.WorkMapper;
import com.cnpc.epai.core.workscene.pojo.vo.*;
import com.cnpc.epai.core.workscene.service.*;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.service.WorkObjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.cnpc.epai.core.workscene.commom.StringConvert.*;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    @Autowired
    private GeoService geoService;

    @Autowired
    private WorkSceneTaskService workTaskService;

    @Autowired
    private WorkNodeService workNodeService;

    @Autowired
    private TreeService treeService;

    @Autowired
    private WorkObjectService workObjectService;

    @Autowired
    private DataService dataService;

    @Autowired
    private OnlineEditService onlineEditService;

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private WorkNavigateTreeService workNavigateTreeService;
    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;

    @Override
    @Transactional
    public Work create(WorkVo workVo) {
        String workId = workVo.getWorkId();
        String dataRegion=workVo.getDataRegion()==null?"TL":workVo.getDataRegion();
        // 修改工作
        if (workId != null && !workId.isEmpty()) {
            Work work=update(workVo);
            return work;
        }
        String templateId = workVo.getTemplateId();
        ChargeUserVo[] chargeUsers = workVo.getChargeUser();
        String workChargeUser = convertToString(chargeUsers);
        JSONObject shareSetting = (JSONObject) JSONObject.toJSON(new ShareVo());
        Work work = Work.builder()
                .workName(workVo.getWorkName())
                .instanceId(templateId)
                .templateId(templateId)
                .templateName(workVo.getTemplateName())
                .geoType(workVo.getGeoType())
                .chargeUser(workChargeUser)
                .startTime(workVo.getStartTime())
                .endTime(workVo.getEndTime())
                .workShare(shareSetting)
                .remarks(workVo.getRemarks())
                .build();
        save(work);
        Geo mainGeo = null;
        List<Object> linkObjList = new ArrayList<>();
        for (JSONObject geoVo : workVo.getWorkObjects()) {
            if(StringUtils.equals("研究对象",geoVo.getString("objectType"))){
                mainGeo = Geo.builder().workId(work.getWorkId())
                        .geoObjId(geoVo.getString("geoObjId"))
                        .geoObjName(geoVo.getString("geoObjName"))
                        .geoType(work.getGeoType())
                        .source(Constants.CREATE)
                        .build();
            }else{
                linkObjList.add(geoVo);
            }
        }
        String geoUuid = new Date().getTime()+"";
        Geo otherObject = Geo.builder()
                .geoId("otherObjectId_"+geoUuid)
                .workId(work.getWorkId())
                .geoObjId("otherObject_"+geoUuid)
                .geoObjName("其他对象")
                .geoType(work.getGeoType())
                .source(Constants.CREATE)
                .createUser(SpringManager.getCurrentUser().getUserId())
                .build();
        JSONObject other = JSONObject.parseObject(JSONObject.toJSONString(otherObject));
        other.put("objectId", "otherObject_"+geoUuid);
        other.put("objectName", "其他对象");
        other.put("objectType", "关联对象");
        other.put("source", "link");
        other.put("createDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        linkObjList.add(other);
        if(mainGeo != null){
            mainGeo.setChildObjects(JSON.toJSONString(linkObjList));
            geoService.save(mainGeo);
        }

        TreeNodeVo[] treeNodes = workVo.getTreeNodes();
        for (TreeNodeVo nodeVo: treeNodes) {
            String nodeId=nodeVo.getNodeId();
            String workid=work.getWorkId();
            int leng=workid.length()+(nodeId.length()-32);
            String workid0=workid.substring(leng);
            String Newid= nodeId+workid0;
            nodeVo.setNodeId(Newid);
        }
        List<WorkNavigateTreeNode> NewTreelist=workVo.getNewTree();
        for (WorkNavigateTreeNode nodeVo: NewTreelist) {
            String nodeId=nodeVo.getNodeId();
            String workid=work.getWorkId();
            int leng=workid.length()+(nodeId.length()-32);
            String workid0=workid.substring(leng);
            String Newid= nodeId+workid0;
            nodeVo.setNodeId(Newid);

            String pnodeId=nodeVo.getPNodeId();
            if(StringUtils.isNotEmpty(pnodeId)){
                int leng2=workid.length()+(pnodeId.length()-32);
                String workid2=workid.substring(leng2);
                String Newpid= pnodeId+workid2;
                nodeVo.setPNodeId(Newpid);
            }
        }
        String treeNodeIds = convertToString(treeNodes);
        List<WorkTask> workTasks = new ArrayList<>();
        for (ChargeUserVo chargeUser : chargeUsers) {
            WorkTask workTask = WorkTask.builder()
                    .taskName(work.getWorkName())
                    .workId(work.getWorkId())
                    .templateId(work.getTemplateId())
                    .chargeUserId(chargeUser.getChargeUserId())
                    .chargeUserName(chargeUser.getChargeUserName())
                    .treeNodeIds(treeNodeIds).build();
            workTasks.add(workTask);
        }
        workTaskService.saveBatch(workTasks);

        List<WorkNode> workNodes = new ArrayList<>();
        for (TreeNodeVo treeNode : treeNodes) {
            WorkNode workNode = WorkNode.builder()
                    .workId(work.getWorkId())
                    .treeNodeId(treeNode.getNodeId())
                    .treeNodeName(treeNode.getNodeName())
                    .startTime(workVo.getStartTime()).endTime(workVo.getEndTime())
                    .chargeLatest(workChargeUser)
                    .build();
            workNodes.add(workNode);
        }
        workNodeService.saveBatch(workNodes);
        //保存T3
        WorkNavigateTree workT3 = WorkNavigateTree.builder()
                .treeId(work.getWorkId())
                .isTemplate("N")
                .templateName(work.getTemplateName())
                .templateLevel("T3")
                .source_templateId(work.getTemplateId())
                .purpose("WORKROOM")
                .purposeId(work.getWorkId())
                .dataRegion(dataRegion)
                .build();
        workNavigateTreeService.save(workT3);
        if(workVo.getNewTree() != null){
            //List<WorkNavigateTreeNode> NewTreelist = JSONObject.parseArray(JSON.toJSONString(workVo.getNewTree()), WorkNavigateTreeNode.class);
            for(int i=0;i<NewTreelist.size();i++){
                WorkNavigateTreeNode obj =NewTreelist.get(i);
                obj.setTreeId(work.getWorkId());
                obj.setCreateUser(work.getCreateUser());
                obj.setCreateDate(work.getCreateDate());
                obj.setUpdateUser(work.getUpdateUser());
                obj.setUpdateDate(work.getUpdateDate());
            }
            workNavigateTreeNodeService.saveBatch(NewTreelist);
        }
        return work;
    }
    private void scanTree2(String dataRegion,Work work,List<WorkNavigateTreeNode> workTreeNode, String PnodeId,List<Map<String, Object>> tree) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            String nodeId = "T3_"+node.get("nodeId");
            String treeId = (String) node.get("treeId");
            String sourceNodeId = (String) node.get("sourceNodeId");
            String nodeName = (String) node.get("nodeName");
            String nodeType = (String) node.get("nodeType");
            String sortSequence = node.get("sortSequence")==null?"1":node.get("sortSequence").toString();
            String remarks = node.get("remarks")==null?"":(String)node.get("remarks");
            String nodeIcon = node.get("nodeIcon")==null?null:(String)node.get("nodeIcon");
            String targetId = node.get("targetId")==null?null:(String)node.get("targetId");
            String attribute = node.get("attribute")==null?null:(String)node.get("attribute");
            //String pnodeId = node.get("pnodeId")==null?null:(String)node.get("pnodeId");
            /*WorkNavigateTreeNode worknodeT3 = WorkNavigateTreeNode.builder()
                    .nodeId(nodeId)
                    .treeId(work.getWorkId())
                    .pNodeId(PnodeId)
                    .sourceNodeId(sourceNodeId)
                    .nodeName(nodeName)
                    .sortSequence(Long.parseLong(sortSequence))
                    .nodeType(nodeType)
                    .targetId(targetId)
                    .dataRegion(dataRegion)
                    .nodeType(nodeType)
                    .build();
            workTreeNode.add(worknodeT3);*/
            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            scanTree2(dataRegion,work,workTreeNode,nodeId, subTree);
        }
    }
    private void scanTree(Map<String, String> typeMap, List<Map<String, Object>> tree) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");

            String nodeType = (String) node.get("nodeType");
            String nodeId = (String) node.get("nodeId");
            if (nodeType != null && nodeType.equals("WORKUNIT")) {
                typeMap.put(nodeId, nodeType);
            }

            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            scanTree(typeMap, subTree);
        }
    }

    public Work update(WorkVo workVo) {
        String workId = workVo.getWorkId();
        ChargeUserVo[] chargeUsers = workVo.getChargeUser();
        String workChargeUser = convertToString(chargeUsers);

        Work work = getById(workVo.getWorkId());
        work.setWorkName(workVo.getWorkName());
        work.setChargeUser(workChargeUser);
        work.setStartTime(workVo.getStartTime());
        work.setEndTime(workVo.getEndTime());
        work.setRemarks(workVo.getRemarks());
        updateById(work);

        QueryWrapper<WorkNode> nodeQueryWrapper = new QueryWrapper<>();
        nodeQueryWrapper.eq("work_id", workId);
        List<WorkNode> treeNodes = workNodeService.list(nodeQueryWrapper);
        String treeNodeIds = convertEntityToString(treeNodes);

        List<WorkTask> workTasks = new ArrayList<>();
        StringBuilder chargeBuilder = new StringBuilder();
        for (ChargeUserVo chargeUserVo : chargeUsers) {
            String userId = chargeUserVo.getChargeUserId();
            QueryWrapper<WorkTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("work_id", workId).eq("charge_user_id", userId);
            WorkTask one = workTaskService.getOne(queryWrapper);
            if (one == null) {
                WorkTask workTask = WorkTask.builder()
                        .taskName(work.getWorkName())
                        .workId(work.getWorkId())
                        .templateId(workVo.getTemplateId())
                        .chargeUserId(userId)
                        .chargeUserName(chargeUserVo.getChargeUserName())
                        .treeNodeIds(treeNodeIds).build();
                workTasks.add(workTask);
                chargeBuilder.append(",").append(workTask.getChargeUserId());
            }
        }
        workTaskService.saveBatch(workTasks);

        // 新的负责人
        String newAddCharge = chargeBuilder.toString();
        if (!StringUtils.isEmpty(newAddCharge)) {
            for (WorkNode workNode : treeNodes) {
                String charges = workNode.getChargeLatest();
                StringBuilder stringBuilder = new StringBuilder(charges);
                String newCharges = stringBuilder.append(",").append(newAddCharge).toString();
                workNode.setChargeLatest(newCharges);
                workNodeService.updateById(workNode);
            }
        }

        return work;
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean configTemplate(String workId, ReportTemplateVo reportTemplate) {

        try {
            UpdateWrapper<Work> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("report_output", reportTemplate.isReportOutput())
                    .set("report_template_id", reportTemplate.getReportTemplateId())
                    .set("report_template_name", reportTemplate.getReportTemplateName())
                    .eq("work_id", workId);
            update(updateWrapper);

            Work work = getById(workId);
            if (work == null) {
                throw new IllegalArgumentException("工作不存在, workId: " + workId);
            }

            List<TreeNodeVo> deleteNode = new ArrayList<>();
            List<TreeNodeVo> addNode = new ArrayList<>();

            Set<String> newNode = new HashSet<>();
            Set<String> oldNode = new HashSet<>();

            TreeNodeVo[] treeNodeVos = reportTemplate.getTreeNodes();
            for (TreeNodeVo treeNodeVo : treeNodeVos) {
                newNode.add(treeNodeVo.getNodeId());
            }

            QueryWrapper<WorkNode> nodeQueryWrapper = new QueryWrapper<>();
            nodeQueryWrapper.eq("work_id", workId);
            List<WorkNode> list = workNodeService.list(nodeQueryWrapper);
            for (WorkNode workNode : list) {
                oldNode.add(workNode.getTreeNodeId());
                if (!newNode.contains(workNode.getTreeNodeId())) {
                    TreeNodeVo treeNodeVo = new TreeNodeVo();
                    treeNodeVo.setNodeId(workNode.getTreeNodeId());
                    treeNodeVo.setNodeName(workNode.getTreeNodeName());
                    deleteNode.add(treeNodeVo);
                }
            }
            for (TreeNodeVo treeNodeVo : treeNodeVos) {
                if (!oldNode.contains(treeNodeVo.getNodeId())) {
                    addNode.add(treeNodeVo);
                }
            }
            /** 更新任务 */
            QueryWrapper<WorkTask> taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("work_id", workId);
            List<WorkTask> workTasks = workTaskService.list(taskWrapper);
            for (WorkTask workTask : workTasks) {
                boolean isUpdate = false;
                String nodes = workTask.getTreeNodeIds();
                Set<String> set = convertToSet(nodes);
                for (TreeNodeVo treeNodeVo : deleteNode) {
                    if (set.contains(treeNodeVo.getNodeId())) {
                        set.remove(treeNodeVo.getNodeId());
                        isUpdate = true;
                    }
                }
                for (TreeNodeVo treeNodeVo : addNode) {
                    if (!set.contains(treeNodeVo.getNodeId())) {
                        set.add(treeNodeVo.getNodeId());
                        isUpdate = true;
                    }
                }
                if (isUpdate) {
                    workTask.setTreeNodeIds(convertToString(set));
                    workTaskService.updateById(workTask);
                }
            }

            for (TreeNodeVo treeNodeVo : deleteNode) {
                QueryWrapper<WorkNode> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.eq("work_id", workId).eq("tree_node_id", treeNodeVo.getNodeId());
                workNodeService.remove(deleteWrapper);
            }

            for (TreeNodeVo treeNodeVo : addNode) {
                WorkNode workNode = WorkNode.builder()
                        .workId(workId)
                        .treeNodeId(treeNodeVo.getNodeId())
                        .treeNodeName(treeNodeVo.getNodeName())
                        .chargeLatest(work.getChargeUser())
                        .startTime(work.getStartTime())
                        .endTime(work.getEndTime())
                        .build();
                        /*.chargeLatest(work.getChargeUser())
                        .startTime(work.getStartTime())
                        .endTime(work.getEndTime())
                        .recommend(work.getRemarks()).build();*/
                workNodeService.save(workNode);
            }
            //更新T3树
            QueryWrapper<WorkNavigateTreeNode> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("tree_id", workId);
            workNavigateTreeNodeService.remove(deleteWrapper);
            if(reportTemplate.getNewTree() != null){
                List<WorkNavigateTreeNode> NewTreelist=reportTemplate.getNewTree();
                for(int i=0;i<NewTreelist.size();i++){
                    WorkNavigateTreeNode obj =NewTreelist.get(i);
                    obj.setTreeId(work.getWorkId());
                    obj.setCreateUser(work.getCreateUser());
                    obj.setCreateDate(work.getCreateDate());
                    obj.setUpdateUser(work.getUpdateUser());
                    obj.setUpdateDate(work.getUpdateDate());
                }
                workNavigateTreeNodeService.saveBatch(NewTreelist);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void configShare(String workId, ShareVo share) {
        JSONObject shareSetting = (JSONObject) JSONObject.toJSON(share);
        Work work = Work.builder().workId(workId).workShare(shareSetting).build();
        updateById(work);

    }

    @Override
    public Object getTree(String workId, String nodeName, String type,String pNodeId) {
        Work work = getById(workId);
        if (work == null) {
            throw new IllegalStateException("树不存在");
        }
        String treeid=work.getTemplateId();
        if("T3".equals(type)){
            treeid=workId;
        }
        List<Map<String, Object>> tree = treeService.getTree2(treeid, pNodeId);
        // 获取已选节点1:授权节点信息
        List<WorkNode> workNodes = workNodeService.getNode(workId);
        Set<String> nodeSet1 = new HashSet<>();
        for (WorkNode workNode : workNodes) {
            nodeSet1.add(workNode.getTreeNodeId());
        }
        // 获取已选节点2：T3对应的SourceNodeId
        QueryWrapper<WorkNavigateTreeNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tree_id",workId);
        List<WorkNavigateTreeNode> workTreeNodelist = workNavigateTreeNodeService.list(queryWrapper);
        Set<String> nodeSet2 = new HashSet<>();
        for (WorkNavigateTreeNode workNode : workTreeNodelist) {
            if (nodeSet1.contains(workNode.getNodeId())) {
                nodeSet2.add(workNode.getSourceNodeId());
            }
        }
        // 添加树节点是否已选取字段
        setTreeNodeMark(nodeSet2, tree);
        // flashBack(nodeSet, tree);
        if (!StringUtils.isEmpty(nodeName)) {
            searchTreeWithName(tree, nodeName);
        }
        return tree;
    }
    @Override
    public Object getTreeByUser(String workId, String nodeName) {
        Work work = getById(workId);
        if (work == null) {
            throw new IllegalStateException("树不存在");
        }
        List<Map<String, Object>> tree = treeService.getTree2(work.getWorkId(), null);

        String userId = SpringManager.getCurrentUser().getUserId();
        // String userId = "n8ON3ACANcUpKsVBeD1uknDhSN6jneVM";
        WorkTask workTask = workTaskService.getWorkTask(work.getWorkId(), userId);

        if (workTask == null) {
            return new ArrayList<>();
        }

        String[] workNodes = workTask.getTreeNodeIds().split(",");

        // 所有 workUnit 类型节点
        Map<String, String> typeMap = new HashMap<>();
        scanTree(typeMap, tree);

        Set<String> nodeSet = new HashSet<>();
        for (String workNode : workNodes) {
            if (typeMap.containsKey(workNode)) {
                nodeSet.add(workNode);
            }
        }
        // 添加树节点是否已选取字段
        flashBack(nodeSet, tree);
        // 去掉不是 workUnit 类型的节点
        searchTreeWithWorkNode(tree, nodeSet);
        if (!StringUtils.isEmpty(nodeName)) {
            searchTreeWithName(tree, nodeName);
        }
        return tree;
    }

    private List<Map<String, Object>> searchTreeWithWorkNode(List<Map<String, Object>> tree, Set<String> nodes) {
        if (tree == null || tree.size() == 0) {
            return null;
        }
        List<Map<String, Object>> res = new ArrayList<>();
        List<Map<String, Object>> add = new ArrayList<>();
        List<Map<String, Object>> remove = new ArrayList<>();
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            List<Map<String, Object>> sub = searchTreeWithWorkNode(subTree, nodes);
            String nodeId = (String) node.get("nodeId");
            if (!nodes.contains(nodeId)) {
                if (sub == null || sub.size() == 0) {
                    remove.add(node);
                } else {
                    res.add(node);
                }
            } else {
                res.add(node);
            }
        }
        tree.removeAll(remove);
        return res;
    }

    private List<Map<String, Object>> searchTreeWithName(List<Map<String, Object>> tree, String name) {
        if (tree == null || tree.size() == 0) {
            return null;
        }
        List<Map<String, Object>> res = new ArrayList<>();
        List<Map<String, Object>> add = new ArrayList<>();
        List<Map<String, Object>> remove = new ArrayList<>();
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            List<Map<String, Object>> sub = searchTreeWithName(subTree, name);
            String nodeName = (String) node.get("nodeName");
            if (!nodeName.contains(name)) {
                if (sub != null && sub.size() > 0) {
                    add.addAll(sub);
                    res.addAll(add);
                }
                remove.add(node);
            } else {
                res.add(node);
            }
        }
        tree.removeAll(remove);
        tree.addAll(add);
        return res;
    }


    /**
     * 递归树添加标记
     * @param nodeSet
     * @param tree
     */
    private void setTreeNodeMark(Set<String> nodeSet, List<Map<String, Object>> tree) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            String nodeId = (String) node.get("sourceNodeId");
            if (nodeSet.contains(nodeId)) {
                node.put("checked", true);
            } else {
                node.put("checked", false);
            }
            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            setTreeNodeMark(nodeSet, subTree);
        }
    }

    /**
     * 回溯树添加标记
     * @param nodeSet
     * @param tree
     * @return
     */
    private boolean flashBack(Set<String> nodeSet, List<Map<String, Object>> tree) {
        boolean flag = false;
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            if (subTree == null || subTree.size() == 0) {
                String nodeId = (String) node.get("nodeId");
                if (nodeSet.contains(nodeId)) {
                    flag = true;
                    node.put("checked", true);
                } else {
                    node.put("checked", false);
                }
                continue;
            }
            if (flashBack(nodeSet, subTree)) {
                node.put("checked", true);
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public List getMinNodes(String treeId, String nodeId) {
        List nodes = treeService.getTree(treeId, nodeId);
        return nodes;
    }

    @Override
    public JSONObject getWork(String workId) {
        Work work = getById(workId);
        QueryWrapper<Geo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id", workId);
        //queryWrapper.eq("source", Constants.CREATE);//????因为好多数据为空：所以注释了
        //List<Geo> geos = geoService.list(queryWrapper);
        Geo one = geoService.getOne(queryWrapper);
        List<Object> workObjects = new ArrayList<>();
        GeoVo geoVo = new GeoVo();
        BeanUtils.copyProperties(one, geoVo);
        workObjects.add(geoVo);
        if(StringUtils.isNotEmpty(one.getChildObjects())){
            JSONArray jsonArray = JSON.parseArray(one.getChildObjects());
            for(Object o : jsonArray){
                ((JSONObject)o).put("linkObject",true);
                workObjects.add(o);
            }
        }
        JSONObject object = (JSONObject) JSON.toJSON(work);

        List<ChargeUserVo> charges = new ArrayList<>();
        String chargeUser = work.getChargeUser();
        if (chargeUser != null) {
            String[] userIds = chargeUser.split(",");
            QueryWrapper<WorkTask> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("charge_user_id", Arrays.asList(userIds)).eq("work_id", workId);
            List<WorkTask> list = workTaskService.list(queryWrapper1);
            for (WorkTask workTask : list) {
                ChargeUserVo chargeUserVo = new ChargeUserVo();
                chargeUserVo.setChargeUserId(workTask.getChargeUserId());
                chargeUserVo.setChargeUserName(workTask.getChargeUserName());
                charges.add(chargeUserVo);
            }
        }
        //WorkTask表的所有数据
        if (chargeUser != null) {
            QueryWrapper<WorkTask> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("work_id", workId);
            List<WorkTask> list = workTaskService.list(queryWrapper1);
            object.put("WorkTask", list);
        }
        //work_node表的所有数据
        if (chargeUser != null) {
            QueryWrapper<WorkNode> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("work_id", workId);
            List<WorkNode> list = workNodeService.list(queryWrapper1);
            object.put("workNode", list);
        }
        object.put("chargeUser", charges);
        object.put("workObjects", workObjects);
        addReportState(workId, work.getReportTemplateId(), object);
        return object;
    }

    private void addReportState(String workId, String reportTemplateId, JSONObject object) {
        if (StringUtils.isEmpty(reportTemplateId)) {
            object.put("reportIsExist", false);
            return;
        }

        JSONObject file = onlineEditService.findFile(workId, reportTemplateId);
        String id = file.getString("id");
        if (StringUtils.isEmpty(id)) {
            object.put("reportIsExist", false);
        } else {
            object.put("reportIsExist", true);
        }
    }

    @Override
    public Object getDocument(String workId, String userId, String status, Date startTime, Date endTime) {

        String projectId = "ACTITL100001656";

        List<SrTaskTreeData> objectByUser =
                workObjectService.getObjectByUser(workId, userId, null, status, startTime, endTime, null,null);
        Set<String> set = new HashSet<>();
        for (SrTaskTreeData srTaskTreeData : objectByUser) {
            String fileId = srTaskTreeData.getFileId();
            set.add(fileId);
        }

        List<Map<String, Object>> document = dataService.getDocument(projectId, userId, status,
                startTime.toString(), endTime.toString());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> doc : document) {
            String fileId = (String) doc.get("file_id");
            if (set.contains(fileId)) {
                result.add(doc);
            }
        }

        return result;
    }

    @Override
    public boolean isExist(String template, String geoType, GeoVo[] geoVos) {
        QueryWrapper<Work> workQueryWrapper = new QueryWrapper<>();
        workQueryWrapper.eq("template_id", template).eq("geo_type", geoType);
        // List<Work> list = this.baseMapper.selectList(workQueryWrapper);
        List<Work> works = list(workQueryWrapper);
        if (works == null) {
            return false;
        }

        Set<String> set = new HashSet<>();
        for (GeoVo geoVo : geoVos) {
            set.add(geoVo.getGeoObjId());
        }

        for (Work work : works) {
            QueryWrapper<Geo> geoQueryWrapper = new QueryWrapper<>();
            geoQueryWrapper.eq("work_id", work.getWorkId()).eq("geo_type", geoType);
            List<Geo> geos = geoService.list(geoQueryWrapper);
            if (geos.size() != set.size()) {
                continue;
            }

            boolean flag = false;
            for (Geo geo : geos) {
                if (!set.contains(geo.getGeoObjId())) {
                    flag = true;
                }
            }
            if (!flag) {
                return !flag;
            }
        }

        return false;
    }

    @Override
    public Object getWorkByTemplate(String template, boolean withStatus) {
        // TODO 查询工作状态逻辑

        List<Object> result = new ArrayList<>();
        QueryWrapper<Work> workWrapper = new QueryWrapper<>();
        workWrapper.in("template_id", template.split(","));
        List<Work> works = list(workWrapper);

        for (Work work : works) {
            QueryWrapper<Geo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("work_id", work.getWorkId()).eq("source", Constants.CREATE);
            List<Geo> geos = geoService.list(queryWrapper);
            JSONObject object = (JSONObject) JSON.toJSON(work);
            List<GeoVo> workObjects = new ArrayList<>();
            for (Geo geo : geos) {
                GeoVo geoVo = new GeoVo();
                BeanUtils.copyProperties(geo, geoVo);
                workObjects.add(geoVo);
            }
            object.put("geos", workObjects);
            result.add(object);
        }

        return result;
    }

    @Override
    public List<Geo> getGeo(String workId) {
        Work work = getById(workId);
        if (work == null)
            throw new IllegalArgumentException("工作不存在");
        QueryWrapper<Geo> geoWrapper = new QueryWrapper<>();
        geoWrapper.eq("work_id", workId).eq("geo_type", work.getGeoType());
        return geoService.list(geoWrapper);
    }

    @Override
    public Object workNodes(String workId) {
        List<Object> result = new ArrayList<>();
        QueryWrapper<WorkNode> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("work_id", workId);
        List<WorkNode> workNodes = workNodeService.list(nodeWrapper);
        WorkTask workTask = workTaskService.getWorkTask(workId, SpringManager.getCurrentUser().getUserId());
        if (workTask == null) {
            for (WorkNode workNode : workNodes) {
                JSONObject o = (JSONObject) JSONObject.toJSON(workNode);
                o.put("taskId", null);
                result.add(o);
            }
            return result;
        }

        Set<String> set = convertToSet(workTask.getTreeNodeIds());
        for (WorkNode workNode : workNodes) {
            JSONObject o = (JSONObject) JSONObject.toJSON(workNode);
            if (set.contains(workNode.getTreeNodeId())) {
                o.put("taskId", workTask.getTaskId());
            } else {
                o.put("taskId", null);
            }
            result.add(o);
        }
        return result;
    }

    @Override
    public List<Work> getWorkByShare(ShareVo share) {
        List<Work> result = new ArrayList<>();
        List<Work> works = list();
        for (Work work : works) {
            JSONObject workShare = work.getWorkShare();
            if (workShare != null) {
                if (workShare.getBoolean("share")) {
                    result.add(work);
                }
            }
        }
        return result;
    }

    @Override
    public Object getDataObject(String workId, String nodeId, String dataSets) {
        List<Map> retList = new ArrayList<>();
        List<Geo> workObjects = getGeo(workId);
        //优先添加研究对象
        workObjects.forEach(geo->{
            Map<String, Object> o = new HashMap<>(3);
            o.put("objectId", geo.getGeoObjId());
            o.put("objectName", geo.getGeoObjName());
            o.put("source",geo.getSource());
            o.put("geoType", geo.getGeoType());
            o.put("objectType","研究对象");
            retList.add(o);
        });

        //查询所有引用对象，结果集已去重
        List<Map> objectContentList = workObjectService.getObjectContentList(workId, nodeId, dataSets);
        objectContentList.forEach(m->{
            boolean b = retList.stream().anyMatch(rm -> StringUtils.equals(rm.get("objectId").toString(), m.get("objectId").toString()));
            if(!b){
                m.put("source","ref");
                m.put("objectType",m.get("objectType")!=null?m.get("objectType"):"引用对象");
                retList.add(m);
            }
        });
        /*Set<String> ids = new HashSet<>();
        for (Map<String, Object> m : objectContentList) {
            ids.add((String) m.get("objectId"));
        }
        for (Geo geo : workObjects) {
            if (!ids.contains(geo.getGeoObjId())) {
                Map<String, Object> o = new HashMap<>(3);
                o.put("objectId", geo.getGeoObjId());
                o.put("objectName", geo.getGeoObjName());
                o.put("source",geo.getSource());
                objectContentList.add(o);
                ids.add(geo.getGeoObjId());
            }
        }*/
        //添加关联对象
        if(workObjects!=null && !workObjects.isEmpty()){
            if(StringUtils.isNotEmpty(workObjects.get(0).getChildObjects())){
                List<Map> maps = JSON.parseArray(workObjects.get(0).getChildObjects(), Map.class);
                maps.stream().forEach((map)->{
                    if(!retList.stream().anyMatch(rm->StringUtils.equals(rm.get("objectId").toString(),map.get("objectId").toString()))){
                        map.put("source","link");
                        map.put("objectType",map.get("objectType")!=null?map.get("objectType"):"关联对象");
                        retList.add(map);
                    }
                    /*if(!ids.contains(map.get("objectId"))){
                        map.put("source","link");
                        objectContentList.add(map);
                    }*/
                });
            }
        }

        return retList;
    }


}
