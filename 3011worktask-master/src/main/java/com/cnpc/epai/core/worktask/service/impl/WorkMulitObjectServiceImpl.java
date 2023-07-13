package com.cnpc.epai.core.worktask.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.common.util.*;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.service.WorkNavigateTreeNodeService;
import com.cnpc.epai.core.workscene.service.WorkSceneTaskService;
import com.cnpc.epai.core.worktask.domain.*;
import com.cnpc.epai.core.worktask.mapper.SrTaskTreeDataMapper;
import com.cnpc.epai.core.worktask.repository.*;
import com.cnpc.epai.core.worktask.service.WorkMulitObjectService;
import com.cnpc.epai.core.worktask.util.AdjacentWellsReuseUtil;
import com.cnpc.epai.core.worktask.util.HeaderTool;
import com.cnpc.epai.core.worktask.util.RestPageImpl;
import com.cnpc.epai.core.worktask.util.ThreadLocalUtil;
import okhttp3.*;
import okhttp3.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cnpc.epai.core.workscene.commom.Constants.DSID;
import static java.util.stream.Collectors.*;

@Service
public class WorkMulitObjectServiceImpl implements WorkMulitObjectService, ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    SrTaskTreeDataRepository srTaskTreeDataRepo;
    @Autowired
    SrWorkCollectRepository srWorkCollectRepo;
    @Autowired
    SrWrokTaskRepository srWrokTaskRepository;
    @Autowired
    SrWorkMsgRepository srWorkMsgRepository;
    @Autowired
    SrWrokTaskRepository taskRepository;
    @Autowired
    SrTaskTreeDataMapper srTaskTreeDataMapper;

    @Autowired
    private WorkSceneTaskService workTaskService;

    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;

    @Autowired
    private WorkTaskFileUploadRepository workTaskFileUploadRepository;

    @Autowired
    private WorkTaskFileUploadRecordRepository workTaskFileUploadRecordRepository;

    @Value("${epai.crpDBSchemaID:ACTIJD100001852}")
    private String CRPDBSCHEMAID;

    // @Value("${epai.domainhost}")
//    private String ServerAddr = "www.crp.tlm.pecp.cloud";
    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Override
    public List<SrTaskTreeData> getOrderObject(String projectId, String objectId, String objectName, Integer distance,
                                               String workId, String datasetId, String datasetType,
                                               HttpServletRequest httpServletRequest) throws IOException {
        Map<String, Object> map = new HashMap<>();
        List<Map<String,Object>> ll = new ArrayList<>();
        List<String> objectNameList = new ArrayList<>();
        List<SrTaskTreeData> list = new ArrayList<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.notEqual(root.get("workId"),workId));
                if(objectName!=null && !objectName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("objectName"), "%"+objectName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("datasetType"),datasetType));
                predicates.add(criteriaBuilder.equal(root.get("datasetId"),datasetId));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        if (distance==null) {
            return list;
        }
        //先获取邻井信息
        if (objectId != null && !objectId.equals("")) {
            Map<String, Object> rtn = new HashMap<>();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("coordinate", objectName)
                    .add("distance", distance.toString())
                    .build();

            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/research/project/coordinate/findAdjacentWells?coordinate="+
                            objectId+"&distance="+distance+"&page=0&size=10000")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();

            map = JSONObject.parseObject(body.string());
            ll = (List<Map<String, Object>>) map.get("content");
            for (Map<String,Object> map1:ll){
                objectNameList.add((String) map1.get("WELL_COMMENT_NAME"));
            }
            System.out.println(objectNameList);
        }
        //获取数据集的文件信息
//        JSONObject rtn1 = new JSONObject();
//        serviceName = new StringBuffer();
//        serviceName.append(
//                "http://4103project/core/dataset/project/{projectId}/{datasetId}/searchdataindex?isHighest=false");
//        ref = new ParameterizedTypeReference<JSONObject>(){};
//        rtn = restTemplate.exchange(serviceName.toString(), HttpMethod.GET,new HttpEntity<>(""),ref,projectId,datasetId).getBody();
//        System.out.println(rtn);
        //有工作id对比工作id，再去工作内对比井信息

        if (list == null || list.size()==0){
            return list;
        }else {
            if (map != null && map.size()!=0){
                for (SrTaskTreeData srTaskTreeData:list){
                    if (!objectNameList.contains(srTaskTreeData.getObjectName())){
                        list.remove(srTaskTreeData);
                    }
                }

            }else {
                return list;
            }
        }



        return new ArrayList<>();
    }

    @Override
    public int saveTreeDataList(JSONObject object, HttpServletRequest httpServletRequest) throws Exception {
        List<SrTaskTreeData> list = new ArrayList<>();
        JSONArray jsonArray = JSONObject.parseArray(JSON.toJSONString(object.get("dataList")));
        if (jsonArray != null && jsonArray.size()>0){
            //0、查询人员权限：sr_work_task
            User currentUser = SpringManager.getCurrentUser();
            QueryWrapper<WorkTask> taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("work_id", object.get("workId").toString());
            taskWrapper.eq("charge_user_id", currentUser.getUserId());
            List<WorkTask> workTasks = workTaskService.list(taskWrapper);
            String taskId=null;
            String chargeUserId=null;
            String chargeUserName=null;
            if(workTasks.size()==0){
                taskId=null;
                chargeUserId=currentUser.getUserId();
                chargeUserName=currentUser.getDisplayName();
            }else{
                taskId=workTasks.get(0).getTaskId();
                chargeUserId=workTasks.get(0).getChargeUserId();
                chargeUserName=workTasks.get(0).getChargeUserName();
            }
            SrTaskTreeData DataEx = new SrTaskTreeData();
            DataEx.setId(ShortUUID.randomUUID());
            DataEx.setWorkId(object.get("workId").toString());
            DataEx.setObjectId(object.get("objectId").toString());
            DataEx.setObjectName(object.get("objectName").toString());
            DataEx.setDatasetId(object.get("datasetId").toString());
            DataEx.setDatasetName(object.get("datasetName").toString());
            DataEx.setNodeId(object.get("nodeId").toString());
            DataEx.setNodeNames(object.get("nodeName").toString());
            DataEx.setDataType(object.get("dataType").toString());
            DataEx.setSource(object.get("source").toString());
            DataEx.setDatasetType("data");
            DataEx.setDataTargetType("研究资料");
            DataEx.setDataStatus("待提交");
            DataEx.setFileName(object.get("objectName").toString()+"-"+object.get("datasetName").toString());
            DataEx.setCreateDate(new Date());
            DataEx.setUpdateDate(new Date());
            DataEx.setCreateUser(chargeUserId);
            DataEx.setCreateUserName(chargeUserName);
            DataEx.setUpdateUser(chargeUserId);
            DataEx.setTaskId(taskId);
            jsonArray=AdjacentWellsReuseUtil.formatTableData("",object,jsonArray);
            String projectId = CRPDBSCHEMAID;
            DataEx=saveSrTreeData2(projectId,jsonArray, DataEx,"研究资料",httpServletRequest);
            list.add(DataEx);
        }
        return jsonArray.size();
    }

    @Override
    public List<Map<String,Object>> callBackTool(String boName, List<String> ptList, String workId) {
        List<SrTaskTreeData> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.notEqual(root.get("workId"), workId));
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetName"));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                predicates.add(criteriaBuilder.equal(root.get("objectName"),boName));
                predicates.add(criteriaBuilder.isNotNull(root.get("dataContent")));
                predicates.add(criteriaBuilder.notEqual(root.get("dataContent"),"null"));
                for (String datasetName : ptList) {
                    in.value(datasetName);
                }
                predicates.add(in);
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        List<Map<String,Object>> ll = new ArrayList<>();
        for (SrTaskTreeData srTaskTreeData : list){
            if (srTaskTreeData.getDataContent()!=null) {
                ll.add((Map<String, Object>) srTaskTreeData.getDataContent());
            }
        }
        return ll;
    }

    @Override
    public Map<String,Object> getTreeDataList(String workId,String nodeId,String nodeNames,String datasetId,
                                              String objectId,String objectNames,HttpServletRequest httpServletRequest) throws IOException {
        Map<String,Object> result = new HashMap<>();
        List<String> nodeList = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(nodeId)) {
            nodeList = Arrays.asList(nodeId.split(","));
        }
        if(StringUtils.isNotBlank(datasetId)) {
            datasetIdList = Arrays.asList(datasetId.split(","));
        }
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> finalNodeList = nodeList;
        List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                if(finalNodeList !=null && finalNodeList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("nodeId"));
                    for (String node : finalNodeList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if(finalDatasetIdList !=null && finalDatasetIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String node : finalDatasetIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                predicates.add(criteriaBuilder.equal(root.get("datasetType"),"data"));
                predicates.add(criteriaBuilder.equal(root.get("dataType"),"结构化"));
                predicates.add(criteriaBuilder.equal(root.get("objectId"),objectId));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
//        if (list==null || list.size()==0){
//            spec = new Specification<SrTaskTreeData>() {
//                @Override
//                public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
//                    List<Predicate> predicates = new ArrayList<>();
//                    predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
//                    if(finalNodeList !=null && finalNodeList.size()>0){
//                        CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("nodeId"));
//                        for (String node : finalNodeList) {
//                            in.value(node);
//                        }
//                        predicates.add(in);
//                    }
//                    predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
//                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
//                }
//            };
//            ThreadContext.putContext("currentSchema", "epai_crpadmin");
//            list=srTaskTreeDataRepo.findAll(spec);
//        }
        Map<String, List<SrTaskTreeData>> groupBy = list.stream().collect(Collectors.groupingBy(SrTaskTreeData::getDatasetId));

        List<Map<String,Object>> res = new ArrayList<>();
        for (String key:datasetIdList){
            Map<String,Object> rtn = new HashMap<>();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/"+key+"?filterDisplay=true")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
            Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
            if (!groupBy.containsKey(key) || groupBy.get(key).size()==0){
                srTaskTreeData.setWorkId(workId);
                srTaskTreeData.setFirstChoice("C");
                srTaskTreeData.setSource("本地上传");
                srTaskTreeData.setDatasetId(key);
                srTaskTreeData.setDatasetType("data");
                srTaskTreeData.setDataType("结构化");
                srTaskTreeData.setDatasetName((String) dataMap.get("name"));
                srTaskTreeData.setNodeId(nodeId);
                srTaskTreeData.setNodeNames(nodeNames);
                srTaskTreeData.setObjectId(objectId);
                srTaskTreeData.setObjectName(objectNames);
                srTaskTreeData = srTaskTreeDataRepo.save(srTaskTreeData);
                dataMap.put("data",new ArrayList<>());
                dataMap.put("taskDataId",srTaskTreeData.getId());
                result.put(key,dataMap);
//                rtn.put("dataList",srTaskTreeData);
//                res.add(rtn);
            }else {
                List<SrTaskTreeData> val = new ArrayList<>();
                val = groupBy.get(key);
                SrTaskTreeData srTaskTreeDatas = val.stream().max(Comparator.comparing(SrTaskTreeData::getUpdateDate)).get();
                System.out.println(srTaskTreeDatas.toString());
                rtn.putAll(dataMap);
                if (srTaskTreeDatas.getDataContent()==null || srTaskTreeDatas.getDataContent().size()==0){
                    dataMap.put("data",new ArrayList<>());
                    dataMap.put("taskDataId",srTaskTreeDatas.getId());
                }else {
                    List<Map> list1 = new ArrayList<>();
                    List<Map> list2 = srTaskTreeDatas.getDataContent();
                    Map<String,List<Map>> mc = list2.stream().collect(Collectors.groupingBy(e -> e.get("SOURCE").toString()));
                    for (String kes:mc.keySet()){
                        Map map = new HashMap();
                        map.put("source",kes);
                        map.put("dataContent",mc.get(kes));
                        list1.add(map);
                    }
                    dataMap.put("data",list1);
                    dataMap.put("taskDataId",srTaskTreeDatas.getId());
                }
                result.put(key,dataMap);
            }

        }
        return result;
    }

    @Override
    public Map<String,Object> getObjectChoice(String projectId, String workId, String objectId, String nodeId,
                                              String datasetId, String status, String dataType,
                                              HttpServletRequest httpServletRequest) throws IOException {
        Map<String,Object> rtn = new HashMap<>();
        List<Map> res = new ArrayList<>();
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(datasetId)) {
            datasetIdList = Arrays.asList(datasetId.split(","));
        }
        List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                if(StringUtils.isNotBlank(nodeId)){
                    predicates.add(criteriaBuilder.equal(root.get("nodeId"),nodeId));
                }
                if(StringUtils.isNotBlank(objectId)){
                    predicates.add(criteriaBuilder.equal(root.get("objectId"),objectId));
                }
                if(finalDatasetIdList !=null && finalDatasetIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String node : finalDatasetIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if (status.equals("A")){
                    predicates.add(criteriaBuilder.equal(root.get("firstChoice"),status));
                }else if (status.equals("B")){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("firstChoice"));
                    in.value("A");
                    in.value("B");
                    predicates.add(in);
                }
                predicates.add(criteriaBuilder.equal(root.get("datasetType"),"file"));
                if(StringUtils.isNotBlank(dataType)){
                    predicates.add(criteriaBuilder.equal(root.get("dataType"), dataType));
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        List<String> datasetIdlist = list.stream().map(SrTaskTreeData::getDatasetId).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
        if (datasetIdlist != null && datasetIdlist.size()==1){

        }else {
            return null;
        }
        if (dataType.equals("结构化")) {
            for (SrTaskTreeData srTaskTreeData : list) {
                List<Map> contentMap = srTaskTreeData.getDataContent();
                StringBuffer DSID = new StringBuffer();
                DSID.append("DSID =");
                for (Map map1:contentMap){
                    DSID.append(" "+map1.get("DSID"));
                }
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
                MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = new FormBody.Builder().build();
                Request request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/project/" + projectId + "/" + srTaskTreeData.getDatasetId()
                                + "/searchdatawithoutindex?param[]="+DSID.toString()+"&page=0&size=100000")
                        .build();
                Call callTask = client.newCall(request);
                Response response = callTask.execute();
                ResponseBody body = response.body();
                String s = body.string();
                Map<String, Object> map = JSONObject.parseObject(s);
                if (map.get("content") == null || map.get("content") == new ArrayList<>()) {

                } else {
                    List<Map> jian = JSON.parseArray(JSON.toJSONString(map.get("content")), Map.class);
                    rtn.putAll(map);
                    res.addAll(jian);
                }
            }
            rtn.put("content", res);
        }else if (dataType.equals("非结构化")){
            rtn.put("content", list);
        }
        return rtn;
    }

    @Override
    public SrTaskTreeData saveSrTreeDataList(SrTaskTreeData srTaskTreeData) {
        SrTaskTreeData sr = srTaskTreeDataRepo.save(srTaskTreeData);
        return sr;
    }

    @Override
    public ArrayList<SrTaskTreeData>  saveResultList(List<SrTaskTreeData> ResultList) {
        ArrayList<SrTaskTreeData> backList = new ArrayList<>();
        for (SrTaskTreeData srTaskTreeData : ResultList) {
            SrTaskTreeData srTaskTreeDataback = saveSrTreeDataList(srTaskTreeData);
            if(srTaskTreeDataback!=null){
                backList.add(srTaskTreeDataback);
            }else {
                System.out.println("成果列表内的行对象  id=" + srTaskTreeData.getId()+"名称="+srTaskTreeData.getFileName()+"保存失败！");
            }
        }
        if(ResultList.size()==backList.size()){
            return  backList;

        }else{
            return null;
        }
    }

    @Override
    public boolean deleteSrTreeDataList(String projectId,List<Map> data, HttpServletRequest httpServletRequest) throws IOException {
        List<String> list = new ArrayList<>();
        Map<String, List<Map>> map = new HashMap();
        map = data.stream().collect(Collectors.groupingBy(e -> e.get("id").toString()));
        for (String id:map.keySet()){
            List<Map> content = map.get(id);
            List<String> dsids = content.stream().map(o->(String)o.get("DSID")).collect(Collectors.toList());
            SrTaskTreeData srTaskTreeData = srTaskTreeDataRepo.findOne(id);
            List<Map> contentMap = srTaskTreeData.getDataContent();
            contentMap.removeIf(v->dsids.contains(v.get("DSID")));
            srTaskTreeData.setDataContent(contentMap);
            srTaskTreeDataRepo.save(srTaskTreeData);
        }

        return true;
    }
    @Override
    public boolean updatedatalistEx(String workTreeDataId,List<Map> data,String datasetId,HttpServletRequest httpServletRequest) throws IOException {

        SrTaskTreeDataEx srTaskTreeDataEx = srTaskTreeDataMapper.selectById(workTreeDataId);

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder().get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/dataset/"+datasetId+"?filterDisplay=true")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
        for(Map lm : data){
//            if(!lm.containsKey("SOURCE")){
//                lm.put("SOURCE",srTaskTreeData.getSource());
//            }
            //if (lm.containsKey("SOURCE")){
            if (!lm.containsKey("DSID") || lm.get("DSID")==null ){
                lm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+UUID.randomUUID().toString().replaceAll("-", ""));
            }
            if (!lm.containsKey("BSFLAG") || lm.get("BSFLAG")==null){
                lm.put("BSFLAG","1");
            }
            if (!lm.containsKey("DATA_REGION") || lm.get("DATA_REGION")==null){
                lm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
            }
            if (!lm.containsKey("DATA_GROUP") || lm.get("DATA_GROUP")==null){
                if (ServerAddr.equals("www.dev.pcep.cloud")) {
                    lm.put("DATA_GROUP", "ACTIJD100001852");
                }else {
                    lm.put("DATA_GROUP", "APPSTLgcjs12345");
                }
            }
            if (!lm.containsKey("CREATE_USER_ID") || lm.get("CREATE_USER_ID")==null){
                lm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
            }
            if (!lm.containsKey("CREATE_DATE") || lm.get("CREATE_DATE")==null){
                lm.put("CREATE_DATE",sdf.format(new Date()));
            }
            if (!lm.containsKey("CREATE_APP_ID") || lm.get("CREATE_APP_ID")==null){
                lm.put("CREATE_APP_ID","TLM_CRP");
            }
            if (!lm.containsKey("UPDATE_USER_ID") || lm.get("UPDATE_USER_ID")==null){
                lm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
            }
            if (!lm.containsKey("UPDATE_DATE") || lm.get("UPDATE_DATE")==null){
                lm.put("UPDATE_DATE",sdf.format(new Date()));
            }
            lm.put("taskDataId",srTaskTreeDataEx.getId());
        }
        srTaskTreeDataEx.setDataContent(data);

        int i = srTaskTreeDataMapper.updateById(srTaskTreeDataEx);

        return i>0;
    }
    @Override
    public boolean deleteSrTreeDataListRow(String id,List<Map> data,HttpServletRequest httpServletRequest) throws IOException {
        //data.stream().map()
        List<String> list = data.stream().map(m -> (String) m.get("id")).collect(toList());
        int i = srTaskTreeDataMapper.deleteBatchIds(list);

        return i>0;
    }
    @Override
    public boolean deleteSrTreeDataListEx(String id,List<Map> data, HttpServletRequest httpServletRequest) throws IOException {
        SrTaskTreeDataEx srTaskTreeDataEx = srTaskTreeDataMapper.selectById(id);
        PGobject dataContent = (PGobject)srTaskTreeDataEx.getDataContent();

        List<JSONObject> jsonArray = JSON.parseArray(dataContent.getValue(), JSONObject.class);
        List<JSONObject> newContent = new ArrayList<>();
        a:for(JSONObject jsonObject : jsonArray){
            String dsid = jsonObject.getString("DSID");
            boolean exist = false;
            for(Map map : data){
                if(StringUtils.equals(map.get("DSID").toString(),dsid)){
                    exist = true;
                    break ;
                }
            }
            if(!exist){
                newContent.add(jsonObject);
            }
        }
        srTaskTreeDataEx.setDataContent(newContent);
        srTaskTreeDataMapper.updateById(srTaskTreeDataEx);

        return true;
    }

    @Override
    public boolean deleteObjectList(String projectId,List<String> dataList,HttpServletRequest httpServletRequest) throws IOException {
        //boolean x = srTaskTreeDataRepo.deleteByIdIn(dataList);
        int i = srTaskTreeDataMapper.deleteBatchIds(dataList);
        return i>0;
    }

    @Override
    public Map<String, Object> getNodeResourceList(String nodeIds, String resType,HttpServletRequest httpServletRequest) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        RequestBody requestBody = new FormBody.Builder().add("nodeIds", nodeIds).build();

        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/objdataset/navigate/findnode")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        Map<String,Object> map1 = JSONObject.parseObject(body.string());
        List<Map<String,Object>> list = new ArrayList<>();
        list = (List<Map<String, Object>>) map1.get("result");
        if (list ==null || list.size()==0){
            return null;
        }
        String sourceid = "";
        Map<String, Object> source = new HashMap<>();
        for (Map<String,Object> map :list){
            source.put((String) map.get("sourceNodeId"),map.get("nodeId"));
            if (list.indexOf(map)<list.size()-1){
                sourceid = sourceid+map.get("sourceNodeId")+",";
            }else {
                sourceid = sourceid+map.get("sourceNodeId");
            }
        }
        requestBody = new FormBody.Builder().add("nodeIds", sourceid).build();

        request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/objdataset/navigate/findnode")
                .build();
        callTask = client.newCall(request);
        response = callTask.execute();
        body = response.body();
        map1 = JSONObject.parseObject(body.string());
        list = new ArrayList<>();
        list = (List<Map<String, Object>>) map1.get("result");
        sourceid = "";
        for (Map<String,Object> map :list){
            source.put((String) map.get("sourceNodeId"),source.get(map.get("nodeId")));
            if (list.indexOf(map)<list.size()-1){
                sourceid = sourceid+map.get("sourceNodeId")+",";
            }else {
                sourceid = sourceid+map.get("sourceNodeId");
            }
        }
        requestBody = new FormBody.Builder().add("treeNodeIds", sourceid).add("resTypes",resType).build();
        request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/objdataset/minBizUnit/getByTreeNodeIds")
                .build();
        callTask = client.newCall(request);
        response = callTask.execute();
        body = response.body();
        map1 = JSONObject.parseObject(body.string());
        list = new ArrayList<>();
        list = (List<Map<String, Object>>) map1.get("result");
        if (list ==null || list.size()==0){
            return null;
        }
        Map<String,List<Map<String,Object>>> mm = list.stream().collect(Collectors.groupingBy(e->e.get("treeNodeId").toString()));
        map1.put("result",mm);
        List<Map<String,List<Map<String,Object>>>> rtn = new ArrayList<>();
        Map<String,List<Map<String,Object>>> node = new HashMap<>();
        for (String key:mm.keySet()){
            List<Map<String,Object>> list1 = new ArrayList<>();
            list1 = mm.get(key);
            Map<String,List<Map<String,Object>>> type = list1.stream().collect(Collectors.groupingBy(e->e.get("resType").toString()));
            mm.put(key,new ArrayList<>());
            for (String k:type.keySet()){
                Map<String,Object> res = new HashMap<>();
                res.put("resType",k);
                res.put("resInfo",type.get(k));
                mm.get(key).add(res);
            }
            node.put((String) source.get(key),mm.get(key));
        }
        rtn.add(node);
        map1.put("result",rtn);
        return map1;
    }

    @Override
    public SrTaskTreeData getSrDataById(String dataId) {
        return srTaskTreeDataRepo.findOne(dataId);
    }

    @Override
    public List<SrTaskTreeData> saveDataContent(String projectId, SrTaskTreeData srTaskTreeData, List<Map> list,
                                                HttpServletRequest httpServletRequest) throws IOException {
        List<String> wellIds = list.stream().map(o->(String)o.get("WELL_ID")).collect(Collectors.toList());
        List<SrTaskTreeData> srlist = new ArrayList<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), srTaskTreeData.getWorkId()));
                predicates.add(criteriaBuilder.equal(root.get("datasetId"), srTaskTreeData.getDatasetId()));
                if (wellIds !=null && wellIds.size()!=0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("objectId"));
                    for (String data : wellIds) {
                        in.value(data);
                    }
                    predicates.add(in);
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        srlist=srTaskTreeDataRepo.findAll(spec);
        List<Map> contentMap = new ArrayList<>();
        for (SrTaskTreeData ss:srlist){
            contentMap.addAll(ss.getDataContent());
        }
        List<String> dsids = new ArrayList<>();
        dsids = contentMap.stream().map(o->(String)o.get("DSID")).collect(Collectors.toList());
        List<String> finalDsids = dsids;
        srTaskTreeData.getDataContent().removeIf(c-> finalDsids.contains(c.get("DSID")));
        if (srTaskTreeData.getDataContent()==null || srTaskTreeData.getDataContent().size()==0){
            return new ArrayList<>();
        }
        StringBuffer serviceName = new StringBuffer();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
        Request request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String,List<Map>> groupMap = reviewDataGroup(srTaskTreeData.getObjectId(),srTaskTreeData.getObjectName(),list);
        List<SrTaskTreeData> srTaskTreeDataList = new ArrayList<>();
        for (String key:groupMap.keySet()) {
            SrTaskTreeData newSr = new SrTaskTreeData();
            newSr = newSrData(srTaskTreeData);
            newSr.setId(ShortUUID.randomUUID());
            newSr.setObjectId((String) groupMap.get(key).get(0).get("OBJECT_ID"));
            newSr.setObjectName((String) groupMap.get(key).get(0).get("OBJECT_NAME"));
            for (Map lm:groupMap.get(key)){
                if (lm.containsKey("SOURCE")){
                    if (!lm.containsKey("DSID") || lm.get("DSID") == null){
                        lm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+
                                UUID.randomUUID().toString().replaceAll("-", ""));
                    }
                    if (!lm.containsKey("BSFLAG") || lm.get("BSFLAG")==null){
                        lm.put("BSFLAG","1");
                    }
                    if (!lm.containsKey("DATA_REGION") || lm.get("DATA_REGION")==null){
                        lm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                    }
                    if (!lm.containsKey("DATA_GROUP") || lm.get("DATA_GROUP")==null){
                        if (ServerAddr.equals("www.dev.pcep.cloud")) {
                            lm.put("DATA_GROUP", "ACTIJD100001852");
                        }else {
                            lm.put("DATA_GROUP", "APPSTLgcjs12345");
                        }
                    }
                    if (!lm.containsKey("CREATE_USER_ID") || lm.get("CREATE_USER_ID")==null){
                        lm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!lm.containsKey("CREATE_DATE") || lm.get("CREATE_DATE")==null){
                        lm.put("CREATE_DATE",sdf.format(new Date()));
                    }
                    if (!lm.containsKey("CREATE_APP_ID") || lm.get("CREATE_APP_ID")==null){
                        lm.put("CREATE_APP_ID","TLM_CRP");
                    }
                    if (!lm.containsKey("UPDATE_USER_ID") || lm.get("UPDATE_USER_ID")==null){
                        lm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!lm.containsKey("UPDATE_DATE") || lm.get("UPDATE_DATE")==null){
                        lm.put("UPDATE_DATE",sdf.format(new Date()));
                    }
                    lm.put("taskDataId",newSr.getId());
                }else {
                    return null;
                }
            }
            String ss = JSON.toJSONString(groupMap.get(key));

            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, ss);
            request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/project/" + projectId + "/" + newSr.getDatasetId() + "/savedatafullresult")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            s = body.string();
            Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            if ((boolean)saveMap.get("flag")){
                newSr.setDataContent(groupMap.get(key));
                srTaskTreeDataList.add(newSr);
            }else {
                return null;
            }
        }
        List<SrTaskTreeData> srTaskTreeDataList1 =  srTaskTreeDataRepo.save(srTaskTreeDataList);
        return srTaskTreeDataList1;
    }

    @Override
    public boolean updateFirstChoice(String id,String firstChoice) {
        List<SrTaskTreeData> list = new ArrayList<>();
        SrTaskTreeData srTaskTreeData = srTaskTreeDataRepo.findOne(id);
        if (firstChoice.equals("A")){
            Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
                @Override
                public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.notEqual(root.get("id"),id));
                    predicates.add(criteriaBuilder.equal(root.get("workId"), srTaskTreeData.getWorkId()));
                    predicates.add(criteriaBuilder.equal(root.get("nodeId"), srTaskTreeData.getNodeId()));
                    predicates.add(criteriaBuilder.equal(root.get("datasetId"), srTaskTreeData.getDatasetId()));
                    Predicate[] predicateArray = new Predicate[predicates.size()];
                    criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                    criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            ThreadContext.putContext("currentSchema", "epai_crpadmin");
            list=srTaskTreeDataRepo.findAll(spec);
            for (SrTaskTreeData srTaskTreeData1:list){
                srTaskTreeData1.setFirstChoice("C");
            }
            srTaskTreeData.setFirstChoice(firstChoice);
            list.add(srTaskTreeData);
            srTaskTreeDataRepo.save(list);
            return true;
        }else if (firstChoice.equals("B")){
            srTaskTreeData.setFirstChoice(firstChoice);
            srTaskTreeDataRepo.save(list);
            return true;
        }
        return false;
    }

    @Override
    public List<SrTaskTreeData> getAllObjectBy(String workId, String nodeId, String datasetId) {
        List<SrTaskTreeData> list = new ArrayList<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                predicates.add(criteriaBuilder.equal(root.get("datasetId"), datasetId));
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        return list;
    }

    @Override
    public boolean submitObjectData(String workId, String nodeId,String dataId, String datasetIds) {
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(datasetIds)) {
            datasetIdList = Arrays.asList(datasetIds.split(","));
        }
        List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotBlank(dataId)){
                    predicates.add(criteriaBuilder.equal(root.get("id"), dataId));
                }else {
                    predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                    predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                    if(finalDatasetIdList !=null && finalDatasetIdList.size()>0){
                        CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                        for (String user : finalDatasetIdList) {
                            in.value(user);
                        }
                        predicates.add(in);
                    }
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("firstChoice"));
                    in.value("A");
                    in.value("B");
                    predicates.add(in);
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        for (SrTaskTreeData ss:list){
            ss.setDataStatus("待审核");
        }
        srTaskTreeDataRepo.save(list);
        return true;
    }

    private SrTaskTreeData newSrData(SrTaskTreeData srTaskTreeData){
        SrTaskTreeData newSr = new SrTaskTreeData();
        newSr.setDatasetType(srTaskTreeData.getDatasetType());
        newSr.setDataType(srTaskTreeData.getDataType());
        newSr.setWorkId(srTaskTreeData.getWorkId());
        newSr.setDatasetId(srTaskTreeData.getDatasetId());
        newSr.setDatasetName(srTaskTreeData.getDatasetName());
        newSr.setNodeId(srTaskTreeData.getNodeId());
        newSr.setNodeNames(srTaskTreeData.getNodeNames());
        newSr.setSource(srTaskTreeData.getSource());
        newSr.setTaskId(srTaskTreeData.getTaskId());
        return newSr;
    }
    @Override
    public List<SrTaskTreeData> importDataListEx(String projectId,SrTaskTreeData srTaskTreeData,List<Map> dataList,
                                                 HttpServletRequest httpServletRequest) throws Exception {
        //分组
        Map<String, List<Map>> objectMap = dataList.stream().collect(groupingBy(m -> (String) m.get("objectId")));

        StringBuffer serviceName = new StringBuffer();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder().get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);

        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
        List<SrTaskTreeData> srTaskTreeDataList = new ArrayList<>();
        //Map<String,List<Map>> groupMap = reviewDataGroup(srTaskTreeData.getObjectId(),srTaskTreeData.getObjectName(),srTaskTreeData.getDataContent());
        for (String key:objectMap.keySet()){
            SrTaskTreeData newSr = JSON.parseObject(JSON.toJSONString(srTaskTreeData),SrTaskTreeData.class);
            //newSr = newSrData(srTaskTreeData);
            newSr.setId(ShortUUID.randomUUID());
            newSr.setObjectId((String) objectMap.get(key).get(0).get("objectId"));
            newSr.setObjectName((String) objectMap.get(key).get(0).get("objectName"));
            for (Map lm:objectMap.get(key)){
                if(!lm.containsKey("SOURCE")){
                    lm.put("SOURCE",srTaskTreeData.getSource());
                }
                //if (lm.containsKey("SOURCE")){
                //if (!lm.containsKey("DSID") || lm.get("DSID")==null ){
                lm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+UUID.randomUUID().toString().replaceAll("-", ""));
                //}
                if (!lm.containsKey("BSFLAG") || lm.get("BSFLAG")==null){
                    lm.put("BSFLAG","1");
                }
                if (!lm.containsKey("DATA_REGION") || lm.get("DATA_REGION")==null){
                    lm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                }
                if (!lm.containsKey("DATA_GROUP") || lm.get("DATA_GROUP")==null){
                    if (ServerAddr.equals("www.dev.pcep.cloud")) {
                        lm.put("DATA_GROUP", "ACTIJD100001852");
                    }else {
                        lm.put("DATA_GROUP", "APPSTLgcjs12345");
                    }
                }
                if (!lm.containsKey("CREATE_USER_ID") || lm.get("CREATE_USER_ID")==null){
                    lm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                }
                if (!lm.containsKey("CREATE_DATE") || lm.get("CREATE_DATE")==null){
                    lm.put("CREATE_DATE",sdf.format(new Date()));
                }
                if (!lm.containsKey("CREATE_APP_ID") || lm.get("CREATE_APP_ID")==null){
                    lm.put("CREATE_APP_ID","TLM_CRP");
                }
                if (!lm.containsKey("UPDATE_USER_ID") || lm.get("UPDATE_USER_ID")==null){
                    lm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                }
                if (!lm.containsKey("UPDATE_DATE") || lm.get("UPDATE_DATE")==null){
                    lm.put("UPDATE_DATE",sdf.format(new Date()));
                }
                lm.put("taskDataId",newSr.getId());
//                }else {
//                    return null;
//                }
            }

            String ss = JSON.toJSONString(objectMap.get(key));
            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, ss);
            request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+newSr.getDatasetId()+"/savedatafullresult")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            s= body.string();
            Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            if ((boolean)saveMap.get("flag")){
                newSr.setDataContent(objectMap.get(key));
                newSr.setFileName(newSr.getObjectName()+"-"+newSr.getDatasetName());
                srTaskTreeDataList.add(newSr);
            }else {
                throw new Exception(saveMap.get("result")+""); //抛异常
            }
        }
        List<SrTaskTreeData> srTaskTreeDataList1 = srTaskTreeDataRepo.save(srTaskTreeDataList);
        return srTaskTreeDataList1;
    }
    @Override
    public List<SrTaskTreeData> importDataList(String projectId,SrTaskTreeData srTaskTreeData,HttpServletRequest httpServletRequest) throws IOException {
        //多对象导入及保存做对象类型判断
        List<String> wellIds = srTaskTreeData.getDataContent().stream().map(o->(String)o.get("WELL_ID")).collect(Collectors.toList());
        List<SrTaskTreeData> list = new ArrayList<>();
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",srTaskTreeData.getWorkId());
        queryWrapper.eq("dataset_id",srTaskTreeData.getDatasetId());
        queryWrapper.eq("bsflag","N");
        List<SrTaskTreeDataEx> srTaskTreeDataExes = srTaskTreeDataMapper.selectList(queryWrapper);
        list = JSON.parseArray(JSON.toJSONString(srTaskTreeDataExes),SrTaskTreeData.class);
        list.forEach(item->{
            srTaskTreeDataExes.forEach(sitem->{
                if(StringUtils.equals(sitem.getId(),item.getId())){
                    if(sitem.getDataContent()!=null){
                        item.setDataContent(JSONObject.parseArray(((PGobject)sitem.getDataContent()).getValue(),Map.class));
                    }
                }
            });
        });
        /*Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), srTaskTreeData.getWorkId()));
                predicates.add(criteriaBuilder.equal(root.get("datasetId"), srTaskTreeData.getDatasetId()));
                if (wellIds !=null && wellIds.size()!=0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("objectId"));
                    for (String data : wellIds) {
                        in.value(data);
                    }
                    predicates.add(in);
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);*/
        List<Map> contentMap = new ArrayList<>();
        for (SrTaskTreeData ss:list){
            contentMap.addAll(ss.getDataContent());
        }
        List<String> dsids = contentMap.stream().map(o->(String)o.get("DSID")).collect(Collectors.toList());
        List<String> finalDsids = dsids;
        srTaskTreeData.getDataContent().removeIf(c-> finalDsids.contains(c.get("DSID")));
        if (srTaskTreeData.getDataContent()==null || srTaskTreeData.getDataContent().size()==0){
            return new ArrayList<>();
        }
        StringBuffer serviceName = new StringBuffer();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
        Request request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
        List<SrTaskTreeData> srTaskTreeDataList = new ArrayList<>();
        Map<String,List<Map>> groupMap = reviewDataGroup(srTaskTreeData.getObjectId(),srTaskTreeData.getObjectName(),srTaskTreeData.getDataContent());
        for (String key:groupMap.keySet()){
            SrTaskTreeData newSr = new SrTaskTreeData();
            newSr = newSrData(srTaskTreeData);
            newSr.setId(ShortUUID.randomUUID());
            newSr.setObjectId((String) groupMap.get(key).get(0).get("OBJECT_ID"));
            newSr.setObjectName((String) groupMap.get(key).get(0).get("OBJECT_NAME"));
            for (Map lm:groupMap.get(key)){
                if (lm.containsKey("SOURCE")){
                    if (!lm.containsKey("DSID") || lm.get("DSID")==null ){
                        lm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+
                                UUID.randomUUID().toString().replaceAll("-", ""));
                    }
                    if (!lm.containsKey("BSFLAG") || lm.get("BSFLAG")==null){
                        lm.put("BSFLAG","1");
                    }
                    if (!lm.containsKey("DATA_REGION") || lm.get("DATA_REGION")==null){
                        lm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                    }
                    if (!lm.containsKey("DATA_GROUP") || lm.get("DATA_GROUP")==null){
                        if (ServerAddr.equals("www.dev.pcep.cloud")) {
                            lm.put("DATA_GROUP", "ACTIJD100001852");
                        }else {
                            lm.put("DATA_GROUP", "APPSTLgcjs12345");
                        }
                    }
                    if (!lm.containsKey("CREATE_USER_ID") || lm.get("CREATE_USER_ID")==null){
                        lm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!lm.containsKey("CREATE_DATE") || lm.get("CREATE_DATE")==null){
                        lm.put("CREATE_DATE",sdf.format(new Date()));
                    }
                    if (!lm.containsKey("CREATE_APP_ID") || lm.get("CREATE_APP_ID")==null){
                        lm.put("CREATE_APP_ID","TLM_CRP");
                    }
                    if (!lm.containsKey("UPDATE_USER_ID") || lm.get("UPDATE_USER_ID")==null){
                        lm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!lm.containsKey("UPDATE_DATE") || lm.get("UPDATE_DATE")==null){
                        lm.put("UPDATE_DATE",sdf.format(new Date()));
                    }
                    lm.put("taskDataId",newSr.getId());
                }else {
                    return null;
                }
            }

            String ss = JSON.toJSONString(groupMap.get(key));
            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, ss);
            request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+newSr.getDatasetId()+"/savedatafullresult")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            s= body.string();
            Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            if ((boolean)saveMap.get("flag")){
                newSr.setDataContent(groupMap.get(key));
                newSr.setFileName(newSr.getObjectName()+"-"+newSr.getDatasetName());
                srTaskTreeDataList.add(newSr);
            }else {
                return null;
            }
        }
        List<SrTaskTreeData> srTaskTreeDataList1 = srTaskTreeDataRepo.save(srTaskTreeDataList);
        return srTaskTreeDataList1;
    }
    public Map<String,List<SrTaskTreeDataEx>> getNewObjectDataRecord(String projectId,String workId,String objectId,List<Map> data,
                                                     HttpServletRequest httpServletRequest) throws IOException {
        List<String> datasetIdList = new ArrayList<>();

        if (data!=null && data.size()!=0){
            for (Map mm:data){
                datasetIdList.add(String.valueOf(mm.get("datasetId")));
            }
        }
        String nodeId = httpServletRequest.getParameter("nodeId");//节点ID
        String nodeName = httpServletRequest.getParameter("nodeName");//节点ID
        List<String> objectIds = Arrays.asList(objectId.split(","));
        List<String> finalDatasetIdList = datasetIdList;
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",workId);
        //第一种：and ( ... or ... or...) 格式"已审核,已完成,已归档"//????测试用，后面取消注释
        queryWrapper.and(wrapper -> wrapper.eq("DATA_STATUS", "已审核").or(wrapper2 -> wrapper2.eq("NODE_ID",nodeId)));
        queryWrapper.in("object_id",objectIds);
        if(!datasetIdList.isEmpty()){
            queryWrapper.in("dataset_id",datasetIdList);
        }
        queryWrapper.eq("bsflag","N");
        queryWrapper.orderByDesc("object_id","create_date");
        //queryWrapper.orderByAsc("create_date");
        List<SrTaskTreeDataEx> srTaskTreeDataExes = srTaskTreeDataMapper.selectList(queryWrapper);
        /*for(SrTaskTreeDataEx ex : srTaskTreeDataExes){
            if(ex.getDataContent() != null){
                PGobject pgo = (PGobject)ex.getDataContent();
                List<Map> maps = JSON.parseArray(pgo.getValue(), Map.class);
                ex.setDataContent(maps);
                ex.setDataContent(null);
            }

        }*/
        //list = JSON.parseArray(JSON.toJSONString(srTaskTreeDataExes),SrTaskTreeData.class);
        Map<String, List<SrTaskTreeDataEx>> groupBy = srTaskTreeDataExes.stream().collect(
                groupingBy(SrTaskTreeDataEx::getDatasetId,LinkedHashMap::new,Collectors.toList()));
        Map<String, Object> ret = new HashMap<>();
        for(String key : groupBy.keySet()){
            List<SrTaskTreeDataEx> vlist = groupBy.get(key);
            //List<Object> rlist = new ArrayList<>();
            for(SrTaskTreeDataEx sttd : vlist){

                //SrTaskTreeData srTaskTreeData = JSON.parseObject(JSON.toJSONString(sttd),SrTaskTreeData.class);
                //srTaskTreeData.setCreateUserName(srTaskTreeData.getUserDisplayName());
                sttd.setCreateUserName(sttd.getUserDisplayName());
                String Content = sttd.getDataContent().toString();
                if (sttd.getDataContent()!=null && !Content.equals("") && !Content.equals("[]") && !Content.equals("[{}]")) {
                    JSONArray jSONArray = JSONArray.parseArray(Content);
                    sttd.setDataContent(jSONArray);
                }else {
                    sttd.setDataContent(null);
                }
                //jsonObject.put("code",dataMap.get("code"));
                //rlist.add(srTaskTreeData);
            }
            //rlist = rlist.stream().sorted(Comparator.comparing(o->((SrTaskTreeData)o).getObjectId())).collect(toList());
            //ret.put(key,rlist);
        }
        return groupBy;
    }

    public List<SrTaskTreeData> getNewObjectDataRecordDetails(String projectId, List<String> idList) throws IOException{
        List<SrTaskTreeData> retMap = new ArrayList<>();
        if(idList == null || idList.isEmpty()){
            return retMap;
        }
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("work_tree_data_id",idList);
        List<SrTaskTreeDataEx> srTaskTreeDataExes = srTaskTreeDataMapper.selectList(queryWrapper);
        List<SrTaskTreeData> list = JSON.parseArray(JSON.toJSONString(srTaskTreeDataExes),SrTaskTreeData.class);
        for(SrTaskTreeDataEx ex : srTaskTreeDataExes){
            if(StrUtil.equals(ex.getDatasetId(),Constants.WELL_MASTER_DATA_SET_ID) &&
                    ThreadLocalUtil.get(Constants.SEND_ASI_STATUS) !=null &&
                    StrUtil.equals((String) ThreadLocalUtil.get(Constants.SEND_ASI_STATUS),Constants.SEND_ASI_BEGIN)){
                ArrayList<String> sendWorkTreeDataIds = (ArrayList<String>) ThreadLocalUtil.get(Constants.SEND_WORK_TREE_DATA_IDS);
                sendWorkTreeDataIds.remove(ex.getId());
                ThreadLocalUtil.set(Constants.WELL_MASTER_DATA_ID,ex.getId());
            }
            if(ex.getDataContent() != null){
                PGobject pgo = (PGobject)ex.getDataContent();

                List<Map> dataContent = JSON.parseArray(pgo.getValue(), Map.class);
                for(SrTaskTreeData data : list){
                    if(StringUtils.equals(ex.getId(),data.getId())){
                        data.setDataContent(dataContent);
                        break;
                    }
                }
            }

        }
        OkHttpClient client = null;
        Request request = null;
        Call callTask = null;
        Response response = null;
        ResponseBody body = null;
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        for(SrTaskTreeData srTaskTreeData : list){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DSID =");
            List<Map> dataContent = srTaskTreeData.getDataContent();
            if(dataContent!=null){
                dataContent.forEach(map->stringBuilder.append(" ").append(map.get("DSID")));
            }

            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody requestBody = RequestBody.create(JSONs, "ss");
            request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization", "Bearer " + TokenUtil.getToken())
                    .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()
                            +"/searchdatawithoutindex?param[]="+DSID.toString()+"&page=0&size=100000")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            String ss= body.string();
            Map<String,Object> map = JSONObject.parseObject(ss);
            List<Map> jian = JSON.parseArray(JSON.toJSONString(map.get("content")),Map.class);
            if (jian != null && jian.size()>0){
                for (Map map1:jian){
                    map1.put("SOURCE",srTaskTreeData.getSource());
                    if (!map1.containsKey("taskDataId") || map1.get("taskDataId")==null || map1.get("taskDataId").equals("")){
                        map1.put("taskDataId",srTaskTreeData.getId());
                    }
                }
                srTaskTreeData.setDataContent(jian);
                srTaskTreeData.setCreateUserName(srTaskTreeData.getUserDisplayName());
            }
        }

        System.out.println();
        return list;
    }
    public Object getNewTreeDataListEx(String projectId,String workId,String objectId,List<Map> data,
                                       HttpServletRequest httpServletRequest) throws IOException {
        List<String> datasetIdList = new ArrayList<>();
        Map res = new HashMap();
        if (data!=null && data.size()!=0){
            for (Map mm:data){
                datasetIdList.add(String.valueOf(mm.get("datasetId")));
            }
        }
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> finalDatasetIdList = datasetIdList;
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",workId);
        queryWrapper.eq("object_id",objectId);
        if(!datasetIdList.isEmpty()){
            queryWrapper.in("dataset_id",datasetIdList);
        }
        queryWrapper.eq("bsflag","N");
        queryWrapper.orderByAsc("create_date");
        List<SrTaskTreeDataEx> srTaskTreeDataExes = srTaskTreeDataMapper.selectList(queryWrapper);
        for(SrTaskTreeDataEx ex : srTaskTreeDataExes){
            if(ex.getDataContent() != null){
                PGobject pgo = (PGobject)ex.getDataContent();
                System.out.println(pgo.getValue());
                List<Map> maps = JSON.parseArray(pgo.getValue(), Map.class);
                ex.setDataContent(maps);
            }

        }
        list = JSON.parseArray(JSON.toJSONString(srTaskTreeDataExes),SrTaskTreeData.class);
        Map<String, List<SrTaskTreeData>> groupBy = list.stream().collect(groupingBy(SrTaskTreeData::getId));
        list = list.stream().sorted(Comparator.comparing(SrTaskTreeData::getUpdateDate)).collect(toList());
        //分批次获取数据
        OkHttpClient client = null;
        Request request = null;
        Call callTask = null;
        Response response = null;
        ResponseBody body = null;
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        for(SrTaskTreeData srTaskTreeData : list){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DSID =");
            List<Map> dataContent = srTaskTreeData.getDataContent();
            if(dataContent!=null){
                dataContent.forEach(map->stringBuilder.append(" ").append(map.get("DSID")));
            }

            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody requestBody = RequestBody.create(JSONs, "ss");
            request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()
                            +"/searchdatawithoutindex?param[]="+DSID.toString()+"&page=0&size=100000")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            String ss= body.string();
            Map<String,Object> map = JSONObject.parseObject(ss);
            List<Map> jian = JSON.parseArray(JSON.toJSONString(map.get("content")),Map.class);
            if (jian != null && jian.size()>0){
                for (Map map1:jian){
                    map1.put("SOURCE",srTaskTreeData.getSource());
                    if (!map1.containsKey("taskDataId") || map1.get("taskDataId")==null || map1.get("taskDataId").equals("")){
                        map1.put("taskDataId",srTaskTreeData.getId());
                    }
                }
                srTaskTreeData.setDataContent(jian);
                srTaskTreeData.setCreateUserName(srTaskTreeData.getUserDisplayName());
            }
        }
        return list;
        /*for (String key:datasetIdList){
            Integer page = 0;
            Integer size = 0;
            String d = "";
            List<Map> sort = new ArrayList<>();
            for (Map mm:data){
                if (key.equals(mm.get("datasetId"))){
                    d = String.valueOf(mm.get("page"));
                    page = Integer.parseInt(d);
                    d = String.valueOf(mm.get("size"));
                    size = Integer.parseInt(d);
                    if (mm.containsKey("sort") && mm.get("sort") != null){
                        sort = (List<Map>) mm.get("sort");
                    }
                }
            }
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/"+key+"?filterDisplay=true")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
            Map<String,Object> dataMap = (Map<String, Object>) JSON.parse(s);
            List<Map> cop = new ArrayList<>();
            if (groupBy.containsKey(key)){
                for (SrTaskTreeData srTaskTreeDatax:groupBy.get(key)){
                    if (srTaskTreeDatax.getDataContent()==null || srTaskTreeDatax.getDataContent().size()==0){

                    }else {
                        cop.addAll(srTaskTreeDatax.getDataContent());
                    }
                }
            }
            if (cop==null || cop.size()==0){
                dataMap.put("data",new ArrayList<>());
                Map pagebale = new HashMap();
                pagebale.put("totalElements",0);
                pagebale.put("number",page);
                pagebale.put("size",size);
                pagebale.put("totalPages",page+1);
                pagebale.put("sort",null);
                pagebale.put("numberOfElements",0);
                List<Map> lm = new ArrayList<>();
                if (sort!=null && sort.size()!=0) {
                    for (Map x : sort) {
                        Map ml = new HashMap();
                        ml.put("direction", String.valueOf(x.get("direction")));
                        ml.put("property", String.valueOf(x.get("property")));
                        ml.put("ignoreCase", false);
                        ml.put("nullHandling", "NATIVE");
                        ml.put("descending", true);
                        ml.put("ascending", false);
                        lm.add(ml);
                    }
                }

                pagebale.put("sort",lm);
                dataMap.put("pagebale",pagebale);
                res.put(key,dataMap);
            }else {
                List<Map> list1 = new ArrayList<>();
                Map<String,Object> rtn = new HashMap<>();
                Map sourceData = new HashMap();
                for (SrTaskTreeData srTaskTreeData:groupBy.get(key)){
                    List<Map> contentMap = srTaskTreeData.getDataContent();
                    StringBuffer DSID = new StringBuffer();
                    DSID.append("DSID =");
                    for (Map map1:contentMap){
                        DSID.append(" "+map1.get("DSID"));
                    }
                    client = new OkHttpClient.Builder()
                            .connectTimeout(10000, TimeUnit.MILLISECONDS)
                            .build();
                    RequestBody requestBody = RequestBody.create(JSONs, "ss");
                    request = new Request.Builder()
                            .post(requestBody)
                            .header("Authorization", httpServletRequest.getHeader("Authorization"))
                            .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()
                                +"/searchdatawithoutindex?param[]="+DSID.toString()+"&page=0&size=100000")
                            .build();
                    System.out.println(request.url().toString());
                    callTask = client.newCall(request);
                    response = callTask.execute();
                    body = response.body();
                    String ss= body.string();
                    Map<String,Object> map = JSONObject.parseObject(ss);
                    List<Map> jian = JSON.parseArray(JSON.toJSONString(map.get("content")),Map.class);
                    if (jian== null || jian.size()==0){

                    }else {
                        for (Map map1:jian){
                            map1.put("SOURCE",srTaskTreeData.getSource());
                            if (!map1.containsKey("taskDataId") || map1.get("taskDataId")==null || map1.get("taskDataId").equals("")){
                                map1.put("taskDataId",srTaskTreeData.getId());
                            }
                        }
                        list1.addAll(jian);
                    }
                }
                List<String> dsids = new ArrayList<>();
                List<Map> mapList = new ArrayList<>();
                mapList.addAll(list1);
                for (Map ms:list1){
                    if (dsids.contains(ms.get("DSID"))){
                        mapList.remove(ms);
                    }else {
                        dsids.add((String) ms.get("DSID"));
                    }
                }
                list1 = mapList;
                if (sort==null || sort.size()==0){
                    list1.sort(Comparator.comparing((Map h) -> ((String) h.get("CREATE_DATE"))));
                }else {
                    for (Map kz:sort) {
                        for (Map mc : list1) {
                            Object param = mc.get(kz.get("property"));
                            if (param != null) {
                                boolean x = false;
                                for (Map map:list1){
                                    if (map.get(kz.get("property"))==null){
                                        x = true;
                                        break;
                                    }
                                }
                                if (x==true){
                                    break;
                                }
                                if (param instanceof Integer) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof String) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Double) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Boolean) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Date) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else {
                                    Collections.sort(list1, Comparator.comparing(o -> Double.valueOf(o.get(kz.get("property")).toString())));
                                    break;
                                }
                            } else {

                            }
                        }
                    }
                }
                List<Map> list3 = new ArrayList<>();
                Integer x = (page+1)*size;
                Integer x1 = page*size;
                Integer x2 = (page+1)*size-1;
                if (list1.size()<=((page+1)*size)){
                    if (list1.size()==1){
                        list3 = list1;
                    }else {
                        list3 = list1.subList(page*size,list1.size());
                    }
                }else {
                    list3 = list1.subList(page*size,(page+1)*size);
                }
                Map<String,List<Map>> map = list3.stream().collect(groupingBy(e->e.get("SOURCE").toString()));
                List<Map<String,Object>> list2 = new ArrayList<>();
                for (String keys:map.keySet()){
                    Map<String,Object> map1 = new HashMap<>();
                    map1.put("source",keys);
                    map1.put("dataContent",map.get(keys));
                    list2.add(map1);
                }
                dataMap.put("data",list2);
                Map pagebale = new HashMap();
                pagebale.put("totalElements",list1.size());
                pagebale.put("number",page);
                pagebale.put("size",size);
                pagebale.put("totalPages",page+1);
                pagebale.put("sort",null);
                pagebale.put("numberOfElements",list3.size());
                List<Map> lm = new ArrayList<>();
                if (sort!=null && sort.size()!=0) {
                    for (Map z : sort) {
                        Map ml = new HashMap();
                        ml.put("direction", String.valueOf(z.get("direction")));
                        ml.put("property", String.valueOf(z.get("property")));
                        ml.put("ignoreCase", false);
                        ml.put("nullHandling", "NATIVE");
                        ml.put("descending", true);
                        ml.put("ascending", false);
                        lm.add(ml);
                    }
                }else {
                    Map ml = new HashMap();
                    ml.put("direction", String.valueOf("CREATE_DATE"));
                    ml.put("property", String.valueOf("ASC"));
                    ml.put("ignoreCase", false);
                    ml.put("nullHandling", "NATIVE");
                    ml.put("descending", true);
                    ml.put("ascending", false);
                    lm.add(ml);
                }
                pagebale.put("sort",lm);
                dataMap.put("pagebale",pagebale);
                res.put(key,dataMap);
            }

        }
        return res;*/
    }
    @Override
    public Map<String, Object> getNewTreeDataList(String projectId,String workId,String objectId,List<Map> data,
                                                  HttpServletRequest httpServletRequest) throws IOException {
        List<String> datasetIdList = new ArrayList<>();
        Map res = new HashMap();
        if (data!=null && data.size()!=0){
            for (Map mm:data){
                datasetIdList.add(String.valueOf(mm.get("datasetId")));
            }
        }
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> finalDatasetIdList = datasetIdList;
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",workId);
        queryWrapper.eq("object_id",objectId);
        if(!datasetIdList.isEmpty()){
            queryWrapper.in("dataset_id",datasetIdList);
        }
        queryWrapper.eq("bsflag","N");
        queryWrapper.orderByAsc("create_date");
        List<SrTaskTreeDataEx> srTaskTreeDataExes = srTaskTreeDataMapper.selectList(queryWrapper);
        for(SrTaskTreeDataEx ex : srTaskTreeDataExes){
            if(ex.getDataContent() != null){
                PGobject pgo = (PGobject)ex.getDataContent();
                List<Map> maps = JSON.parseArray(pgo.getValue(), Map.class);
                ex.setDataContent(maps);
            }

        }
        list = JSON.parseArray(JSON.toJSONString(srTaskTreeDataExes),SrTaskTreeData.class);
        /*Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                if (finalDatasetIdList !=null && finalDatasetIdList.size()!=0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String data : finalDatasetIdList) {
                        in.value(data);
                    }
                    predicates.add(in);
                }
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);*/
        Map<String, List<SrTaskTreeData>> groupBy = list.stream().collect(groupingBy(SrTaskTreeData::getDatasetId));


        for (String key:datasetIdList){
            Integer page = 0;
            Integer size = 0;
            String d = "";
            List<Map> sort = new ArrayList<>();
            for (Map mm:data){
                if (key.equals(mm.get("datasetId"))){
                    d = String.valueOf(mm.get("page"));
                    page = Integer.parseInt(d);
                    d = String.valueOf(mm.get("size"));
                    size = Integer.parseInt(d);
                    if (mm.containsKey("sort") && mm.get("sort") != null){
                        sort = (List<Map>) mm.get("sort");
                    }
                }
            }
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/"+key+"?filterDisplay=true")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
            Map<String,Object> dataMap = (Map<String, Object>) JSON.parse(s);
            List<Map> cop = new ArrayList<>();
            if (groupBy.containsKey(key)){
                for (SrTaskTreeData srTaskTreeDatax:groupBy.get(key)){
                    if (srTaskTreeDatax.getDataContent()==null || srTaskTreeDatax.getDataContent().size()==0){

                    }else {
                        cop.addAll(srTaskTreeDatax.getDataContent());
                    }
                }
            }
            if (cop==null || cop.size()==0){
                dataMap.put("data",new ArrayList<>());
                Map pagebale = new HashMap();
                pagebale.put("totalElements",0);
                pagebale.put("number",page);
                pagebale.put("size",size);
                pagebale.put("totalPages",page+1);
                pagebale.put("sort",null);
                pagebale.put("numberOfElements",0);
                List<Map> lm = new ArrayList<>();
                if (sort!=null && sort.size()!=0) {
                    for (Map x : sort) {
                        Map ml = new HashMap();
                        ml.put("direction", String.valueOf(x.get("direction")));
                        ml.put("property", String.valueOf(x.get("property")));
                        ml.put("ignoreCase", false);
                        ml.put("nullHandling", "NATIVE");
                        ml.put("descending", true);
                        ml.put("ascending", false);
                        lm.add(ml);
                    }
                }

                pagebale.put("sort",lm);
                dataMap.put("pagebale",pagebale);
                res.put(key,dataMap);
            }else {
                List<Map> list1 = new ArrayList<>();
                Map<String,Object> rtn = new HashMap<>();
                Map sourceData = new HashMap();
                for (SrTaskTreeData srTaskTreeData:groupBy.get(key)){
                    List<Map> contentMap = srTaskTreeData.getDataContent();
                    StringBuffer DSID = new StringBuffer();
                    DSID.append("DSID =");
                    for (Map map1:contentMap){
                        DSID.append(" "+map1.get("DSID"));
                    }
                    client = new OkHttpClient.Builder()
                            .connectTimeout(10000, TimeUnit.MILLISECONDS)
                            .build();
                    RequestBody requestBody = RequestBody.create(JSONs, "ss");
                    request = new Request.Builder()
                            .post(requestBody)
                            .header("Authorization", httpServletRequest.getHeader("Authorization"))
                            .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()
                                    +"/searchdatawithoutindex?param[]="+DSID.toString()+"&page=0&size=100000")
                            .build();
                    System.out.println(request.url().toString());
                    callTask = client.newCall(request);
                    response = callTask.execute();
                    body = response.body();
                    String ss= body.string();
                    Map<String,Object> map = JSONObject.parseObject(ss);
                    List<Map> jian = JSON.parseArray(JSON.toJSONString(map.get("content")),Map.class);
                    if (jian== null || jian.size()==0){

                    }else {
                        for (Map map1:jian){
                            map1.put("SOURCE",srTaskTreeData.getSource());
                            if (!map1.containsKey("taskDataId") || map1.get("taskDataId")==null || map1.get("taskDataId").equals("")){
                                map1.put("taskDataId",srTaskTreeData.getId());
                            }
                        }
                        list1.addAll(jian);
                    }
                }
                List<String> dsids = new ArrayList<>();
                List<Map> mapList = new ArrayList<>();
                mapList.addAll(list1);
                for (Map ms:list1){
                    if (dsids.contains(ms.get("DSID"))){
                        mapList.remove(ms);
                    }else {
                        dsids.add((String) ms.get("DSID"));
                    }
                }
                list1 = mapList;
                if (sort==null || sort.size()==0){
                    list1.sort(Comparator.comparing((Map h) -> ((String) h.get("CREATE_DATE"))));
                }else {
                    for (Map kz:sort) {
                        for (Map mc : list1) {
                            Object param = mc.get(kz.get("property"));
                            if (param != null) {
                                boolean x = false;
                                for (Map map:list1){
                                    if (map.get(kz.get("property"))==null){
                                        x = true;
                                        break;
                                    }
                                }
                                if (x==true){
                                    break;
                                }
                                if (param instanceof Integer) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof String) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Double) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Boolean) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else if (param instanceof Date) {
                                    if (kz.get("direction").equals("ASC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))));
                                    } else if (kz.get("direction").equals("DESC")) {
                                        list1.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))).reversed());
                                    }
                                    break;
                                } else {
                                    Collections.sort(list1, Comparator.comparing(o -> Double.valueOf(o.get(kz.get("property")).toString())));
                                    break;
                                }
                            } else {

                            }
                        }
                    }
//                    if (kz.get("property").toString().contains("DATE")){
//                        if (kz.get("direction").equals("ASC")){
//                            list1 = list1.stream().sorted(Comparator.comparing(a->String.valueOf(a.get(kz.get("property"))))).collect(toList());
//                        }else if (kz.get("direction").equals("DESC")){
//                            list1.sort(Comparator.comparing((Map h) -> ((String)h.get(kz.get("property")))).reversed());
//                        }
//
//                    }else {
//                        Collections.sort(list1 , new Comparator<Map>() {
//                        @Override
//                        public int compare(Map o1, Map o2) {
//                            Integer o1Value = Integer.valueOf(o1.get(kz.get("property")));
//                            Integer o2Value = Integer.valueOf((Integer) o2.get(kz.get("property")));
//                            if (kz.get("direction").equals("ASC")){
//                                return o1Value.compareTo(o2Value);
//                            }else if (kz.get("direction").equals("DESC")){
//                                return o2Value.compareTo(o1Value);
//                            }
//                            return o1Value.compareTo(o2Value);
//                        }
//                    });
//                    }
//                    list1 = list1.stream().sorted(Comparator.comparing(a->String.valueOf(a.get(kz.get("property"))))).collect(toList());
//                    Collections.sort(list1 , new Comparator<Map>() {
//                        @Override
//                        public int compare(Map o1, Map o2) {
//                            Integer o1Value = Integer.valueOf(o1.get(kz.get("property")).toString());
//                            Integer o2Value = Integer.valueOf(o2.get(kz.get("property")).toString());
//                            if (kz.get("direction").equals("ASC")){
//                                return o1Value.compareTo(o2Value);
//                            }else if (kz.get("direction").equals("DESC")){
//                                return o2Value.compareTo(o1Value);
//                            }
//                            return o1Value.compareTo(o2Value);
//                        }
//                    });
                }
                List<Map> list3 = new ArrayList<>();
                Integer x = (page+1)*size;
                Integer x1 = page*size;
                Integer x2 = (page+1)*size-1;
                if (list1.size()<=((page+1)*size)){
                    if (list1.size()==1){
                        list3 = list1;
                    }else {
                        list3 = list1.subList(page*size,list1.size());
                    }
                }else {
                    list3 = list1.subList(page*size,(page+1)*size);
                }
                Map<String,List<Map>> map = list3.stream().collect(groupingBy(e->e.get("SOURCE").toString()));
                List<Map<String,Object>> list2 = new ArrayList<>();
                for (String keys:map.keySet()){
                    Map<String,Object> map1 = new HashMap<>();
                    map1.put("source",keys);
                    map1.put("dataContent",map.get(keys));
                    list2.add(map1);
                }
                dataMap.put("data",list2);
                Map pagebale = new HashMap();
                pagebale.put("totalElements",list1.size());
                pagebale.put("number",page);
                pagebale.put("size",size);
                pagebale.put("totalPages",page+1);
                pagebale.put("sort",null);
                pagebale.put("numberOfElements",list3.size());
                List<Map> lm = new ArrayList<>();
                if (sort!=null && sort.size()!=0) {
                    for (Map z : sort) {
                        Map ml = new HashMap();
                        ml.put("direction", String.valueOf(z.get("direction")));
                        ml.put("property", String.valueOf(z.get("property")));
                        ml.put("ignoreCase", false);
                        ml.put("nullHandling", "NATIVE");
                        ml.put("descending", true);
                        ml.put("ascending", false);
                        lm.add(ml);
                    }
                }else {
                    Map ml = new HashMap();
                    ml.put("direction", String.valueOf("CREATE_DATE"));
                    ml.put("property", String.valueOf("ASC"));
                    ml.put("ignoreCase", false);
                    ml.put("nullHandling", "NATIVE");
                    ml.put("descending", true);
                    ml.put("ascending", false);
                    lm.add(ml);
                }
                pagebale.put("sort",lm);
                dataMap.put("pagebale",pagebale);
                res.put(key,dataMap);
            }

        }
        return res;
    }

    @Override
    public SrTaskTreeData saveData(String workId, String taskId, String wellName, String wellPath, HttpServletRequest httpServletRequest)  throws IOException{
        /**
         * 1、通过wellName获取井信息
         * 2、通过1013session服务获取当前任务Id和工作Id
         * 3、通过1013session服务获取当前节点Id和Name
         * 4、转换数据为数据集格式
         * 5、调用归档服务进行归档处理
         */
        String fileName = "temp/wellPath.txt";
        String projectId = CRPDBSCHEMAID;
        SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
        srTaskTreeData.setDatasetType("fiele");
        srTaskTreeData.setDataType("结构化");
        srTaskTreeData.setDatasetId("46a8bcecc9add9563a0e9c799c5d9040");
        srTaskTreeData.setDatasetName("设计定向井轨迹数据");
        srTaskTreeData.setSource("专业软件");
        srTaskTreeData.setFileName(wellName + "设计定向井轨迹数据");
        srTaskTreeData.setObjectName(wellName);
        //根据井名查询对应的井ID
        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build();

        String fileter = "{" +
                "\"dataRegions\": [" +
                "\"TL\"" +
                "]," +
                "\"fields\":[" +
                "\"WELL_ID\"," +
                "\"WELLBORE_ID\"," +
                "\"WELL_COMMON_NAME\"," +
                "\"WELLBORE_LABEL\"]," +
                "\"filter\": {" +
                "\"key\": \"WELL_COMMON_NAME\"," +
                "\"logic\": \"AND\"," +
                "\"realValue\": " +
                "[\"" +
                wellName +
                "\"]," +
                "\"symbol\": \"=\"" +
                "}" +
                "}";
        MediaType reqBody = MediaType.parse("application/json; charset=utf-8");
        RequestBody filterBody = RequestBody.create(reqBody,fileter );
        Request request = new Request.Builder()
                .post(filterBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/sys/dataservice/udb/X_CD_WELL_SOURCE?page=0&size=1")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String res = body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(res);
        List<Map> wellInfoL = JSON.parseArray(JSON.toJSONString(dataMap.get("content")),Map.class);
        String wellId = "" ,wellboreId = "",wellboreName = "";
        if(null != wellInfoL && wellInfoL.size() > 0 ){
            wellId = wellInfoL.get(0).get("WELL_ID").toString();
            wellboreId = wellInfoL.get(0).get("WELLBORE_ID").toString();
            wellboreName = wellInfoL.get(0).get("WELLBORE_LABEL").toString();
        }
        srTaskTreeData.setObjectId(wellId);
        //获取工作及任务信息
        if(StringUtils.isEmpty(workId) || StringUtils.isEmpty(taskId)){
            request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/sys/session/tlmTaskInfo")
                    .build();
            callTask = client.newCall(request);
            response = callTask.execute();
            body = response.body();
            res = body.string();
            Map<String, Object> workInfoMap = JSONObject.parseObject(StringUtils.isEmpty(res)?"":res.substring(1,res.length()-1).replace("\\",""));
            if(null != workInfoMap && !workInfoMap.isEmpty()){
                workId = workInfoMap.get("workId").toString();
                taskId = workInfoMap.get("taskId").toString();
            }
        }
        srTaskTreeData.setWorkId(workId);
        srTaskTreeData.setTaskId(taskId);
        //获取节点信息
        request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/sys/session/tlmNodeInfo")
                .build();
        callTask = client.newCall(request);
        response = callTask.execute();
        body = response.body();
        res = body.string();
        Map<String, Object> nodeInfoMap = JSONObject.parseObject(StringUtils.isEmpty(res)?"":res.substring(1,res.length()-1).replace("\\",""));
        if(null != nodeInfoMap && !nodeInfoMap.isEmpty()){
            srTaskTreeData.setNodeId(nodeInfoMap.get("nodeId").toString());
            srTaskTreeData.setNodeNames(nodeInfoMap.get("nodeName").toString());
        }
        List<Map<String, Object>> wellPathReturn = new ArrayList<>();
        if (!StringUtils.isEmpty(wellPath)) {
            List<String> wellPathL = Arrays.asList(wellPath.split(","));
            List<String> wellPathInfoL;
            Map<String, Object> wellPathM;
            if (null != wellPathL && wellPathL.size() > 0) {
                for (String wellPathS : wellPathL) {
                    wellPathInfoL = Arrays.asList(wellPathS.split("\\|"));
                    if (null != wellPathInfoL && wellPathInfoL.size() > 6) {
                        wellPathM = new HashMap<>();
                        wellPathM.put("WELL_PATH_ID", UUID.randomUUID().toString().replaceAll("-", ""));
                        wellPathM.put("WELL_ID", wellId);
                        wellPathM.put("WELLBORE_ID", wellboreId);
                        wellPathM.put("WELL_COMMON_NAME", wellName);
                        wellPathM.put("WELLBORE_COMMON_NAME", wellboreName);
                        //测深、垂深、井斜角、方位角、东西位移、南北位移、闭合距
                        wellPathM.put("MEASURED_DEPTH", StringUtils.isEmpty(wellPathInfoL.get(0)) ? -9999 : Double.parseDouble(wellPathInfoL.get(0)));
                        wellPathM.put("VERTICAL_DEPTH", StringUtils.isEmpty(wellPathInfoL.get(1)) ? null : Double.parseDouble(wellPathInfoL.get(1)));
                        wellPathM.put("HOLE_DEVIATION", StringUtils.isEmpty(wellPathInfoL.get(2)) ? null : Double.parseDouble(wellPathInfoL.get(2)));
                        wellPathM.put("AZIMUTH", StringUtils.isEmpty(wellPathInfoL.get(3)) ? null : Double.parseDouble(wellPathInfoL.get(3)));
                        wellPathM.put("EAST_WEST_DISPLACE", StringUtils.isEmpty(wellPathInfoL.get(4)) ? null : Double.parseDouble(wellPathInfoL.get(4)));
                        wellPathM.put("SOUTH_NORTH_DISPLACE", StringUtils.isEmpty(wellPathInfoL.get(5)) ? null : Double.parseDouble(wellPathInfoL.get(5)));
                        wellPathM.put("H_DEPARTURE_ELDER", StringUtils.isEmpty(wellPathInfoL.get(6)) ? null : Double.parseDouble(wellPathInfoL.get(6)));
                        wellPathReturn.add(wellPathM);
                    }
                }
            }
        }
        JSONArray data = JSONArray.parseArray(JSONArray.toJSON(wellPathReturn).toString());
        System.out.println("workId:" + srTaskTreeData.getWorkId() + ";taskId:"+ srTaskTreeData.getTaskId()
                + ";nodeId:"+srTaskTreeData.getNodeId()+";objId:"+srTaskTreeData.getObjectId()+";dataSetId:"+
                srTaskTreeData.getDatasetId()+";data:"+data.toString());
        return saveSrTreeData(projectId, data, srTaskTreeData, httpServletRequest);
    }

    private String loadFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String str;
            while((str = reader.readLine()) != null) {
                str = new String(str.trim().getBytes(), "UTF-8");
                stringBuilder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private Map<String,List<Map>> reviewDataGroup(String objectId, String objectName,List<Map> data){
        for (Map map:data){
            if (map.get("OBJECT_ID")==null || map.get("OBJECT_ID").equals("")){
                map.put("OBJECT_ID",objectId);
                map.put("OBJECT_NAME",objectName);
            }else if (map.get("OBJECT_NAME")==null || map.get("OBJECT_NAME").equals("")){
                map.put("OBJECT_ID",objectId);
                map.put("OBJECT_NAME",objectName);
            }
        }
        Map<String,List<Map>> map = data.stream().collect(groupingBy(e->e.get("OBJECT_ID").toString()));
        return map;
    }

    @Override
    public List<SrTaskTreeData> getObjectByYear(String nodeId,String userId) {
        List<SrTaskTreeData> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");
        Date date = new Date();
        String newDate=sdf.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, -1);
        date= calendar.getTime();
        String oldDate = sdf.format(date);
        Date finalDate = date;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                predicates.add(criteriaBuilder.between(root.get("createDate"), finalDate, new Date()));
                if(userId!=null && !userId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("createUser"), userId));
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        return list;
    }

    @Override
    public List<SrTaskTreeData>  getObjectByUser(String workId, String userId, String nodeId, String dataStatus,
                                                 Date startTime, Date endTime,String datasetId) {
        JSONObject rtn = new JSONObject();
        StringBuffer serviceName = new StringBuffer();
        //获取所有当前用户在当前工作的成果
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> userIdList = new ArrayList<>();
        List<String> nodeIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(userId)) {
            userIdList = Arrays.asList(userId.split(","));
        }
        if(StringUtils.isNotBlank(nodeId)) {
            nodeIdList = Arrays.asList(nodeId.split(","));
        }
        List<String> finalUserIdList = userIdList;
        List<String> finalNodeIdList = nodeIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(finalUserIdList !=null && finalUserIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("createUser"));
                    for (String user : finalUserIdList) {
                        in.value(user);
                    }
                    predicates.add(in);
                }
                if (startTime !=null && endTime !=null){
                    predicates.add(criteriaBuilder.between(root.get("createDate"), startTime, endTime));
                }
                if(finalNodeIdList !=null && finalNodeIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("nodeId"));
                    for (String node : finalNodeIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if(datasetId!=null && !datasetId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("datasetId"), datasetId));
                }
                if(workId!=null && !workId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                }
                if(dataStatus!=null && !dataStatus.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("dataStatus"), dataStatus));
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
/*
        for (SrTaskTreeData srTaskTreeData : list) {
            srTaskTreeData.setTaskName(srWrokTaskRepository.findOne(srTaskTreeData.getTaskId()).getTaskName());
        }*/
        return list;
    }

    @Override
    public Map<String,Object> getObjectBy(Pageable page, String workId, String fileName, String nodeId, String dataStatus,
                                          Date startTime, Date endTime, String datasetId, String userId,String belong, HttpServletRequest httpServletRequest) throws IOException {
        //获取所有当前用户在当前工作的成果
        List<String> nodeIdList = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(nodeId)) {
            nodeIdList = Arrays.asList(nodeId.split(","));
        }
        if(StringUtils.isNotBlank(datasetId)) {
            datasetIdList = Arrays.asList(datasetId.split(","));
        }
        String dataTargetType = httpServletRequest.getParameter("dataTargetType");
        String objectId = httpServletRequest.getParameter("objectId");
        String source = httpServletRequest.getParameter("source");//数据来源
        String updateUser = httpServletRequest.getParameter("updateUser");//提交人
        String dataStatus0 = httpServletRequest.getParameter("dataStatus");//审核状态
        String firstChoice = httpServletRequest.getParameter("firstChoice");//首选

        List<String> finalNodeIdList = nodeIdList;
        List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (startTime !=null && endTime !=null){
                    predicates.add(criteriaBuilder.between(root.get("createDate"), startTime, endTime));
                }
                if(finalNodeIdList !=null && finalNodeIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("nodeId"));
                    for (String node : finalNodeIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if(finalDatasetIdList !=null && finalDatasetIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String node : finalDatasetIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if(workId!=null && !workId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                }
                if (!org.springframework.util.StringUtils.isEmpty(fileName)) {
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%" + fileName + "%"));
                }
                if(dataStatus!=null && !dataStatus.equals("")){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("dataStatus"));
                    String[] shzt=dataStatus.split(",");
                    for(int a=0;a<shzt.length;a++){
                        in.value(shzt[a]);
                    }
                    predicates.add(in);
                }
                if(firstChoice!=null && !firstChoice.equals("")){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("firstChoice"));
                    String[] shzt=firstChoice.split(",");
                    for(int a=0;a<shzt.length;a++){
                        in.value(shzt[a]);
                    }
                    predicates.add(in);
                }

                if(source!=null && !source.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("source"), "%" + source + "%"));
                }
                if(updateUser!=null && !updateUser.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("updateUser"), updateUser));
                }
                if(dataStatus0!=null && !dataStatus0.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("dataStatus"), "%" + dataStatus0 + "%"));
                }

                if(userId!=null && !userId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("createUser"), userId));
                }
                if (StringUtils.isNotEmpty(objectId)) {
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                }
                if (StringUtils.isNotEmpty(dataTargetType)) {
                    predicates.add(criteriaBuilder.equal(root.get("dataTargetType"), dataTargetType));
                }
                if(StringUtils.isNotEmpty(belong)){
                    predicates.add(criteriaBuilder.equal(root.get("belong"), belong));
                }
                predicates.add(criteriaBuilder.equal(root.get("datasetType"), "file"));
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        Page<SrTaskTreeData> all = srTaskTreeDataRepo.findAll(spec, page);
        List<SrTaskTreeData> list = all.getContent();
        Map<String,List<SrTaskTreeData>> map = list.stream().collect(Collectors.groupingBy(SrTaskTreeData::getDatasetId));
        Map<String,Object> rtn = new HashMap<>();
        List<Map<String,Object>> res = new ArrayList<>();
        for (String dsId:datasetIdList){
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/"+dsId+"?filterDisplay=true")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
            Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            Map<String,Object> nMap = new HashMap<>();
            if (map.containsKey(dsId)){
                List<SrTaskTreeData> taskTreeData = map.get(dsId);
                nMap.putAll(dataMap);
                taskTreeData = taskTreeData.stream().sorted(Comparator.comparing(SrTaskTreeData::getUpdateDate).reversed()).collect(Collectors.toList());
                nMap.put("dataList",taskTreeData);
                res.add(nMap);
            }else {
                nMap.putAll(dataMap);
                nMap.put("dataList",new ArrayList<>());
                res.add(nMap);
            }
        }
        rtn.put("content",res);
        return rtn;
    }
    //保存研究资料数据
    private SrTaskTreeData processSaveMaterialSrTreeData(SrTaskTreeData srTaskTreeData,JSONArray data,String projectId,
                                                         HttpServletRequest httpServletRequest) throws Exception {
        String msg="";
        int successNum = 0;
        //研究资料特殊处理
        List<Map> xnewlist = JSON.parseArray(JSON.toJSONString(data), Map.class);
        Map<String, List<Map>> groupData = new HashMap<>();
        List<Map> groupDataOpt = null;
        for (Map map : xnewlist) {
            if (groupData.containsKey(map.get("objectId"))) {
                groupData.get(map.get("objectId").toString()).add(map);
            } else {
                groupDataOpt = new ArrayList<>();
                groupDataOpt.add(map);
                groupData.put(map.get("objectId").toString(), groupDataOpt);
            }
        }
        if (StringUtils.equals("结构化", srTaskTreeData.getDataType())) {
            //把基本数据先获取回来
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder().get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                    .build();
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
            Map<String,Object> dataMap = (Map<String, Object>) JSON.parse(s);
            //一个一个处理
            for(String k : groupData.keySet()){
                List<Map> listMap = groupData.get(k);
                SrTaskTreeData sttde = JSON.parseObject(JSON.toJSONString(srTaskTreeData),SrTaskTreeData.class);

                SrTaskTreeData finalSrTaskTreeData = srTaskTreeData;
                Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
                    @Override
                    public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("workId"), finalSrTaskTreeData.getWorkId()));
                        predicates.add(criteriaBuilder.equal(root.get("datasetId"), finalSrTaskTreeData.getDatasetId()));
                        predicates.add(criteriaBuilder.equal(root.get("nodeId"), finalSrTaskTreeData.getNodeId()));
                        predicates.add(criteriaBuilder.equal(root.get("firstChoice"),"A"));
                        predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                        predicates.add(criteriaBuilder.equal(root.get("datasetType"), "file"));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                    }
                };

                List<SrTaskTreeData> list = srTaskTreeDataRepo.findAll(spec);
                if (list==null || list.size()==0){
                    sttde.setFirstChoice("A");
                    sttde.setDatasetType("data");// 保存类型，数据列表用data,成果列表用file
                }else {
                    sttde.setDatasetType("data");
                    SrTaskTreeData sw = list.get(0);
                    sw.setFirstChoice("C");
                    srTaskTreeDataRepo.save(sw);
                    sttde.setFirstChoice("A");
                }
                sttde.setObjectName(listMap.get(0).get("objectName").toString());
                sttde.setObjectId(listMap.get(0).get("objectId").toString());
                sttde.setFileName(sttde.getObjectName()+"-"+sttde.getDatasetName());
                genFileName(sttde);
                //存储基本信息
                sttde =srTaskTreeDataRepo.save(sttde);
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                for (Map cm:listMap){
                    //if (!cm.containsKey("DSID") || cm.get("DSID")==null){
                    cm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+
                            UUID.randomUUID().toString().replaceAll("-", ""));
                    //}
                    if (!cm.containsKey("BSFLAG") || cm.get("BSFLAG")==null){
                        cm.put("BSFLAG","1");
                    }
                    if (!cm.containsKey("DATA_REGION") || cm.get("DATA_REGION")==null){
                        cm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                    }
                    if (!cm.containsKey("DATA_GROUP") || cm.get("DATA_GROUP")==null){
                        if (ServerAddr.equals("www.dev.pcep.cloud")) {
                            cm.put("DATA_GROUP", "ACTIJD100001852");
                        }else {
                            cm.put("DATA_GROUP", "APPSTLgcjs12345");
                        }
                    }
                    if (!cm.containsKey("CREATE_USER_ID") || cm.get("CREATE_USER_ID")==null){
                        cm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("CREATE_DATE") || cm.get("CREATE_DATE")==null){
                        cm.put("CREATE_DATE",(sdf.format(new Date())));
                    }
                    if (!cm.containsKey("CREATE_APP_ID") || cm.get("CREATE_APP_ID")==null){
                        cm.put("CREATE_APP_ID","TLM_CRP");
                    }
                    if (!cm.containsKey("UPDATE_USER_ID") || cm.get("UPDATE_USER_ID")==null){
                        cm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("UPDATE_DATE") || cm.get("UPDATE_DATE")==null){
                        cm.put("UPDATE_DATE",sdf.format(new Date()));
                    }
                    cm.put("taskDataId",sttde.getId());
                }
                String ss = JSON.toJSONString(listMap);
                String url = "http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+sttde.getDatasetId()+"/savedatafullresult";
                client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON,ss);
                request = new Request.Builder().post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+sttde.getDatasetId()+"/savedatafullresult")
                        .build();
                long id = System.currentTimeMillis();
                System.out.println("----["+id+"]开始请求接口---------结构化数据银行");
                System.out.println("----["+id+"]url:"+url);
                System.out.println("----["+id+"]body:"+ss);
                System.out.println("----["+id+"]Authorization:"+httpServletRequest.getHeader("Authorization"));
                callTask = client.newCall(request);
                response = callTask.execute();
                body = response.body();
                s= body.string();
                System.out.println("----["+id+"]接口返回:"+s);
                Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
                if ((boolean)saveMap.get("flag")){
                    successNum++;
                    sttde.setDataContent(listMap);
                    srTaskTreeDataRepo.save(sttde);
                    //return srTaskTreeData;
                }else {
                    srTaskTreeDataRepo.delete(sttde.getId());
                    String dayinprintln="【"+listMap.get(0).get("objectName")+"】报错："+saveMap.get("result");
                    System.out.println(dayinprintln);
                    msg=msg+dayinprintln;
                    //return null;
                }
            }
            if(successNum > 0){
                return  srTaskTreeData;
            }else{
                throw new Exception(msg); //抛异常
            }
        } else {
            //非结构化的保存
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
            String text = JSON.toJSONString(srTaskTreeData);
            String baseUrl = "http://" + ServerAddr + "/core/document/"+projectId+"/"+srTaskTreeData.getDatasetId()
                    +"/项目成果?";
            String token = httpServletRequest.getHeader("Authorization");
            for(String k : groupData.keySet()){
                List<Map> listMap = groupData.get(k);
                //分为两种情况，listMap大小为1，listMap大小大于1
                if(listMap.size()>1){
                    for (Map cm:listMap) {
                        try {
                            int i = saveRecord(text, cm, client, baseUrl, token);
                            successNum += i;
                        } catch (Exception e) {
                            e.printStackTrace();
                            String dayinprintln="【"+cm.get("objectName")+"】报错："+e.getMessage();
                            msg += dayinprintln;
                        }
                    }
                }else{
                    Map map = listMap.get(0);
                    try {
                        int i = saveRecord(text,map,client,baseUrl,token);
                        successNum += i;
                    } catch (Exception e) {
                        e.printStackTrace();
                        String dayinprintln="【"+map.get("objectName")+"】报错："+e.getMessage();
                        msg += dayinprintln;
                    }
                }
            }
            if(successNum > 0){
                return  srTaskTreeData;
            }else{
                throw new Exception(msg); //抛异常
            }
        }
    }
    public int saveRecord(String text,Map cm,OkHttpClient client,String baseUrl,String token)throws Exception{
        int ret = 0;
        SrTaskTreeData sttde = JSON.parseObject(text,SrTaskTreeData.class);
        sttde.setId(ShortUUID.randomUUID());
        sttde.setObjectName(cm.get("objectName").toString());
        sttde.setObjectId(cm.get("objectId").toString());
        sttde.setFileName(sttde.getObjectName()+"-"+sttde.getDatasetName());
        genFileName(sttde);
        if(tupian(sttde,cm)){
            return ret;
        }
        List<Map> content = new ArrayList<>();
        content.add(cm);
        sttde.setDataContent(content);
        srTaskTreeDataRepo.save(sttde);
        try{
            saveToDocument(client,baseUrl,token,sttde);
            ret = 1;
        }catch (Exception e){
            srTaskTreeDataRepo.delete(sttde.getId());
            throw e;
        }
        return ret;
    }

    /**
     * 非结构化数据保存至文档中心
     * @param client
     * @param baseUrl
     * @param token
     * @param cm
     * @throws Exception
     */
    public void saveToDocument(OkHttpClient client,String baseUrl,String token,SrTaskTreeData cm)throws Exception{
        Object fileId=cm.getFileId();
        Object fileAllName=cm.getFileAllName();
        StringBuffer stringBuffer =null;
        List<Map> data=cm.getDataContent();
        if(data != null && data.size()>0){
            Map<String,Object> map = data.get(0);
            //1、智能推荐、数据同步（数据来源：侏罗纪）属性
            StringBuffer stringBuffer1 = new StringBuffer();
            try {
                String keys="customParameters,file_id,file_name,objectId,objectName,objectType,DSID,SOURCE,source,projectId," +//制图+本地上传+智能推荐
                        "dataStatus,dataTargetTypeZt,dataType,datasetId,datasetName,datasetType,fileAllName," +
                        "fileId,fileName,fileSize,firstChoice,nodeId,nodeNames,taskId,workId," +
                        "bo,client,ptName,saveImageViewUrl,viewKey";//制图&
                for(String key : map.keySet()){
                    String val=map.get(key)==null?"":map.get(key).toString();
                    if(keys.contains(key) || StringUtils.isEmpty(val)){
                        continue;
                    }
                    if(val.contains("&")){
                        System.out.println("val值包含关键字&跳过，"+key+":"+val);
                        continue;
                    }
                    stringBuffer1.append("%2C%22").append(key).append("%22%3A%22").append(map.get(key)).append("%22");
                }
            } catch (Exception e) {
                System.out.println("获取customParameters参数出错"+e.getMessage());
                e.printStackTrace();
                //如果发生异常，清空参数
                stringBuffer1.setLength(0);
            }
            //2、如果传递了customParameters属性，拼接参数
            stringBuffer = new StringBuffer(stringBuffer1);
            try {
                if(map.containsKey("customParameters")){
                    JSONArray customParameters = (JSONArray) map.get("customParameters");
                    for(Object o : customParameters){
                        JSONObject jo = (JSONObject)o;
                        for(String key : jo.keySet()){
                            stringBuffer.append("%2C%22").append(key).append("%22%3A%22").append(jo.getString(key)).append("%22");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("获取customParameters参数出错"+e.getMessage());
                e.printStackTrace();
                //如果发生异常，清空参数
                stringBuffer.setLength(0);
            }
        }else{
            stringBuffer = new StringBuffer();
        }
        baseUrl += "data=%7B%22file_id%22%3A%22"+fileId+"%22%2C%22file_name%22%3A%22"+
            fileAllName+"%22"+stringBuffer.toString()+"%7D";//file_size
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", token)
                .url(baseUrl)
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        if (!s.equals("true")){
            throw new Exception(baseUrl+"\n接口报错："+s); //抛异常
        }
    }

    public Boolean tupian(SrTaskTreeData srTaskTreeData,Map<String,Object> cm){
        String FILE_ID=cm.get("FILE_ID") == null ?"": cm.get("FILE_ID").toString();
        String file_id=cm.get("file_id") == null ?FILE_ID: cm.get("file_id").toString();
        String fileId = cm.get("fileId") == null ?file_id: cm.get("fileId").toString();

        String FILE_NAME=cm.get("FILE_NAME") == null ?"": cm.get("FILE_NAME").toString();
        String file_name=cm.get("file_name") == null ?FILE_NAME: cm.get("file_name").toString();
        String fileAllName = cm.get("fileAllName") == null ? file_name : cm.get("fileAllName").toString();

        String FILE_SIZE = cm.get("FILE_SIZE")==null?"":cm.get("FILE_SIZE").toString();
        String file_size = cm.get("file_size")==null?FILE_SIZE:cm.get("file_size").toString();
        String fileSize = cm.get("fileSize")==null?file_size:cm.get("fileSize").toString();

        srTaskTreeData.setFileId(fileId);
        srTaskTreeData.setFileSize(fileSize);
        srTaskTreeData.setFileAllName(fileAllName);
        if(StringUtils.isEmpty(fileId)  || StringUtils.isEmpty(fileAllName)){
            return true;
        }else{
            return false;
        }
    }
    //保存成果数据
    private SrTaskTreeData processSaveResuleSrTreeData(SrTaskTreeData srTaskTreeData,JSONArray data,String projectId,
                                                        HttpServletRequest httpServletRequest)throws Exception{
            srTaskTreeData.setDataContent(JSON.parseArray(JSON.toJSONString(data),Map.class));
            if (srTaskTreeData.getDataType().equals("非结构化")){
                List<Map> maps = JSON.parseArray(JSON.toJSONString(data), Map.class);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
                String token = httpServletRequest.getHeader("Authorization");
                String baseUrl = "http://" + ServerAddr + "/core/document/"+projectId+"/"+srTaskTreeData.getDatasetId()
                        +"/项目成果?";
                //如果dataContent有多条记录，生成多个主记录
                if(maps.size()>1){
                    //存在多条记录
                    srTaskTreeData.setDataContent(null);
                    String text = JSON.toJSONString(srTaskTreeData);
                    List<SrTaskTreeData> successList = new ArrayList<>();
                    for(Map cm : maps){
                        SrTaskTreeData taskTreeData = JSON.parseObject(text, SrTaskTreeData.class);
                        taskTreeData.setId(ShortUUID.randomUUID());
                        if(tupian(taskTreeData,cm)){
                            continue;
                        }
                        List<Map> contentData = new ArrayList<>();
                        contentData.add(cm);
                        taskTreeData.setDataContent(contentData);
                        srTaskTreeDataRepo.save(taskTreeData);
                        try{
                            saveToDocument(client,baseUrl,token,taskTreeData);
                            successList.add(taskTreeData);
                        }catch (Exception e){
                            srTaskTreeDataRepo.delete(taskTreeData.getId());
                            System.out.println("调用文档中心接口失败，接口返回信息："+e.getMessage());
                            //throw e;
                        }
                    }
                    if(successList.isEmpty()){
                        return null;
                    }else{
                        //任意返回一个
                        return successList.get(0);
                    }
                }else{
                    Map cm = maps.get(0);
                    if(tupian(srTaskTreeData,cm)){
                        throw new Exception("明细数据：图片ID或图片名称为空"); //抛异常
                    }
                    srTaskTreeData =srTaskTreeDataRepo.save(srTaskTreeData);
                    try{
                        saveToDocument(client,baseUrl,token,srTaskTreeData);
                    }catch (Exception e){
                        srTaskTreeDataRepo.delete(srTaskTreeData.getId());
                        throw e;
                    }
                    return srTaskTreeData;
                }
            }else if (srTaskTreeData.getDataType().equals("结构化")){
                srTaskTreeData =srTaskTreeDataRepo.save(srTaskTreeData);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//                MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
                Request request = new Request.Builder()
                        .get()
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                        .build();
                Call callTask = client.newCall(request);
                Response response = callTask.execute();
                ResponseBody body = response.body();
                String s= body.string();
                Map<String,Object> dataMap = (Map<String, Object>) JSON.parse(s);
                List<Map> newlist = JSON.parseArray(JSON.toJSONString(data),Map.class);
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                for (Map cm:newlist){
                    //if (!cm.containsKey("DSID") || cm.get("DSID")==null){
                    cm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+
                            UUID.randomUUID().toString().replaceAll("-", ""));
                    //}
                    if (!cm.containsKey("BSFLAG") || cm.get("BSFLAG")==null){
                        cm.put("BSFLAG","1");
                    }
                    if (!cm.containsKey("DATA_REGION") || cm.get("DATA_REGION")==null){
                        cm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                    }
                    if (!cm.containsKey("DATA_GROUP") || cm.get("DATA_GROUP")==null){
                        if (ServerAddr.equals("www.dev.pcep.cloud")) {
                            cm.put("DATA_GROUP", "ACTIJD100001852");
                        }else {
                            cm.put("DATA_GROUP", "APPSTLgcjs12345");
                        }
                    }
                    if (!cm.containsKey("CREATE_USER_ID") || cm.get("CREATE_USER_ID")==null){
                        cm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("CREATE_DATE") || cm.get("CREATE_DATE")==null){
                        cm.put("CREATE_DATE",(sdf.format(new Date())));
                    }
                    if (!cm.containsKey("CREATE_APP_ID") || cm.get("CREATE_APP_ID")==null){
                        cm.put("CREATE_APP_ID","TLM_CRP");
                    }
                    if (!cm.containsKey("UPDATE_USER_ID") || cm.get("UPDATE_USER_ID")==null){
                        cm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("UPDATE_DATE") || cm.get("UPDATE_DATE")==null){
                        cm.put("UPDATE_DATE",sdf.format(new Date()));
                    }
                    cm.put("taskDataId",srTaskTreeData.getId());
                }
                String ss = JSON.toJSONString(newlist);
                String url = "http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()+"/savedatafullresult";
                client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
                MediaType json = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(json,ss);
                request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url(url)
                        .build();
                long id = System.currentTimeMillis();
                System.out.println("----["+id+"]开始请求接口---------结构化数据银行");
                System.out.println("----["+id+"]url:"+url);
                System.out.println("----["+id+"]body:"+ss);
                System.out.println("----["+id+"]Authorization:"+httpServletRequest.getHeader("Authorization"));
                callTask = client.newCall(request);
                response = callTask.execute();
                body = response.body();
                s= body.string();
                System.out.println("----["+id+"]接口返回:"+s);
                Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
                if ((boolean)saveMap.get("flag")){
                    srTaskTreeData.setDataContent(newlist);
                    srTaskTreeDataRepo.save(srTaskTreeData);
                    return srTaskTreeData;
                }else {
                    srTaskTreeDataRepo.delete(srTaskTreeData.getId());
                    System.out.print("----------------------------------------数据上传失败-----------------------------------------");
                    System.out.print("报错"+s);
                    throw new Exception(saveMap.get("result")+""); //抛异常
//                    return null;
                }
            }
        return null;
    }

    //原先的方法，留存
    @Override
    public synchronized SrTaskTreeData saveSrTreeData(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                                      HttpServletRequest httpServletRequest) {
        boolean x = false;
        try {
            List<SrTaskTreeData> list = new ArrayList<>();
            SrTaskTreeData finalSrTaskTreeData = srTaskTreeData;
            Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
                @Override
                public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("workId"), finalSrTaskTreeData.getWorkId()));
                    predicates.add(criteriaBuilder.equal(root.get("datasetId"), finalSrTaskTreeData.getDatasetId()));
                    predicates.add(criteriaBuilder.equal(root.get("nodeId"), finalSrTaskTreeData.getNodeId()));
                    predicates.add(criteriaBuilder.equal(root.get("firstChoice"),"A"));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                    predicates.add(criteriaBuilder.equal(root.get("datasetType"), "file"));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            list= srTaskTreeDataRepo.findAll(spec);
            if (list==null || list.size()==0){
                srTaskTreeData.setFirstChoice("A");
                srTaskTreeData.setDatasetType("file");
            }else {
                srTaskTreeData.setDatasetType("file");
                SrTaskTreeData sw = list.get(0);
                sw.setFirstChoice("C");
                srTaskTreeDataRepo.save(sw);
                srTaskTreeData.setFirstChoice("A");
            }
            srTaskTreeData =srTaskTreeDataRepo.save(srTaskTreeData);
            if (srTaskTreeData.getDataType().equals("非结构化")){
                Map<String,String> map = new HashMap<>();
                map.put("\"file_id\"","\""+srTaskTreeData.getFileId()+"\"");
                map.put("\"file_name\":","\""+srTaskTreeData.getFileAllName()+"\"");
                String mapp = "{\"file_id\":"+srTaskTreeData.getFileId()+"},{\"file_name\":\""+srTaskTreeData.getFileAllName()+"\"}";
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
//                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = new FormBody.Builder().build();
                Request request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/document/"+projectId+"/"+srTaskTreeData.getDatasetId()
                                +"/项目成果?data=%7B%22file_id%22%3A%22"+srTaskTreeData.getFileId()
                                +"%22%2C%22file_name%22%3A%22"+srTaskTreeData.getFileAllName()+"%22%7D")
                        .build();
                Call callTask = client.newCall(request);
                Response response = callTask.execute();
                ResponseBody body = response.body();
                String s= body.string();
                if (!s.equals("true")){
                    srTaskTreeDataRepo.delete(srTaskTreeData.getId());
                    return null;
                }else {
                    return srTaskTreeData;
                }
            }else if (srTaskTreeData.getDataType().equals("结构化")){
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//                MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
                Request request = new Request.Builder()
                        .get()
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/"+srTaskTreeData.getDatasetId()+"?filterDisplay=true")
                        .build();
                Call callTask = client.newCall(request);
                Response response = callTask.execute();
                ResponseBody body = response.body();
                String s= body.string();
                Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
                List<Map> newlist = com.alibaba.fastjson.JSON.parseArray(com.alibaba.fastjson.JSON.toJSONString(data),Map.class);
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                for (Map cm:newlist){
                    if (!cm.containsKey("DSID") || cm.get("DSID")==null){
                        cm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+
                                UUID.randomUUID().toString().replaceAll("-", ""));
                    }
                    if (!cm.containsKey("BSFLAG") || cm.get("BSFLAG")==null){
                        cm.put("BSFLAG","1");
                    }
                    if (!cm.containsKey("DATA_REGION") || cm.get("DATA_REGION")==null){
                        cm.put("DATA_REGION",SpringManager.getCurrentUser().getDataRegion());
                    }
                    if (!cm.containsKey("DATA_GROUP") || cm.get("DATA_GROUP")==null){
                        if (ServerAddr.equals("www.dev.pcep.cloud")) {
                            cm.put("DATA_GROUP", "ACTIJD100001852");
                        }else {
                            cm.put("DATA_GROUP", "APPSTLgcjs12345");
                        }
                    }
                    if (!cm.containsKey("CREATE_USER_ID") || cm.get("CREATE_USER_ID")==null){
                        cm.put("CREATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("CREATE_DATE") || cm.get("CREATE_DATE")==null){
                        cm.put("CREATE_DATE",(sdf.format(new Date())));
                    }
                    if (!cm.containsKey("CREATE_APP_ID") || cm.get("CREATE_APP_ID")==null){
                        cm.put("CREATE_APP_ID","TLM_CRP");
                    }
                    if (!cm.containsKey("UPDATE_USER_ID") || cm.get("UPDATE_USER_ID")==null){
                        cm.put("UPDATE_USER_ID",SpringManager.getCurrentUser().getUserId());
                    }
                    if (!cm.containsKey("UPDATE_DATE") || cm.get("UPDATE_DATE")==null){
                        cm.put("UPDATE_DATE",sdf.format(new Date()));
                    }
                    cm.put("taskDataId",srTaskTreeData.getId());
                }
                String ss = JSON.toJSONString(newlist);
                client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON,ss);
                request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+srTaskTreeData.getDatasetId()+"/savedatafullresult")
                        .build();
                callTask = client.newCall(request);
                response = callTask.execute();
                body = response.body();
                s= body.string();
                Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
                if ((boolean)saveMap.get("flag")){
                    srTaskTreeData.setDataContent(newlist);
                    srTaskTreeDataRepo.save(srTaskTreeData);
                    return srTaskTreeData;
                }else {
                    srTaskTreeDataRepo.delete(srTaskTreeData.getId());
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //报告邻井复用
    public List<SrTaskTreeData> beforeResults(JSONObject object,HttpServletRequest httpServletRequest) throws Exception {
        String objectId = object.get("objectId")==null?"":object.get("objectId").toString();
        String objectName = object.get("objectName")==null?"":object.get("objectName").toString();
        String workIdgb = object.get("workIdgb")==null?"":object.get("workIdgb").toString();
        String workIdfy = object.get("workIdfy")==null?"":object.get("workIdfy").toString();
        String nodeIds = object.get("nodeIds")==null?"":object.get("nodeIds").toString();//T2树id
        String projectId = CRPDBSCHEMAID;
        /*报告邻井复用：当前节点的成果、研究资料的质控数据
        //1、对象替换成研究对象（哈得1报表复用哈得2质控数据）数据都变成哈得1
        //2、多井筒查询后：有个00的主井筒标志
        //3、质控审核（已审核）的数据
        */
        //0、查询人员权限：sr_work_task
        User currentUser = SpringManager.getCurrentUser();
        QueryWrapper<WorkTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("work_id", workIdgb);
        taskWrapper.eq("charge_user_id", currentUser.getUserId());
        List<WorkTask> workTasks = workTaskService.list(taskWrapper);
        String taskId=null;
        String chargeUserId=null;
        String chargeUserName=null;
        if(workTasks.size()==0){
            taskId=null;
            chargeUserId=currentUser.getUserId();
            chargeUserName=currentUser.getDisplayName();
        }else{
            taskId=workTasks.get(0).getTaskId();
            chargeUserId=workTasks.get(0).getChargeUserId();
            chargeUserName=workTasks.get(0).getChargeUserName();
        }
        //1、查询井筒信息：CD_WELLBORE
        Map<String,Object> workObject_CD_WELLBORE=AdjacentWellsReuseUtil.getWellBore(ServerAddr,object,"CD_WELLBORE");
        //2、根据T2：nodeIds查询T3树的节点
        String [] T2nodeIds=nodeIds.split(",");
        Map<String,Object> [] nodeIdsMap= new Map[T2nodeIds.length];
        Map<String,Object> tree= null;
        for(int i=0;i<T2nodeIds.length;i++){
            tree = new HashMap();
            tree.put("T2id",T2nodeIds[i]);
            tree.put("T3id",AdjacentWellsReuseUtil.t2IdToT3Id(T2nodeIds[i],workIdgb));
            nodeIdsMap[i]=tree;
        }
        //3、获取T3树的sourceNodeId并从T0获取节点资源；
        String [] sourceNodeIds= new String[T2nodeIds.length];
        QueryWrapper<WorkNavigateTreeNode> queryWrapper0 = new QueryWrapper<>();
        if(T2nodeIds.length>0){
            queryWrapper0.in("node_id",T2nodeIds);
        }
        List<WorkNavigateTreeNode> list = workNavigateTreeNodeService.list(queryWrapper0);
        for(int i=0;i<list.size();i++){
            String sourceNodeId=list.get(i).getSourceNodeId();
            String nodeId=list.get(i).getNodeId();
            String nodeName=list.get(i).getNodeName();
            sourceNodeIds[i]=sourceNodeId;
            for(int j=0;j<nodeIdsMap.length;j++){
                String T2id=nodeIdsMap[j].get("T2id").toString();
                if(StringUtils.equals(T2id,nodeId)){
                    nodeIdsMap[j].put("sourceNodeId",sourceNodeId);
                    nodeIdsMap[j].put("nodeName",nodeName);
                }
            }
        }
        //4、获取节点资源的配置数据：authMasterDataType,authMasterDataCat,authMasterDataCat,type0,customParameters
        List<Map<String,Object>> toolList= AdjacentWellsReuseUtil.nodeResources(ServerAddr,sourceNodeIds);
        Map<String,Object> datasetMap = new HashMap<>();
        List<String> datasetIds= new ArrayList<>();
        Map<String,Object> data = null;
        Map<String,Object> extAttributes = null;
        List<String> datasetIdList = null;
        Map<String,Object> authMasterMap = null;
        Map<String,Object> resTypeMap = null;
        for (Map nodemap:nodeIdsMap){
            String sourceNodeId=nodemap.get("sourceNodeId").toString();
            String T2id=nodemap.get("T2id").toString();
            datasetIdList= new ArrayList<>();
            authMasterMap= new HashMap();
            resTypeMap= new HashMap();
            for (Map<String,Object> tool:toolList){
                if (tool.get("treeNodeId").equals(sourceNodeId)){
                    if ("result".equals(tool.get("resType")) || "dataset".equals(tool.get("resType"))){
                        extAttributes = (Map<String,Object>)JSON.parse(tool.get("extAttributes").toString());
                        String datasetId=tool.get("resId")==null?"":tool.get("resId").toString();
                        datasetIds.add(datasetId);
                        datasetIdList.add(datasetId);
                        authMasterMap.put(datasetId,extAttributes.get("authMasterDataType"));
                        resTypeMap.put(datasetId,"result".equals(tool.get("resType"))?"成果列表":"研究资料");
                    }
                }
            }
            data= new HashMap();
            data.put("datasetIdsString",StringUtils.join(datasetIdList,","));
            data.put("authMasterMap",authMasterMap);
            data.put("resTypeMap",resTypeMap);
            datasetMap.put(T2id,data);
        }
        //5、workId、节点资源、质控审核：查询复用报告的数据
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workIdfy));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                List<String> dataStatus=new ArrayList<>();
                dataStatus.add("已审核");dataStatus.add("已完成");dataStatus.add("已归档");//????测试用，后面取消注释
                if(dataStatus !=null && dataStatus.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("dataStatus"));
                    for (String node : dataStatus) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                if(datasetIds !=null && datasetIds.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String node : datasetIds) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrTaskTreeData> DataExlist= srTaskTreeDataRepo.findAll(spec);
        Map<String,Object> dataMap = null;
        JSONArray jsonArray = null ;
        //更新数据id报错：映射主键DataEx.setId("");
        List<SrTaskTreeData> DataExlist1 = JSONObject.parseArray(JSON.toJSONString(DataExlist),SrTaskTreeData.class);
        for (SrTaskTreeData DataEx:DataExlist1){
            String datasetId=DataEx.getDatasetId();
            for (Map<String,Object> nodemap:nodeIdsMap){
                String T2id=nodemap.get("T2id").toString();
                String T3id=nodemap.get("T3id").toString();
                String nodeName=nodemap.get("nodeName").toString();
                //datasetId,datasetName,viewCode,authMasterDataType
                dataMap= (Map<String,Object>) datasetMap.get(T2id);
                authMasterMap= (Map<String,Object>)dataMap.get("authMasterMap");
                resTypeMap= (Map<String,Object>)dataMap.get("resTypeMap");
                String datasetIdsString=(String) dataMap.get("datasetIdsString");
                if(datasetIdsString.contains(datasetId)){
                    String authMasterDataType=authMasterMap.get(datasetId).toString();
                    String dataTargetTypeZt=resTypeMap.get(datasetId)==null?DataEx.getDataTargetType():resTypeMap.get(datasetId).toString();
                    List<Map> DataContent=DataEx.getDataContent();
                    String fileName=DataEx.getFileName();
                    String fileNameAll=DataEx.getFileAllName()==null?"":DataEx.getFileAllName();
                    String ysName=DataEx.getObjectName();
                    jsonArray = JSONObject.parseArray(JSON.toJSONString(DataContent));
                    // 报告邻井复用:处理主单、明细数据CD_WELLBORE、CD_WELL
                    jsonArray=AdjacentWellsReuseUtil.formatTableData(authMasterDataType,workObject_CD_WELLBORE,jsonArray);
                    DataEx.setId(ShortUUID.randomUUID());
                    DataEx.setObjectId(objectId);
                    DataEx.setObjectName(objectName);
                    DataEx.setWorkId(workIdgb);
                    DataEx.setDatasetType("成果列表".equals(dataTargetTypeZt)?"file":"data");
                    DataEx.setDataTargetType(dataTargetTypeZt);
                    DataEx.setDataStatus("待提交");
                    DataEx.setNodeId(T3id);
                    DataEx.setNodeNames(nodeName);
                    DataEx.setSource("邻井复用");
                    String fileName1 = fileName.replaceAll(ysName,objectName);//成果名称：研究对象名称的替换
                    //正则表达式处理
                    String pattern = "\\([0-9]+\\)$";
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(fileName1);
                    StringBuffer stringBuffer = new StringBuffer();
                    while(m.find()){
                        System.out.println(m.group());
                        m.appendReplacement(stringBuffer,"");
                    }
                    m.appendTail(stringBuffer);
                    fileName1=stringBuffer.toString();
                    DataEx.setFileName(fileName1);
                    String fileNameAll1 = fileNameAll.replaceAll(ysName,objectName);
                    DataEx.setFileAllName(fileNameAll1);
                    DataEx.setCreateDate(new Date());
                    DataEx.setUpdateDate(new Date());
                    DataEx.setCreateUser(chargeUserId);
                    DataEx.setCreateUserName(chargeUserName);
                    DataEx.setUpdateUser(chargeUserId);
                    DataEx.setTaskId(taskId);
                    //DataEx.setBelong();//属于
                    //DataEx.setRefSourceIds();//成果追溯ID
                    //flow_track_id	flow_work_id	flow_node_name
                    //6:调用：智能推荐-引用数据的：保存方法
                    //List<Map> ListDataContent = JSONObject.parseArray(JSON.toJSONString(jsonArray),Map.class);
                    //DataEx.setDataContent(ListDataContent);
                    DataEx=saveSrTreeData2(projectId,jsonArray, DataEx,dataTargetTypeZt,httpServletRequest);
                    break;//避免2个节点包含相同的数据集，导致数据重复
                }
            }
        }
        return DataExlist1;
    }
    public synchronized SrTaskTreeData saveSrTreeData2(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                                       String dataTargetTypeZt,HttpServletRequest httpServletRequest) throws Exception {
            if(StringUtils.isEmpty(dataTargetTypeZt) || StringUtils.isEmpty(srTaskTreeData.getDataType())){
                return null;
            }
            srTaskTreeData.setDataTargetType(dataTargetTypeZt);
            //区分是成果还是资料，成果列表 研究资料
            if(StringUtils.equals("成果列表",dataTargetTypeZt)){
                List<SrTaskTreeData> list = new ArrayList<>();

                SrTaskTreeData finalSrTaskTreeData = srTaskTreeData;
                Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
                    @Override
                    public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("workId"), finalSrTaskTreeData.getWorkId()));
                        predicates.add(criteriaBuilder.equal(root.get("datasetId"), finalSrTaskTreeData.getDatasetId()));
                        predicates.add(criteriaBuilder.equal(root.get("nodeId"), finalSrTaskTreeData.getNodeId()));
                        predicates.add(criteriaBuilder.equal(root.get("firstChoice"),"A"));
                        predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                        predicates.add(criteriaBuilder.equal(root.get("datasetType"), "file"));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                    }
                };
                list= srTaskTreeDataRepo.findAll(spec);
                if (list==null || list.size()==0){
                    srTaskTreeData.setFirstChoice("A");
                    srTaskTreeData.setDatasetType("file");
                }else {
                    srTaskTreeData.setDatasetType("file");
                    SrTaskTreeData sw = list.get(0);
                    sw.setFirstChoice("C");
                    srTaskTreeDataRepo.save(sw);
                    srTaskTreeData.setFirstChoice("A");
                }
                genFileName(srTaskTreeData);
                return processSaveResuleSrTreeData(srTaskTreeData,data,projectId,httpServletRequest);
            }else{
                return processSaveMaterialSrTreeData(srTaskTreeData,data,projectId,httpServletRequest);
            }
    }
    /**
     * 相同成果名称后缀生成 (1)...(x)
     * @param srTaskTreeData
     */
    public void genFileName(SrTaskTreeData srTaskTreeData){
        //查询是否存在记录
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("WORK_ID",srTaskTreeData.getWorkId());
        queryWrapper.eq("DATASET_ID",srTaskTreeData.getDatasetId());
        queryWrapper.likeRight("FILE_NAME",srTaskTreeData.getFileName());
        Integer num = srTaskTreeDataMapper.selectCount(queryWrapper);
        if (num > 0) {
            srTaskTreeData.setFileName(srTaskTreeData.getFileName()+"("+(num)+")");
        }
    }

    @Override
    public SrWorkCollect saveSrWorkCollect(String resultId) {
        //String userId = SpringManager.getCurrentUser().getUserId();
        SrWorkCollect one1 = srWorkCollectRepo.findOne(resultId);
        if (one1 != null && one1.getBsflag().equals("Y")){
            one1.setBsflag("N");
            SrWorkCollect srWorkCollect = srWorkCollectRepo.saveAndFlush(one1);
            return srWorkCollect;
        }else {
            SrTaskTreeData one = srTaskTreeDataRepo.findOne(resultId);

            SrWorkCollect srWorkCollect = new SrWorkCollect();
            srWorkCollect.setId(one.getId());
            srWorkCollect.setFileName(one.getFileName());
            srWorkCollect.setFileUrl(one.getFileId());
            srWorkCollect.setFileSzie(one.getFileSize());
            srWorkCollect.setCollect_date(new Date());
            srWorkCollect.setUploadDate(one.getCreateDate());
            srWorkCollect.setCreateUser(one.getCreateUser());
            srWorkCollect.setCreateDate(one.getCreateDate());
            srWorkCollect.setUpdateUser(one.getUpdateUser());
            srWorkCollect.setUploadDate(one.getUpdateDate());
            srWorkCollect.setBsflag("N");
            srWorkCollect.setRemarks("");
            srWorkCollect.setWorkName(srWorkMsgRepository.getOne(one.getWorkId()).getWorkName());
            srWorkCollect.setResultType(one.getDataType());
            SrWorkCollect save = srWorkCollectRepo.save(srWorkCollect);
            return save;
        }

    }

    @Override
    public Page<SrWorkCollect> getSrWorkCollect(String fileName, Date startTime, Date endTime, Pageable pageable) {
        String userId = SpringManager.getCurrentUser().getUserId();
        Specification<SrWorkCollect> spec = new Specification<SrWorkCollect>() {
            @Override
            public Predicate toPredicate(Root<SrWorkCollect> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (startTime !=null && endTime !=null){
                    predicates.add(criteriaBuilder.between(root.get("createDate"), startTime, endTime));
                }
                if (!org.springframework.util.StringUtils.isEmpty(fileName)) {
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%" + fileName + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("createUser"),userId));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return srWorkCollectRepo.findAll(spec, pageable);
    }

    @Override
    public void deleteSrWorkCollect(String resultId) {
        SrWorkCollect one = srWorkCollectRepo.findOne(resultId);
        one.setBsflag("Y");
        srWorkCollectRepo.saveAndFlush(one);
    }

    @Override
    public List<SrTaskTreeData> getSrTreeData(String workId, String dataSetId, String key) {
        List<SrTaskTreeData> list =  new ArrayList<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(key!=null && key != ""){
                    predicates.add(criteriaBuilder.like(root.get("objectName"), "%"+key+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("datasetId"), dataSetId));
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        return list;
    }

    @Override
    public Page<SrTaskTreeData> getObjectByUserTask(Pageable page) {
        Page<SrTaskTreeData> list = new RestPageImpl<>();
        String userId = SpringManager.getCurrentUser().getUserId();
        List<String> tsakIdList = taskRepository.selectByUser(userId);
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(tsakIdList !=null && tsakIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("taskId"));
                    for (String node : tsakIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                predicates.add(criteriaBuilder.equal(root.get("dataType"),"object"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec,page);
        return list;
    }

    @Override
    public List<SrTaskTreeData> getAllObject(String nodeId,String objectId, String userId) {
        List<SrTaskTreeData> list = new ArrayList<>();
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                if(userId!=null && !userId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("createUser"), userId));
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);
        return list;
    }

    @Override
    public List<Map> getObjectContentList(String workId, String nodeId, String datasetId) {
        List<Map> maps = new ArrayList<>();
        List<SrTaskTreeDataEx> list = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(datasetId)){
            datasetIdList = Arrays.asList(datasetId.split(","));
        }else {
            return new ArrayList<>();
        }
        QueryWrapper<SrTaskTreeDataEx> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id",workId);
        queryWrapper.eq("node_id",nodeId);
        queryWrapper.eq("bsflag","N");
        if(datasetIdList.size()>0){
            queryWrapper.in("dataset_id",datasetIdList);
        }
        list = srTaskTreeDataMapper.selectList(queryWrapper);

        /*List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
                predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                if(finalDatasetIdList !=null && finalDatasetIdList.size()>0){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("datasetId"));
                    for (String node : finalDatasetIdList) {
                        in.value(node);
                    }
                    predicates.add(in);
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createDate")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        list=srTaskTreeDataRepo.findAll(spec);*/
        if (list==null || list.size()==0){
            return new ArrayList<>();
        }else {
            list = list.stream().collect(
                    collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getObjectId()))),
                            ArrayList::new));
            for (SrTaskTreeDataEx srTaskTreeData:list){
                Map map = new HashMap();
                if(StringUtils.isEmpty(srTaskTreeData.getObjectId()) || StringUtils.isEmpty(srTaskTreeData.getObjectName())){
                    continue;
                }
                map.put("objectId",srTaskTreeData.getObjectId());
                map.put("objectName",srTaskTreeData.getObjectName());
                map.put("refObject",true);
                maps.add(map);
            }
        }
        return maps;
    }

    @Override
    public List<Map> getAllTool(String id, HttpServletRequest httpServletRequest) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
//                MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
        Request request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/research/tool/fullTree?id="+id)
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        List<Map> list = (List<Map>) JSONArray.parse(s);
        List<Map<String, Object>> list1 = new ArrayList<>();
        for (Map x:list){
            if (x.get("text").equals("可视化组件")) {
                list1.addAll((Collection<? extends Map<String, Object>>) x.get("children"));
            }
        }
        setTreeMark(list1);
        return list;
    }

    @Override
    public List<Map> getToolOfDataset(String treeId, String toolId, HttpServletRequest httpServletRequest) throws IOException {
        //获取树所有最小业务单元
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
//                MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
        Request request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/objdataset/navigate/"+treeId+"/fulltree")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> map = (Map) JSON.parse(s);
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key:map.keySet()){
            if (key.equals("result")){
                list.addAll((Collection<? extends Map<String, Object>>) map.get(key));
            }
        }

        StringBuilder nodeIds = new StringBuilder();
        setToolTree(list,nodeIds);
        //遍历id去查询节点资源匹配当前工具
        String ids = nodeIds.toString();
        ids = ids.substring(0,ids.length()-1);
        RequestBody requestBody = new FormBody.Builder().add("treeNodeIds", ids).build();
        request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/objdataset/minBizUnit/getByTreeNodeIds")
                .build();
        callTask = client.newCall(request);
        response = callTask.execute();
        body = response.body();
        s= body.string();
        Map map1 = (Map) JSON.parse(s);
        List<Map> toolList = new ArrayList<>();
        toolList = (List<Map>) map1.get("result");
        List<Map> result = new ArrayList<>();
        for (Map tool:toolList){
            if (tool.get("resId").equals(toolId)){
                for (Map dataset:toolList){
                    if (dataset.get("treeNodeId").equals(tool.get("treeNodeId"))){
                        if (dataset.get("resType").equals("dataset")){
                            Map datasetValue = new HashMap();
                            String t = (String) dataset.get("extAttributes");
                            datasetValue = (Map) JSON.parse(t);
                            Map res = new HashMap();

                            res.put("datasetId",dataset.get("resId"));
                            res.put("datasetName",datasetValue.get("resName"));
                            result.add(res);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void setToolTree( List<Map<String, Object>> tree,StringBuilder stringBuilder) {
        String s = "";
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            String nodeId = (String) node.get("nodeId");

            if (subTree == null || subTree.size() == 0) {
                stringBuilder.append(node.get("sourceNodeId")+",");
                continue;
            }
            setToolTree(subTree,stringBuilder);
        }
    }

    private void setTreeMark( List<Map<String, Object>> tree) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");
            String nodeId = (String) node.get("nodeId");

            if (subTree == null || subTree.size() == 0) {
                node.put("parentName","可视化组件");
                continue;
            }
            setTreeMark(subTree);
        }
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        System.out.println("CRPDBSCHEMAID: " + CRPDBSCHEMAID);
        System.out.println("host: " + ServerAddr);
    }

    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50000, TimeUnit.MILLISECONDS)
                .readTimeout(50000, TimeUnit.MILLISECONDS)
                .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", "")
                .url("")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s = body.string();
        System.out.println(s);
        Map<String, Object> map = JSONObject.parseObject(s);
    }
    public void getSrTaskTreeDataListByIds(String[] ids, ApiResult apiResult){
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("id"));
                for (String data : ids) {
                    in.value(data);
                }
                predicates.add(in);
                Predicate[] predicateArray = new Predicate[predicates.size()];
                criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicateArray)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<SrTaskTreeData> treeDataList = srTaskTreeDataRepo.findAll(spec);
        System.out.println(treeDataList.size());
        Map<String, List<SrTaskTreeData>> collect = treeDataList.stream().collect(groupingBy(SrTaskTreeData::getDatasetId));
        List<Map<String,Object>> data = new ArrayList<>();
        Map<String,Object> dataOpt = null;
        for(String key : collect.keySet()){
            List<SrTaskTreeData> taskTreeDataList = collect.get(key);
            String s = JSON.toJSONString(taskTreeDataList.get(0));
            dataOpt = JSON.parseObject(s, Map.class);
            dataOpt.remove("dataContent");
            dataOpt.put("records",taskTreeDataList);
            data.add(dataOpt);
        }
        apiResult.setResult(data);
    }
    public void updateT3NodeOrder(List<WorkNavigateTreeNode> list,ApiResult apiResult){
        List<WorkNavigateTreeNode> updateList = new ArrayList<>();
        WorkNavigateTreeNode workNavigateTreeNode = null;
        for(WorkNavigateTreeNode node : list){
            workNavigateTreeNode = new WorkNavigateTreeNode();
            workNavigateTreeNode.setNodeId(node.getNodeId());
            workNavigateTreeNode.setSortSequence(node.getSortSequence());
            updateList.add(workNavigateTreeNode);
        }
        workNavigateTreeNodeService.updateBatchById(updateList);
        apiResult.setFlag(true);
    }

    //保存文件信息
    @Override
    public List<WorkTaskFileUpload> saveFileInfo(List<WorkTaskFileUpload> workTaskFileUploadList) {
        return workTaskFileUploadRepository.save(workTaskFileUploadList);
    }

    //查询文件
    @Override
    public Page<WorkTaskFileUpload> findByObjectIdAndFileName(String objectId, String fileName, String collectionTab, String browsingTab, String updateTab, Pageable pageable) {
        if(updateTab.equals("1")) {
          return getUpdateFileList(objectId, fileName, pageable);
        } else if (collectionTab.equals("2")) {
            return getCollectionFileList(objectId, fileName, pageable);
        } else if(browsingTab.equals("3")) {
            return getBrowsingFileList(objectId, fileName, pageable);
        } else {
            Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
                @Override
                public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    if(fileName!=null && !fileName.equals("")){
                        predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                    }
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                    criteriaQuery.orderBy(criteriaBuilder.asc(root.get("sortFlag")), criteriaBuilder.desc(root.get("createDate")));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            return workTaskFileUploadRepository.findAll(spec, pageable);
        }
    }

    /**
     * 获取同名文件
     * @param fileName 文件名
     * @return 文件信息
     */
    @Override
    public WorkTaskFileUpload findFileByObjIdAndFn(String objectId, String fileName) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRepository.findOne(spec);
    }

    //文件更新旧文件逻辑删除
    @Override
    public void updateOldFile(String uploadId, String objectId, String fileId, String fileName, String isCollection) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        WorkTaskFileUpload one = workTaskFileUploadRepository.findOne(spec);
        one.setBsflag("Y");
        workTaskFileUploadRepository.saveAndFlush(one);
        //添加更新文件收藏信息
        if (isCollection.equals("1")) {
            WorkTaskFileUploadRecord wr = new WorkTaskFileUploadRecord();
            wr.setId(ShortUUID.randomUUID());
            wr.setFileName(fileName);
            wr.setFileId(fileId);
            wr.setUploadId(uploadId);
            wr.setObjectId(objectId);
            wr.setFileFlag("0");//地址资料文件操作标识
            wr.setRecordFlag("c");
            wr.setCreateUser(SpringManager.getCurrentUser().getUserId());
            wr.setCreateDate(new Date(System.currentTimeMillis()));
            wr.setBsflag("N");
            workTaskFileUploadRecordRepository.save(wr);
        }
    }

    //文件收藏
    @Override
    public WorkTaskFileUploadRecord saveFileCollection(String objectId, String fileName) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        WorkTaskFileUpload wu = workTaskFileUploadRepository.findOne(spec);
        if (wu != null) {
            wu.setFileCollection("1");
            workTaskFileUploadRepository.saveAndFlush(wu);
            WorkTaskFileUploadRecord wr = new WorkTaskFileUploadRecord();
            wr.setId(ShortUUID.randomUUID());
            wr.setFileName(wu.getFileName());
            wr.setFileId(wu.getFileId());
            wr.setUploadId(wu.getId());
            wr.setObjectId(objectId);
            wr.setFileFlag("0");//地址资料文件操作标识
            wr.setRecordFlag("c");
            wr.setCreateUser(SpringManager.getCurrentUser().getUserId());
            wr.setCreateDate(new Date(System.currentTimeMillis()));
            wr.setBsflag("N");
            return workTaskFileUploadRecordRepository.save(wr);
        } else{
            return null;
        }
    }

    //取消收藏
    @Override
    public WorkTaskFileUploadRecord deleteFileCollection(String objectId, String fileId, String fileName, String adjacentFile) {
        if (!adjacentFile.equals("1")) {//地质资料取消收藏
            //变更文件信息收藏字段
            Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
                @Override
                public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                    predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                    predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            WorkTaskFileUpload wu = workTaskFileUploadRepository.findOne(spec);
            wu.setFileCollection("0");
            workTaskFileUploadRepository.saveAndFlush(wu);
            Specification<WorkTaskFileUploadRecord> spec1 = new Specification<WorkTaskFileUploadRecord>() {
                @Override
                public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                    predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                    predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "c"));
                    predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            WorkTaskFileUploadRecord wr = workTaskFileUploadRecordRepository.findOne(spec1);
            wr.setBsflag("Y");
            return workTaskFileUploadRecordRepository.saveAndFlush(wr);
        } else {//邻井资料取消收藏
            Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
                @Override
                public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                    predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                    predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "c"));
                    predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                    predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            WorkTaskFileUploadRecord wr = workTaskFileUploadRecordRepository.findOne(spec);
            wr.setBsflag("Y");
            return workTaskFileUploadRecordRepository.saveAndFlush(wr);
        }

    }

    //浏览记录
    @Override
    public WorkTaskFileUploadRecord saveFileBrowsing(String objectId, String fileName, String onFileId) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        WorkTaskFileUpload wu = workTaskFileUploadRepository.findOne(spec);
        if (wu.getFileBrowsing().equals("1")) {
            wu.setOnFileId(onFileId);
            workTaskFileUploadRepository.saveAndFlush(wu);
            WorkTaskFileUploadRecord workTaskFileUploadRecord = new WorkTaskFileUploadRecord();
            System.out.println(workTaskFileUploadRecord.getRecordFlag());
            return new WorkTaskFileUploadRecord();
        } else if (wu.getFileBrowsing().equals("0")){
            wu.setFileBrowsing("1");
            wu.setOnFileId(onFileId);
            workTaskFileUploadRepository.saveAndFlush(wu);
            WorkTaskFileUploadRecord wr = new WorkTaskFileUploadRecord();
            wr.setId(ShortUUID.randomUUID());
            wr.setFileName(wu.getFileName());
            wr.setFileId(wu.getFileId());
            wr.setUploadId(wu.getId());
            wr.setObjectId(objectId);
            wr.setFileFlag("0");//地址资料文件操作标识
            wr.setRecordFlag("b");
            wr.setCreateUser(SpringManager.getCurrentUser().getUserId());
            wr.setCreateDate(new Date(System.currentTimeMillis()));
            wr.setBsflag("N");
            return workTaskFileUploadRecordRepository.save(wr);
        } else {
            return null;
        }
    }


    /**
     * 获取文件列表
     * @param objectId 研究对象id
     * @return 查询分页结果
     */
    @Override
    public List<WorkTaskFileUpload> getFileList(String objectId) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("sortFlag")), criteriaBuilder.desc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRepository.findAll(spec);
    }

    //获取文件更新列表
    @Override
    public Page<WorkTaskFileUpload> getUpdateFileList(String objectId, String fileName, Pageable pageable) {
        Specification<WorkTaskFileUpload> spec = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("fileUpdate"),"1"));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                predicates.add(criteriaBuilder.isNull(root.get("onFileId")));
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("sortFlag")), criteriaBuilder.desc(root.get("createDate")));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRepository.findAll(spec, pageable);
    }

    //获取收藏列表
    @Override
    public Page<WorkTaskFileUpload> getCollectionFileList(String objectId, String fileName, Pageable pageable) {
        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "c"));
                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                predicates.add(criteriaBuilder.equal(root.get("fileFlag"),"0"));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<WorkTaskFileUploadRecord> wrList = workTaskFileUploadRecordRepository.findAll(spec);
        List<String> ids = new ArrayList<>();
        if (wrList.size() >0) {
            for (WorkTaskFileUploadRecord workTaskFileUploadRecord : wrList) {
                ids.add(workTaskFileUploadRecord.getUploadId());
            }
        }
        Specification<WorkTaskFileUpload> spec1 = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("fileCollection"), "1"));
                if(ids != null && ids.size() > 0) {
                    predicates.add(root.get("id").in(ids));
                }
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("sortFlag")), criteriaBuilder.desc(root.get("createDate")));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRepository.findAll(spec1, pageable);
    }

    //获取浏览列表
    @Override
    public Page<WorkTaskFileUpload> getBrowsingFileList(String objectId, String fileName, Pageable pageable) {
        /*Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "0"));
                predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "b"));
                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<WorkTaskFileUploadRecord> wrList = workTaskFileUploadRecordRepository.findAll(spec);
        List<String> ids = new ArrayList<>();
        if (wrList.size() >0) {
            for (WorkTaskFileUploadRecord workTaskFileUploadRecord : wrList) {
                ids.add(workTaskFileUploadRecord.getUploadId());
            }
        }*/
        Specification<WorkTaskFileUpload> spec1 = new Specification<WorkTaskFileUpload>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUpload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("fileBrowsing"), "1"));
                /*if(ids != null && ids.size() > 0) {
                    predicates.add(root.get("id").in(ids));
                }*/
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get("sortFlag")), criteriaBuilder.desc(root.get("createDate")));
//                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRepository.findAll(spec1, pageable);
    }


    /**
     * 邻井资料
     */
    public List getAdjacentFileList(String objectId, String fileName) throws Exception {
        String dataSetId = "58a027g1oriqe47f2jh24bry,3rk0a9mc1tkmfqkir5byw1rx,dwFWOsEZbenp9ApZYldEMVE4,gABSrp5X5zkMuMMKySBEGMSc," +
                "4PGB5kIn5Y1pfrU5TckACwvd,7srf77g87tem7fgowchev62k,TXZtb61zPTNQ5bRZf202042,F2rKaAQI0s3vaOTtqk26f6EB," +
                "UwJQNQf0J501em1KU1Qw9nPD,xJCc1cr5b83Iraz90W1TOe4g,KcQWzHaiYJn9ktAWw4f1lNxo,rayylgf4crua8o1dnhlvwbkm," +
                "vpnpbd60ks5d9o1gbwvwgkih,gABSrp5X7zkMuMMKySBEGMSc,6DFtshGnVZDViE8mKMSwORrY,uMvM4FUiNUriHMGONn56h0zw," +
                "TXZtb61zPTNQ5bRZf202044,1snfr4xhltxa7fd3qubb1n6v,l6xayxi81rwx83u4sqi09icf,wwd0o2ql0sc4e1zrva494l6i," +
                "40losqmoft2udmsjtha3luha,Q0Z9E4r2PNLIvuPTftVLd01N,xtjn0HOJpq9U2plireI7sx1y,8h6lo5698tec83c8hcnhkm3x," +
                "38lcp33rctkra7d2rgnx0qjr,GBCWD2eBo9jPsWn5eU1A28sg,PVYXsSWCvjY0u3iW9neLhUZA,q4ve25ppsu59aqvdgvq2m3o7," +
                "4kjsyizrkt0p5uhwrapfw48y";
        List<String> list = Arrays.asList(dataSetId.split(","));
        List<Object> adjacentFileList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String token = HeaderTool.getToken();
//            String token = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJsb2dpbl9uYW1lIjoiODY0NjIyNzMiLCJ1c2VyX2lkIjoiM2kxeGhkeDU5cnkyNHJnZWUzNHM5d3dzIiwib3JnYW5pemF0aW9uIjoiT1JHQVRMMTEwMDA0NzA5IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLnn7Pnjq7ku5EiLCJyZWdpb24iOiJUTCIsImlhdCI6MTY2OTAwNjIzNCwiZXhwIjoxNjcxNTk4MjM0fQ.FilW0gAO3xwBDqAiUw5uRdY2DcGAKT2oidnKpovCTq_zT7B9cSeHjis1wrmXSye-CUVBjlWafRi21Zs7KeLQ95TIxh5bk24pbaHBSQmZyYyuKOa3WFKf079YBcDlKAUDCuDqVZiQCmcmHRKRNzHLcaiFNwEnshK1rFJGTX7o6ctpoFT3117P9e10uPUd9BezL83Kx6V0rWWwWa6Xtl7st0xVOxX2isPFTcvfzT7vA4iPcqGXVjheD9ytnu6xrGrO_csVcgIuIRZ_Gwt4Kb7JZR5anNHUMfu2Hi_dbYG2MkThcSm3G8za1AsFLWWrYRDGhQrAJxGXhe6J4uCJiMP7LQ";
            String ss = "{ \"subFilter\": [" +
                    "{\"key\": \"WELL_ID\",\"logic\": \"AND\",\"realValue\": [\"" + objectId + "\"],\"symbol\": \"=\"}" +
                    "] }";
//            String url = "http://www.dev.pcep.cloud/sys/dataservice/fs/"+list.get(i);
//            String url = "http://www.pcep.cloud/sys/dataservice/fs/"+list.get(i);
            String url = "http://" + ServerAddr + "/sys/dataservice/fs/"+list.get(i);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(60000, TimeUnit.MILLISECONDS)
                    .build();
            MediaType json = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json,ss);
            Request request = new Request.Builder()
                    .post(requestBody)
                    .header("Authorization",token)
                    .url(url)
                    .build();
            Date date=new Date();
            SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String id=df.format(date);
            System.out.println("----["+id+"]查询井筒信息开始请求接口---------");
            System.out.println("----["+id+"]url:"+url);
            System.out.println("----["+id+"]body:"+ss);
            System.out.println("----["+id+"]Authorization:"+token);
            Call callTask = client.newCall(request);
            Response response = callTask.execute();
            ResponseBody body = response.body();
            String s= body.string();
//            System.out.println("----["+id+"]接口返回:"+s);
            List<Object> list1 = new ArrayList<>();
            JSONArray jsonArray1 = new JSONArray();
            Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(saveMap.get("content")));
//            System.out.println("jsonArray" + jsonArray);
            if (jsonArray != null) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    String fileName1 = jsonArray.getJSONObject(j).get("file_name").toString();
                    if (!fileName.equals("") && fileName1.contains(fileName)) {
                        String fileId = jsonArray.getJSONObject(j).get("file_id").toString();
                        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
                            @Override
                            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                                List<Predicate> predicates = new ArrayList<>();
                                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                                predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "1"));
                                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                            }
                        };
                        List<WorkTaskFileUploadRecord> wr = workTaskFileUploadRecordRepository.findAll(spec);
                        if (wr.size() > 0) {
                            for (WorkTaskFileUploadRecord wrr: wr) {
                                if (wrr.getRecordFlag().equals("c")) {
                                    jsonArray.getJSONObject(j).put("isCollection", "1");
                                } else if (wrr.getRecordFlag().equals("b")) {
                                    jsonArray.getJSONObject(j).put("isBrowsing", "1");
                                }
                            }
                        } else {
                            jsonArray.getJSONObject(j).put("isCollection", "0");
                            jsonArray.getJSONObject(j).put("isBrowsing", "0");
                        }
    //                    list1.add(JSONArray.parseArray(jsonArray.getJSONArray(j).toJSONString(), Object.class));
                        jsonArray1.add(jsonArray.getJSONObject(j));
                        list1 = JSONArray.parseArray(jsonArray1.toJSONString(), Object.class);
                    } else if (fileName.equals("")){
                        String fileId = jsonArray.getJSONObject(j).get("file_id").toString();
                        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
                            @Override
                            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                                List<Predicate> predicates = new ArrayList<>();
                                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                                predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "1"));
                                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                            }
                        };
                        List<WorkTaskFileUploadRecord> wr = workTaskFileUploadRecordRepository.findAll(spec);
                        if (wr.size() > 0) {
                            for (WorkTaskFileUploadRecord wrr: wr) {
                                if (wrr.getRecordFlag().equals("c")) {
                                    jsonArray.getJSONObject(j).put("isCollection", "1");
                                } else if (wrr.getRecordFlag().equals("b")) {
                                    jsonArray.getJSONObject(j).put("isBrowsing", "1");
                                }
                            }
                        } else {
                            jsonArray.getJSONObject(j).put("isCollection", "0");
                            jsonArray.getJSONObject(j).put("isBrowsing", "0");
                        }
                        list1 = JSONObject.parseArray(jsonArray.toJSONString(), Object.class);
                    }
                }
            } else {
                return adjacentFileList;
            }
            if (list1.size() > 0) {
                adjacentFileList.addAll(list1);
            }
        }
        return adjacentFileList;
    }

    //邻井文件查询
    public List searchAdjacentFileList (String objectId, String fileName, String collectionTab, String browsingTab) throws Exception {
        if (collectionTab.equals("1")) {
            return getAdjCollectionFileList(objectId, fileName);
        } else if(browsingTab.equals("2")) {
            return getAdjBrowsingFileList(objectId, fileName);
        } else {
            return getAdjacentFileList(objectId, fileName);
        }
    }


    //邻井文件收藏
    @Override
    public WorkTaskFileUploadRecord saveAdjacentFileCollection(String objectId, String fileName, String fileId, String fileState, String fileUploadUser, String createDate) {
            WorkTaskFileUploadRecord wr = new WorkTaskFileUploadRecord();
            wr.setId(ShortUUID.randomUUID());
            wr.setFileName(fileName);
            wr.setFileId(fileId);
            wr.setObjectId(objectId);
            wr.setFileState(fileState);
            wr.setFileFlag("1");//邻井资料文件记录标识
            wr.setRecordFlag("c");
            wr.setCreateUser(SpringManager.getCurrentUser().getUserId());
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(!createDate.equals("")) {
                Timestamp date = new Timestamp(sf.parse(createDate).getTime());
                wr.setCreateDate(date);
            }
        } catch (Exception e){
                e.printStackTrace();
            }
            wr.setBsflag("N");
            return workTaskFileUploadRecordRepository.save(wr);
    }


    //获取邻井资料收藏文件列表
    public List getAdjCollectionFileList(String objectId, String fileName) {
        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "1"));
                predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "c"));
                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRecordRepository.findAll(spec);
    }

    //邻井资料浏览记录
    @Override
    public WorkTaskFileUploadRecord saveAdjacentFileBrowsing(String objectId, String fileName, String fileId, String fileState, String fileUploadUser, String createDate) {
        //查询当前文件是否已经生成浏览记录
        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                predicates.add(criteriaBuilder.equal(root.get("fileName"), fileName));
                predicates.add(criteriaBuilder.equal(root.get("fileId"), fileId));
                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "1"));
                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "b"));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        WorkTaskFileUploadRecord wur = workTaskFileUploadRecordRepository.findOne(spec);
        if (wur != null) {
            return new WorkTaskFileUploadRecord();
        } else{
            WorkTaskFileUploadRecord wr = new WorkTaskFileUploadRecord();
            wr.setId(ShortUUID.randomUUID());
            wr.setFileName(fileName);
            wr.setFileId(fileId);
            wr.setObjectId(objectId);
            wr.setFileState(fileState);
            wr.setFileFlag("1");//邻井资料文件记录标识
            wr.setRecordFlag("b");
            wr.setCreateUser(SpringManager.getCurrentUser().getUserId());
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if(!createDate.equals("")) {
                    Timestamp date = new Timestamp(sf.parse(createDate).getTime());
                    wr.setCreateDate(date);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            wr.setBsflag("N");
            return workTaskFileUploadRecordRepository.save(wr);
        }
    }

    //获取邻井资料浏览文件列表
    public List getAdjBrowsingFileList(String objectId, String fileName) {
        Specification<WorkTaskFileUploadRecord> spec = new Specification<WorkTaskFileUploadRecord>() {
            @Override
            public Predicate toPredicate(Root<WorkTaskFileUploadRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%"+fileName+"%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("fileFlag"), "1"));
                predicates.add(criteriaBuilder.equal(root.get("recordFlag"), "b"));
                predicates.add(criteriaBuilder.equal(root.get("createUser"), SpringManager.getCurrentUser().getUserId()));
                predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return workTaskFileUploadRecordRepository.findAll(spec);
    }
}
