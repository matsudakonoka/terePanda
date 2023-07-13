package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.common.util.User;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.*;
import com.cnpc.epai.core.workscene.mapper.WorkMapper;
import com.cnpc.epai.core.workscene.pojo.vo.WorkMulitVo;
import com.cnpc.epai.core.workscene.service.*;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.mapper.SrTaskTreeDataMapper;
import com.cnpc.epai.core.worktask.repository.SrTaskTreeDataRepository;
import com.cnpc.epai.core.worktask.service.impl.WorkMulitObjectServiceImpl;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 谁家的小乖
 * @since 2022年11月7日
 */
@Service
@Transactional
public class WorkMulitServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkMulitService {

    @Autowired
    private GeoService geoService;

    @Autowired
    private WorkSceneTaskService workTaskService;

    @Autowired
    private WorkNodeService workNodeService;

    @Autowired
    private WorkNavigateTreeService workNavigateTreeService;

    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;

    /**
     * @author 谁家的小乖
     * @since 2022年11月7日
     */
    @Override
    public ApiResult createWorkByReport(WorkMulitVo workMulitVo) {
        ApiResult apiResult = ApiResult.newInstance();
        /*工作主表 sr_work_msg,工作对应的研究对象表 sr_work_geo,T3树  sr_navigate_tree,T3树节点表 sr_navigate_tree_node,人员授权表 sr_work_node,工作授权表 sr_work_task */
        String objectId = workMulitVo.getObjectId();
        String objectName = workMulitVo.getObjectName();
        String objectType = workMulitVo.getObjectType();
        String templateId = workMulitVo.getTemplateId();
        List<JSONObject> usersAuth = workMulitVo.getUsersAuth();
        System.out.println("usersAuth = " + usersAuth);
        List<JSONObject> chargeUsers = workMulitVo.getChargeUsers();
        Date startTime = workMulitVo.getStartTime();
        Date endTime = workMulitVo.getEndTime();
        User currentUser = SpringManager.getCurrentUser();
        WorkNavigateTree navigateTree = workNavigateTreeService.getById(templateId);
        List<WorkNavigateTreeNode> treeNodes = workNavigateTreeNodeService.list(new QueryWrapper<WorkNavigateTreeNode>().eq("tree_id", templateId));
        Date date = new Date();
        for (JSONObject userAuth : usersAuth) {
            for (WorkNavigateTreeNode treeNode : treeNodes) {
                if (StringUtils.equals(treeNode.getNodeId(), userAuth.getString("authNodId"))) {
                    userAuth.put("authNodName", treeNode.getNodeName());
                }
            }
        }
        String chargeUserIds = "";
        StringBuffer chargeUserIdsBuffer = new StringBuffer();
        for (JSONObject chargeUser : chargeUsers) {
            chargeUserIdsBuffer.append(chargeUser.getString("chargeUserId")).append(",");
        }
        chargeUserIds = chargeUserIdsBuffer.substring(0, chargeUserIdsBuffer.length() - 1);
        Work work = Work.builder()
                .workName(objectName + objectType + navigateTree.getTemplateName())
                .instanceId(templateId)
                .templateId(templateId)
                .templateName(navigateTree.getTemplateName())
                .geoType(objectType)
                .chargeUser(chargeUserIds)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        save(work);     //sr_work_msg 工作主表
        System.out.println("创建的工作sr_work_msg是 = " + JSON.toJSONString(work));
        String geoUuid = new Date().getTime()+"";
        Geo otherObject = Geo.builder()
                .geoId("otherObjectId_"+geoUuid)
                .workId(work.getWorkId())
                .geoObjId("otherObject_"+geoUuid)
                .geoObjName("其他对象")
                .geoType(objectType)
                .source(Constants.CREATE)
                .createUser(currentUser.getUserId())
                .build();
        JSONObject other = JSONObject.parseObject(JSONObject.toJSONString(otherObject));
        other.put("objectId", "otherObject_"+geoUuid);
        other.put("objectName", "其他对象");
        other.put("objectType", "关联对象");
        other.put("source", "link");
        other.put("createDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        JSONArray array = new JSONArray();
        array.add(other);
        Geo mainGeo = Geo.builder()
                .workId(work.getWorkId())
                .geoObjId(objectId)
                .geoObjName(objectName)
                .geoType(objectType)
                .source(Constants.CREATE)
                .childObjects(JSONArray.toJSONString(array))
                .build();
        geoService.save(mainGeo); //sr_work_geo 工作对应的研究对象表
        System.out.println("创建的研究对象sr_work_geo是 = " + JSON.toJSONString(mainGeo));
        String nodeIds = "";
        StringBuffer nodeIdsBuffer = new StringBuffer();
        for (WorkNavigateTreeNode treeNode : treeNodes) {
            treeNode.setNodeId(genT3NodeId(treeNode.getNodeId(), work.getWorkId()));// 32
            if (StringUtils.isNotEmpty(treeNode.getPNodeId())) {
                treeNode.setPNodeId(genT3NodeId(treeNode.getPNodeId(), work.getWorkId()));
            }
            treeNode.setTreeId(work.getWorkId());
            treeNode.setCreateDate(date);
            treeNode.setUpdateDate(date);
            treeNode.setCreateUser(currentUser.getUserId());
            treeNode.setUpdateUser(currentUser.getUserId());
            nodeIdsBuffer.append(treeNode.getNodeId()).append(",");
        }
        nodeIds = nodeIdsBuffer.substring(0, nodeIdsBuffer.length() - 1);
        workNavigateTreeNodeService.saveBatch(treeNodes);//sr_navigate_tree_node T3树节点表
        System.out.println("创建的T3树节点表sr_navigate_tree_node是 = " + JSON.toJSONString(treeNodes));
        List<WorkTask> workTasks = new ArrayList<>();
        for (JSONObject chargeUser : chargeUsers) {
            WorkTask workTask = WorkTask.builder()
                    .taskName(work.getWorkName())
                    .workId(work.getWorkId())
                    .templateId(work.getTemplateId())
                    .chargeUserId(chargeUser.getString("chargeUserId"))
                    .chargeUserName(chargeUser.getString("chargeUserName"))
                    .treeNodeIds(nodeIds)
                    .createUser(currentUser.getUserId())
                    .createDate(date)
                    .updateUser(currentUser.getUserId())
                    .updateDate(date)
                    .build();
            workTasks.add(workTask);
        }
        workTaskService.saveBatch(workTasks);// sr_work_task 工作授权表 按人数插入
        System.out.println("创建的工作授权表sr_work_task是 = " + JSON.toJSONString(workTasks));
        List<WorkNode> workNodes = new ArrayList<>();
        for (JSONObject authNode : usersAuth) {
            WorkNode workNode = WorkNode.builder()
                    .workId(work.getWorkId())
                    .treeNodeId(genT3NodeId(authNode.getString("authNodId"), work.getWorkId()))
                    .treeNodeName(authNode.getString("authNodName"))
                    .startTime(workMulitVo.getStartTime())
                    .endTime(workMulitVo.getEndTime())
                    .chargeLatest(authNode.getString("userId"))
                    .createUser(currentUser.getUserId())
                    .createDate(date)
                    .updateUser(currentUser.getUserId())
                    .updateDate(date)
                    .build();
            workNodes.add(workNode);
        }
        workNodeService.saveBatch(workNodes);//sr_work_node 人员授权表 按节点插入
        System.out.println("创建的人员授权表sr_work_node是 = " + JSON.toJSONString(workNodes));
        WorkNavigateTree workT3 = WorkNavigateTree.builder()
                .treeId(work.getWorkId())
                .isTemplate("N")
                .templateName(navigateTree.getTemplateName())
                .templateLevel("T3")
                .source_templateId(work.getTemplateId())
                .purpose("WORKROOM")
                .purposeId(work.getWorkId())
                .createUser(currentUser.getUserId())
                .createDate(date)
                .updateUser(currentUser.getUserId())
                .updateDate(date)
                .build();
        workNavigateTreeService.save(workT3);//sr_navigate_tree T3树
        System.out.println("创建的T3树sr_navigate_tree是 = = " + JSON.toJSONString(workT3));
        List<WorkTask> tasks = workTaskService.list(new LambdaQueryWrapper<WorkTask>()
                .eq(WorkTask::getWorkId, work.getWorkId())
                .eq(WorkTask::getChargeUserId, chargeUsers.get(0).getString("chargeUserId")));
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("work", work);
        resultMap.put("taskId", tasks.get(0).getTaskId());
        apiResult.setResult(resultMap);
        apiResult.setMsg("创建成功");
        apiResult.setFlag(true);
        return apiResult;
    }

    /**
     * @author 谁家的小乖
     * @since 2022年11月7日
     * //  生成T3节点的id保证不超过32位
     */
    private String genT3NodeId(String nodeId, String workId) {
        if (nodeId.length() <= 24) {
            return nodeId + "_" + workId.substring(12);
        } else {
            return nodeId.substring(0, 24) + "_" + workId.substring(12);
        }
    }

    @Autowired
    WorkMulitObjectServiceImpl workMulitObjectServiceImpl;

    @Autowired
    SrTaskTreeDataRepository srTaskTreeDataRepo;

    @Autowired
    SrTaskTreeDataMapper srTaskTreeDataMapper;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    /**
     * @author 谁家的小乖
     * @since 2022年11月15日
     */
    public synchronized ApiResult saveUpdateByOtherObject(String projectId,
                                                          JSONArray data,
                                                          SrTaskTreeData srTaskTreeData,
                                                          String dataTargetTypeZt,
                                                          HttpServletRequest httpServletRequest) throws Exception {
        ApiResult apiResult = ApiResult.newInstance();
        List<Geo> geoList = geoService.list(new QueryWrapper<Geo>().lambda()
                .eq(Geo::getWorkId, srTaskTreeData.getWorkId())
                .eq(Geo::getGeoObjId, srTaskTreeData.getObjectId())
                .eq(Geo::getGeoObjName, srTaskTreeData.getObjectName())
                .eq(Geo::getSource, Constants.CREATE)
                .isNotNull(Geo::getChildObjects));
        if (geoList.size() > 0) {
            String childObjects = geoList.get(0).getChildObjects();
            String otherArray = JSONArray.parseArray(childObjects).get(0).toString();
            JSONObject otherObject = JSONObject.parseObject(otherArray);
            if (StringUtils.equals(otherObject.get("objectName").toString(), "其他对象")) {
                String otherObjectId = otherObject.get("objectId").toString();
                String otherObjectName = otherObject.get("objectName").toString();
                srTaskTreeData.setObjectId(otherObjectId);
                srTaskTreeData.setObjectName(otherObjectName);
                srTaskTreeData.setDataTargetType(dataTargetTypeZt);
                List<Map> maps = JSON.parseArray(JSON.toJSONString(data), Map.class);
                for (Map cm : maps) {
                    cm.put("objectId", otherObjectId);
                    cm.put("objectName", otherObjectName);
                }
                JSONArray rowsMX = JSONArray.parseArray(JSONArray.toJSONString(maps));
                if (StringUtils.equals("成果列表", dataTargetTypeZt)) {//区分是成果还是资料，成果列表 研究资料
                    Specification<SrTaskTreeData> spec = (root, criteriaQuery, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("workId"), srTaskTreeData.getWorkId()));
                        predicates.add(criteriaBuilder.equal(root.get("datasetId"), srTaskTreeData.getDatasetId()));
                        predicates.add(criteriaBuilder.equal(root.get("nodeId"), srTaskTreeData.getNodeId()));
                        predicates.add(criteriaBuilder.equal(root.get("firstChoice"), "A"));
                        predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                        predicates.add(criteriaBuilder.equal(root.get("datasetType"), "file"));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                    };
                    List<SrTaskTreeData> list = srTaskTreeDataRepo.findAll(spec);
                    if (list == null || list.size() == 0) {
                        srTaskTreeData.setFirstChoice("A");
                        srTaskTreeData.setDatasetType("file");
                    } else {
                        srTaskTreeData.setDatasetType("file");
                        SrTaskTreeData sw = list.get(0);
                        sw.setFirstChoice("C");
                        srTaskTreeDataRepo.save(sw);
                        srTaskTreeData.setFirstChoice("A");
                    }
                    workMulitObjectServiceImpl.genFileName(srTaskTreeData);
                    apiResult = processSaveResuleSrTreeData(srTaskTreeData, rowsMX, projectId, httpServletRequest);
                } else {
                    apiResult = processSaveMaterialSrTreeData(srTaskTreeData, rowsMX, projectId, httpServletRequest);
                }
            } else {
                apiResult.setFlag(false);
                apiResult.setResult(null);
                apiResult.setMsg("当前工作下未找到 ”其他对象“！");
            }
        } else {
            apiResult.setFlag(false);
            apiResult.setResult(null);
            apiResult.setMsg("当前工作下研究对象为空！ ");
        }
        return apiResult;
    }

    //保存成果数据
    private ApiResult processSaveResuleSrTreeData(SrTaskTreeData srTaskTreeData,
                                                  JSONArray rowsMX,
                                                  String projectId,
                                                  HttpServletRequest httpServletRequest) throws Exception {
        ApiResult apiResult = ApiResult.newInstance();
        if (srTaskTreeData.getDataType().equals("非结构化")) {
            List<Map> maps = JSON.parseArray(JSON.toJSONString(rowsMX), Map.class);
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
            String token = httpServletRequest.getHeader("Authorization");
            String baseUrl = "http://" + ServerAddr + "/core/document/" + projectId + "/" + srTaskTreeData.getDatasetId() + "/项目成果?";
            if (maps.size() > 1) {//如果dataContent有多条记录，生成多个主记录//存在多条记录
                List<SrTaskTreeData> successList = new ArrayList<>();
                srTaskTreeData.setDataContent(null);
                for (Map cm : maps) {
                    SrTaskTreeData taskTreeData = JSON.parseObject(JSON.toJSONString(srTaskTreeData), SrTaskTreeData.class);
                    taskTreeData.setId(ShortUUID.randomUUID().replace("-", ""));
                    if (workMulitObjectServiceImpl.tupian(taskTreeData, cm)) {
                        continue;
                    }
                    List<Map> contentData = new ArrayList<>();
                    contentData.add(cm);
                    taskTreeData.setDataContent(contentData);
                    try {
                        workMulitObjectServiceImpl.saveToDocument(client, baseUrl, token, taskTreeData);
                        srTaskTreeDataRepo.save(taskTreeData);
                        successList.add(taskTreeData);
                    } catch (Exception e) {
                        System.out.println("调用文档中心接口失败，接口返回信息：" + e.getMessage());
                    }
                }
                if (successList.isEmpty()) {
                    apiResult.setFlag(false);
                    apiResult.setResult(null);
                    apiResult.setMsg("没有满足条件保存的数！");
                } else {
                    apiResult.setFlag(true);
                    apiResult.setResult(successList.get(0));//任意返回一个
                    apiResult.setMsg("保存成功");
                }
            } else {
                srTaskTreeData.setId(ShortUUID.randomUUID().replace("-", ""));
                srTaskTreeData.setDataContent(JSON.parseArray(JSON.toJSONString(rowsMX), Map.class));
                Map cm = maps.get(0);
                if (workMulitObjectServiceImpl.tupian(srTaskTreeData, cm)) {
                    throw new Exception("明细数据：图片ID或图片名称为空");
                }
                try {
                    workMulitObjectServiceImpl.saveToDocument(client, baseUrl, token, srTaskTreeData);
                    apiResult.setFlag(true);
                    apiResult.setResult(srTaskTreeDataRepo.save(srTaskTreeData));
                    apiResult.setMsg("保存成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    apiResult.setFlag(false);
                    apiResult.setResult(e.getMessage());
                    apiResult.setMsg("保存到工作室非结构化文档处理报错！");
                }
            }
        } else {
            apiResult.setFlag(false);
            apiResult.setResult(null);
            apiResult.setMsg("结构化数据不做处理！");
        }
        return apiResult;
    }

    //保存研究资料数据
    private ApiResult processSaveMaterialSrTreeData(SrTaskTreeData srTaskTreeData, JSONArray rowsMX, String projectId, HttpServletRequest httpServletRequest) {
        ApiResult apiResult = ApiResult.newInstance();
        String msg = "";
        int successNum = 0;
        List<Map> xnewlist = JSON.parseArray(JSON.toJSONString(rowsMX), Map.class);// 研究资料特殊处理
        Map<String, List<Map>> groupData = new HashMap<>();
        List<Map> groupDataOpt;
        for (Map map : xnewlist) {
            if (groupData.containsKey(map.get("objectId"))) {
                groupData.get(map.get("objectId").toString()).add(map);
            } else {
                groupDataOpt = new ArrayList<>();
                groupDataOpt.add(map);
                groupData.put(map.get("objectId").toString(), groupDataOpt);
            }
        }
        if (StringUtils.equals("非结构化", srTaskTreeData.getDataType())) {//非结构化的保存
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
            String text = JSON.toJSONString(srTaskTreeData);
            String baseUrl = "http://" + ServerAddr + "/core/document/" + projectId + "/" + srTaskTreeData.getDatasetId() + "/项目成果?";
            String token = httpServletRequest.getHeader("Authorization");
            for (String k : groupData.keySet()) {
                List<Map> listMap = groupData.get(k);
                if (listMap.size() > 1) {//分为两种情况，listMap大小为1，listMap大小大于1
                    for (Map cm : listMap) {
                        try {
                            int i = workMulitObjectServiceImpl.saveRecord(text, cm, client, baseUrl, token);
                            successNum += i;
                        } catch (Exception e) {
                            e.printStackTrace();
                            String dayinprintln = "【" + cm.get("objectName") + "】报错：" + e.getMessage();
                            msg += dayinprintln;
                        }
                    }
                } else {
                    Map map = listMap.get(0);
                    try {
                        int i = workMulitObjectServiceImpl.saveRecord(text, map, client, baseUrl, token);
                        successNum += i;
                    } catch (Exception e) {
                        e.printStackTrace();
                        String dayinprintln = "【" + map.get("objectName") + "】报错：" + e.getMessage();
                        msg += dayinprintln;
                    }
                }
            }
            if (successNum > 0) {
                apiResult.setFlag(true);
                apiResult.setResult(srTaskTreeData);
                apiResult.setMsg(msg);
            } else {
                apiResult.setFlag(false);
                apiResult.setResult(null);
                apiResult.setMsg(msg);
            }
        } else {
            apiResult.setFlag(false);
            apiResult.setResult(null);
            apiResult.setMsg("结构化数据不做处理！");
        }
        return apiResult;
    }
}
