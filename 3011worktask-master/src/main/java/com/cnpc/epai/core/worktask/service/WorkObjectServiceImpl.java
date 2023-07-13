package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.common.util.ThreadContext;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.SrWorkCollect;
import com.cnpc.epai.core.worktask.repository.SrTaskTreeDataRepository;
import com.cnpc.epai.core.worktask.repository.SrWorkCollectRepository;
import com.cnpc.epai.core.worktask.repository.SrWorkMsgRepository;
import com.cnpc.epai.core.worktask.repository.SrWrokTaskRepository;
import com.cnpc.epai.core.worktask.util.RestPageImpl;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
public class WorkObjectServiceImpl implements WorkObjectService, ApplicationListener<ContextRefreshedEvent> {

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
    private RestTemplate restTemplate;

    @Value("${epai.crpDBSchemaID:1111}")
    private String CRPDBSCHEMAID;

    @Value("${epai.domainhost}")
    private String ServerAddr;


    @Override
    public List<SrTaskTreeData> getOrderObject(String projectId, String objectId, String objectName, Integer distance, String workId, String datasetId,String datasetType, HttpServletRequest httpServletRequest) throws IOException {
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
            RequestBody requestBody = new FormBody.Builder().add("coordinate", objectName).add("distance", distance.toString()).build();

            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", httpServletRequest.getHeader("Authorization"))
                    .url("http://" + ServerAddr + "/research/project/coordinate/findAdjacentWells?coordinate="+objectId+"&distance="+distance+"&page=0&size=10000")
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
    public List<SrTaskTreeData> saveTreeDataList(String projectId, String workId, String datasetId, String datasetName,JSONArray dataList,HttpServletRequest httpServletRequest) throws IOException {
        List<SrTaskTreeData> list = new ArrayList<>();
        List<JSONObject> content = JSONObject.parseArray(dataList.toJSONString(),JSONObject.class);
        List<Map> ll = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        StringBuffer serviceName = new StringBuffer();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//            RequestBody requestBody = new FormBody.Builder().add("filterDisplay", String.valueOf(true)).add("region","TL").build();
        Request request = new Request.Builder()
                .get()
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/core/dataset/"+datasetId+"?filterDisplay=true")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        for (JSONObject j:content){
            List<Map> list1 = (List<Map>) j.get("dataList");
            //前台处理：同步过的不在同步；delete数据追溯找不到；或者送审了
//            List<SrTaskTreeData> list2 = new ArrayList<>();
//            Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
//                @Override
//                public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
//                    List<Predicate> predicates = new ArrayList<>();
//                    predicates.add(criteriaBuilder.equal(root.get("workId"), workId));
//                    predicates.add(criteriaBuilder.equal(root.get("bsflag"),"N"));
//                    predicates.add(criteriaBuilder.equal(root.get("objectId"),j.get("objectId")));
//                    predicates.add(criteriaBuilder.equal(root.get("datasetId"), datasetId));
//                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
//                }
//            };
//            ThreadContext.putContext("currentSchema", "epai_crpadmin");
//            list2=srTaskTreeDataRepo.findAll(spec);
//            if (list2 != null && list2.size()>0){
//                srTaskTreeDataRepo.delete(list2);
//            }
            if (list1 ==null || list1.size()==0){
                SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
                srTaskTreeData.setObjectId(j.get("objectId").toString());
                srTaskTreeData.setObjectName(j.get("objectName").toString());
                srTaskTreeData.setWorkId(workId);
                srTaskTreeData.setDatasetId(datasetId);
                srTaskTreeData.setDatasetName(datasetName);
                srTaskTreeData.setNodeId(j.get("nodeId").toString());
                srTaskTreeData.setNodeNames(j.get("nodeName").toString());
                srTaskTreeData.setSource("侏罗纪");
                list.add(srTaskTreeData);
            }else {

                SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
                srTaskTreeData.setId(ShortUUID.randomUUID());
                srTaskTreeData.setObjectId(j.get("objectId").toString());
                srTaskTreeData.setObjectName(j.get("objectName").toString());
                srTaskTreeData.setWorkId(workId);
                srTaskTreeData.setDatasetId(datasetId);
                srTaskTreeData.setDatasetName(datasetName);
                srTaskTreeData.setNodeId(j.get("nodeId").toString());
                srTaskTreeData.setNodeNames(j.get("nodeName").toString());
                srTaskTreeData.setDataContent(list1);
                srTaskTreeData.setSource("侏罗纪");
                list.add(srTaskTreeData);
                ll.addAll(list1);
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (Map lm:list1){
                    lm.put("SOURCE","侏罗纪");
                    if (!lm.containsKey("DSID") || lm.get("DSID") ==null){
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
                    lm.put("taskDataId",srTaskTreeData.getId());
                }
                String ss = com.alibaba.fastjson.JSON.toJSONString(ll);
                client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
                RequestBody requestBody = RequestBody.create(JSON, ss);
                request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+datasetId+"/savedatafullresult")
                        .build();
                callTask = client.newCall(request);
                response = callTask.execute();
                body = response.body();
                s= body.string();
                Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
                if (false==(boolean)saveMap.get("flag")){
                    return null;
                }
            }
        }
        List<SrTaskTreeData> loc = srTaskTreeDataRepo.save(list);
        return loc;
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
    public Map<String,Object> getTreeDataList(String workId,String nodeId,String nodeNames,String datasetId, String objectId,String objectNames,HttpServletRequest httpServletRequest) throws IOException {
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
    public Map<String,Object> getObjectChoice(String projectId, String workId, String objectId, String nodeId, String datasetId, String status, String dataType, HttpServletRequest httpServletRequest) throws IOException {
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
                res.addAll(srTaskTreeData.getDataContent());
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
    public boolean deleteObjectList(String projectId,List<String> dataList,HttpServletRequest httpServletRequest) throws IOException {
        List<SrTaskTreeData> list = new ArrayList<>();
        list = srTaskTreeDataRepo.findAll(dataList);
        srTaskTreeDataRepo.delete(list);

        return true;
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
        }else if (firstChoice.equals("B") || firstChoice.equals("C")){
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
    public List<SrTaskTreeData> importDataList(String projectId,SrTaskTreeData srTaskTreeData,HttpServletRequest httpServletRequest) throws IOException {
        //多对象导入及保存做对象类型判断
        List<String> wellIds = srTaskTreeData.getDataContent().stream().map(o->(String)o.get("WELL_ID")).collect(Collectors.toList());
        List<SrTaskTreeData> list = new ArrayList<>();
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
        list=srTaskTreeDataRepo.findAll(spec);
        List<Map> contentMap = new ArrayList<>();
        for (SrTaskTreeData ss:list){
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
                srTaskTreeDataList.add(newSr);
            }else {
                return null;
            }
        }
        List<SrTaskTreeData> srTaskTreeDataList1 = srTaskTreeDataRepo.save(srTaskTreeDataList);
        return srTaskTreeDataList1;
    }

    @Override
    public Map<String, Object> getNewTreeDataList(String projectId,String workId,String objectId,List<Map> data,HttpServletRequest httpServletRequest) throws IOException {
        List<String> datasetIdList = new ArrayList<>();
        Map res = new HashMap();
        if (data!=null && data.size()!=0){
            for (Map mm:data){
                datasetIdList.add(String.valueOf(mm.get("datasetId")));
            }
        }
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> finalDatasetIdList = datasetIdList;
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
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
                if (null!=objectId && !objectId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("objectId"), objectId));
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
                for (SrTaskTreeData ss:groupBy.get(key)){
                    if (ss.getDataContent()!=null && ss.getDataContent().size()>0){
                        List<Map> list2 = new ArrayList<>();
                        for (Map task:ss.getDataContent()){
                            task.put("taskDataId",ss.getId());
                            task.put("SOURCE",ss.getSource());
                            list2.add(task);
                        }
                        list1.addAll(list2);
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
                if (list1.size()<=((page+1)*size)){
                    if (list1.size()==1){
                        list3 = list1;
                    }else {
                        list3 = list1.subList(page*size,list1.size());
                    }
                }else {
                    list3 = list1.subList(page*size,(page+1)*size);
                }
                Map<String,Map> idMap = new HashMap();
                for (Map dd:list3){
                    Map sour = new HashMap();
                    sour.put("taskDataId",dd.get("taskDataId"));
                    sour.put("SOURCE",dd.get("SOURCE"));
                    idMap.put((String) dd.get("DSID"),sour);
                }
                StringBuffer DSID = new StringBuffer();
                DSID.append("DSID in");
                for (Map map1:list3){
                    DSID.append(" "+map1.get("DSID"));
                }
                client = new OkHttpClient.Builder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody requestBody = RequestBody.create(JSONs, "ss");
                request = new Request.Builder()
                        .post(requestBody)
                        .header("Authorization", httpServletRequest.getHeader("Authorization"))
                        .url("http://" + ServerAddr + "/core/dataset/project/"+projectId+"/"+key+"/searchdatawithoutindex?param%5B%5D="+DSID.toString()+"&page=0&size=100000")
                        .build();
                callTask = client.newCall(request);
                response = callTask.execute();
                body = response.body();
                String ss= body.string();
                Map<String,Object> mapcs = JSONObject.parseObject(ss);
                List<Map> jian = JSON.parseArray(JSON.toJSONString(mapcs.get("content")),Map.class);
                if (jian== null || jian.size()==0){

                }else {
                    for (Map map1:jian){
                        map1.put("SOURCE",idMap.get(map1.get("DSID")).get("SOURCE"));
                        if (!map1.containsKey("taskDataId") || map1.get("taskDataId")==null || map1.get("taskDataId").equals("")){
                            map1.put("taskDataId",idMap.get(map1.get("DSID")).get("taskDataId"));
                        }
                    }
                }
                Map<String,List<Map>> map = jian.stream().collect(groupingBy(e->e.get("SOURCE").toString()));
                List<Map<String,Object>> list4 = new ArrayList<>();
                for (String keys:map.keySet()){
                    Map<String,Object> map1 = new HashMap<>();
                    List<Map> cas = new ArrayList<>();
                    cas = map.get(keys);
                    if (sort==null || sort.size()==0){
                        cas.sort(Comparator.comparing((Map h) -> ((String) h.get("CREATE_DATE"))));
                    }else {
                        for (Map kz:sort) {
                            for (Map mc : cas) {
                                Object param = mc.get(kz.get("property"));
                                if (param != null) {
                                    boolean x = false;
                                    for (Map casmap:cas){
                                        if (casmap.get(kz.get("property"))==null){
                                            x = true;
                                            break;
                                        }
                                    }
                                    if (x==true){
                                        break;
                                    }
                                    if (param instanceof Integer) {
                                        if (kz.get("direction").equals("ASC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))));
                                        } else if (kz.get("direction").equals("DESC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Integer) h.get(kz.get("property")))).reversed());
                                        }
                                        break;
                                    } else if (param instanceof String) {
                                        if (kz.get("direction").equals("ASC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))));
                                        } else if (kz.get("direction").equals("DESC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((String) h.get(kz.get("property")))).reversed());
                                        }
                                        break;
                                    } else if (param instanceof Double) {
                                        if (kz.get("direction").equals("ASC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))));
                                        } else if (kz.get("direction").equals("DESC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Double) h.get(kz.get("property")))).reversed());
                                        }
                                        break;
                                    } else if (param instanceof Boolean) {
                                        if (kz.get("direction").equals("ASC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))));
                                        } else if (kz.get("direction").equals("DESC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Boolean) h.get(kz.get("property")))).reversed());
                                        }
                                        break;
                                    } else if (param instanceof Date) {
                                        if (kz.get("direction").equals("ASC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))));
                                        } else if (kz.get("direction").equals("DESC")) {
                                            cas.sort(Comparator.comparing((Map h) -> ((Date) h.get(kz.get("property")))).reversed());
                                        }
                                        break;
                                    } else {
                                        Collections.sort(cas, Comparator.comparing(o -> Double.valueOf(o.get(kz.get("property")).toString())));
                                        break;
                                    }
                                } else {

                                }
                            }
                        }
                    }
                    map1.put("source",keys);
                    map1.put("dataContent",cas);
                    list4.add(map1);
                }
                dataMap.put("data",list4);
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
        System.out.println("workId:" + srTaskTreeData.getWorkId() + ";taskId:"+ srTaskTreeData.getTaskId() + ";nodeId:"+srTaskTreeData.getNodeId()+";objId:"+srTaskTreeData.getObjectId()+";dataSetId:"+srTaskTreeData.getDatasetId()+";data:"+data.toString());
        return saveSrTreeData(projectId, data, srTaskTreeData, httpServletRequest,null);
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
    public List<SrTaskTreeData> getObjectByYear(HttpServletRequest httpServletRequest,String nodeId,String userId) {
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
        String datasetId = httpServletRequest.getParameter("datasetId");
        String dataStatus = httpServletRequest.getParameter("dataStatus");
        String dataTargetType = httpServletRequest.getParameter("dataTargetType");
        String fileName = httpServletRequest.getParameter("fileName");
        Specification<SrTaskTreeData> spec = new Specification<SrTaskTreeData>() {
            @Override
            public Predicate toPredicate(Root<SrTaskTreeData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                //predicates.add(criteriaBuilder.between(root.get("createDate"), finalDate, new Date()));
                if(nodeId!=null && !nodeId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("nodeId"), nodeId));
                }
                if(dataTargetType!=null && !dataTargetType.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("dataTargetType"), dataTargetType));
                }
                if(fileName!=null && !fileName.equals("")){
                    predicates.add(criteriaBuilder.like(root.get("fileName"), "%" + fileName + "%"));
                }
                if(userId!=null && !userId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("createUser"), userId));
                }
                if(datasetId!=null && !datasetId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("datasetId"), datasetId));
                }
                if(dataStatus!=null && !dataStatus.equals("")){
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("dataStatus"));
                    String[] shzt=dataStatus.split(",");
                    for(int a=0;a<shzt.length;a++){
                        in.value(shzt[a]);
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
        list=srTaskTreeDataRepo.findAll(spec);
        return list;
    }
    @Override
    public List<SrTaskTreeData>  getObjectByUser(String workId, String userId, String nodeId, String dataStatus,
                                                 Date startTime, Date endTime,String datasetId) {
        return getObjectByUser(workId,userId, nodeId, dataStatus,startTime,endTime,datasetId,null,null);
    }
    @Override
    public List<SrTaskTreeData>  getObjectByUser(String workId, String userId, String nodeId, String dataStatus,
                                                 Date startTime, Date endTime,String datasetId,String dataTargetType) {
        return getObjectByUser(workId,userId, nodeId, dataStatus,startTime,endTime,datasetId,dataTargetType,null);
    }
    @Override
    public List<SrTaskTreeData>  getObjectByUser(String workId, String userId, String nodeId, String dataStatus,
                                                 Date startTime, Date endTime,String datasetId,String dataTargetType,String dataType) {
        JSONObject rtn = new JSONObject();
        StringBuffer serviceName = new StringBuffer();
        //获取所有当前用户在当前工作的成果
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> userIdList = new ArrayList<>();
        List<String> nodeIdList = new ArrayList<>();
        List<String> dataStatusList = new ArrayList<>();
        if(StringUtils.isNotBlank(userId)) {
            userIdList = Arrays.asList(userId.split(","));
        }
        if(StringUtils.isNotBlank(nodeId)) {
            nodeIdList = Arrays.asList(nodeId.split(","));
        }
        if(StringUtils.isNotBlank(dataStatus)) {
            dataStatusList = Arrays.asList(dataStatus.split(","));
        }
        List<String> finalUserIdList = userIdList;
        List<String> finalNodeIdList = nodeIdList;
        List<String> finadataStatusList = dataStatusList;
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
                if(finadataStatusList!=null && finadataStatusList.size()>0){
                    //predicates.add(criteriaBuilder.equal(root.get("dataStatus"), dataStatus));
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("dataStatus"));
                    for (String node : finadataStatusList) {
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
                if(dataTargetType!=null && !dataTargetType.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("dataTargetType"), dataTargetType));
                }
                if(dataType!=null && !dataType.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("dataType"), dataType));
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
    public Map<String,Object> getObjectBy(Pageable page, String workId,String fileName, String nodeId, String dataStatus,
                                            Date startTime, Date endTime, String datasetId,String userId,HttpServletRequest httpServletRequest) throws IOException {
        //获取所有当前用户在当前工作的成果
        List<String> nodeIdList = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(nodeId)) {
            nodeIdList = Arrays.asList(nodeId.split(","));
        }
        if(StringUtils.isNotBlank(datasetId)) {
            datasetIdList = Arrays.asList(datasetId.split(","));
        }
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
                    predicates.add(criteriaBuilder.equal(root.get("dataStatus"), dataStatus));
                }
                if(userId!=null && !userId.equals("")){
                    predicates.add(criteriaBuilder.equal(root.get("createUser"), userId));
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
                List<SrTaskTreeData> choice = new ArrayList<>();
                choice = taskTreeData.stream().filter(o->o.getFirstChoice().equals("A")).collect(Collectors.toList());
                if (choice!= null && choice.size()!=0){
                    if (choice.size()>1){
                        List<String> ids = new ArrayList<>();
                        ids = taskTreeData.stream().map(o->o.getId()).collect(Collectors.toList());
                        choice.stream().sorted(Comparator.comparing(SrTaskTreeData::getCreateDate).reversed()).collect(Collectors.toList());
                        for (int i = 0;i<choice.size();i++){
                            if (i!=0){
                                choice.get(i).setFirstChoice("C");
                            }
                        }
                        srTaskTreeDataRepo.save(choice);
                        taskTreeData = srTaskTreeDataRepo.findAll(ids);
                    }
                }
                nMap.putAll(dataMap);
                taskTreeData =
                taskTreeData = taskTreeData.stream().sorted(Comparator.comparing(SrTaskTreeData::getUpdateDate).reversed()).collect(Collectors.toList());
                nMap.put("dataList",taskTreeData);
                res.add(nMap);
            }else {
                nMap.putAll(dataMap);
                nMap.put("dataList",new ArrayList<>());
                res.add(nMap);
            }
        }
//        for (SrTaskTreeData srTaskTreeData : all) {
//            srTaskTreeData.setTaskName(srWrokTaskRepository.findOne(srTaskTreeData.getTaskId()).getTaskName());
//            SrWorkCollect one = srWorkCollectRepo.findOne(srTaskTreeData.getId());
//            if (one != null && one.getBsflag().equals("N")){
//                srTaskTreeData.setCollected(true);
//            }
//        }
        rtn.put("content",res);
        return rtn;
    }

    @Override
    public synchronized SrTaskTreeData saveSrTreeData(String projectId, JSONArray data, SrTaskTreeData srTaskTreeData,
                                         HttpServletRequest httpServletRequest,String token) {
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
            if (srTaskTreeData.getDataType().equals("非结构化")){
                for(Object o : data){
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
                    if(jsonObject.containsKey("dataContent")){
                        List<Map> dataContent = JSON.parseArray(JSON.toJSONString(jsonObject.get("dataContent")), Map.class);
                        srTaskTreeData.setDataContent(dataContent);
                        break;
                    }
                }
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
                        .header("Authorization", httpServletRequest!=null?httpServletRequest.getHeader("Authorization"):token)
                        .url("http://" + ServerAddr + "/core/document/"+projectId+"/"+srTaskTreeData.getDatasetId()+"/项目成果?data=%7B%22file_id%22%3A%22"+srTaskTreeData.getFileId()+"%22%2C%22file_name%22%3A%22"+srTaskTreeData.getFileAllName()+"%22%7D")
                         //.url("http://" + ServerAddr + "/core/document/"+projectId+"/"+srTaskTreeData.getDatasetId()+"/项目成果?data=%7B%22dsId%22%3A%22"+srTaskTreeData.getDatasetId()+"%22%2C%22file_id%22%3A%22"+srTaskTreeData.getFileId()+"%22%2C%22file_name%22%3A%22"+srTaskTreeData.getFileAllName()+"%22%2C%22newParentDirCode%22%3A%22OJz8guY8EFDDj1zbfgqKpUNJ%22%2C%22DATA_GROUP%22%3A%22ACTIJD100001852%22%2C%22APP_DOMAIN%22%3A%22CRP%22%7D")
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
                        .header("Authorization", httpServletRequest!=null?httpServletRequest.getHeader("Authorization"):token)
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
                        cm.put("DSID",dataMap.get("code")+"_"+SpringManager.getCurrentUser().getDataRegion()+"_"+UUID.randomUUID().toString().replaceAll("-", ""));
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
                        .header("Authorization", httpServletRequest!=null?httpServletRequest.getHeader("Authorization"):token)
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
        List<SrTaskTreeData> list = new ArrayList<>();
        List<String> datasetIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(datasetId)){
            datasetIdList = Arrays.asList(datasetId.split(","));
        }else {
            return new ArrayList<>();
        }
        List<String> finalDatasetIdList = datasetIdList;
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
        list=srTaskTreeDataRepo.findAll(spec);
        if (list==null || list.size()==0){
            return new ArrayList<>();
        }else {
            list = list.stream().collect(
                    collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getObjectId()))),
                            ArrayList::new));
            for (SrTaskTreeData srTaskTreeData:list){
                Map map = new HashMap();
                map.put("objectId",srTaskTreeData.getObjectId());
                map.put("objectName",srTaskTreeData.getObjectName());
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

    @Override
    public Map<String, Object> getObjectChoiceB(String projectId, String workId, String objectId, String nodeId,
                                                String datasetId, String status, String dataType, HttpServletRequest httpServletRequest) {
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
        rtn.put("content", list);
        return rtn;
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
    }

    public static void main(String[] args) throws IOException {

        String id = "aaaa";
        Map map = new HashMap();
        map.put("text","text");
        map.put("data",new ArrayList<>());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
//        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSONs, JSONObject.toJSONString(map));
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOSIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTSIsInVzZXJfbmFtZSI6Im1lbmdsaW5namlhbjIwMDkiLCJzY29wZSI6WyJvcGVuaWQiXSwib3JnYW5pemF0aW9uIjoiT1JHQUpEMTAwMDAwMDIzIiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJleHAiOjE2NTUyODI4MDQsImRpc3BsYXlfbmFtZSI6IuWtn-S7pOeurXRsIiwicmVnaW9uIjoiVEwiLCJpYXQiOjE2NTI2OTA4MDQ1NjUsImp0aSI6IjE5MTJhNDFlLTkxZWEtNDk2ZC04YjQxLTk5N2NiOTgzYjE1NCIsImNsaWVudF9pZCI6IndlYmFwcCJ9.NgKNYVpghsNJW0l89wxZIvcXmqRz7Zdd3EpKLPcRTUS2q9uhkyTZHvez8Rdb7Liop6zsIVLN1nLKjz2eZ6QejijYZkErOr1-RrlOTO9v6ms-W5peZOYoit-5wZZfeqVpywm5ehd_c45X6GA0uU2G9qYvWhIz9DPe0_9jNdFclFuU2-uKmXEoURbX1yX31XKygGBFBIipO_nZcudn3F-B1QpN2CzOkv68HAUxT4E6493chPJiyayILtxDCL1DGApbjggqy7wNgYxi-xep5CwwKfaOgXjB8c1VfHs5z3SMT6x_tTpx8hj5OBezEaesXhgUaBjnfD5r9dDknyYlVxYqqQ")
                .url("http://www.dev.pcep.cloud/research/geologicalengineering/reportStructured/saveTemplateFormula?templateId=aaaa&bookmarkId=bbbb")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s = body.string();
        System.out.println(s);
//        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50000, TimeUnit.MILLISECONDS)
//                .readTimeout(50000, TimeUnit.MILLISECONDS)
//                .build();
////        RequestBody requestBody = new FormBody.Builder().add("data", String.valueOf(dataList.get("dataList"))).build();
//        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
//        RequestBody requestBody = new FormBody.Builder().build();
//        Request request = new Request.Builder()
//                .post(requestBody)
//                .header("Authorization", "")
//                .url("")
//                .build();
//        Call callTask = client.newCall(request);
//        Response response = callTask.execute();
//        ResponseBody body = response.body();
//        String s = body.string();
//        System.out.println(s);
//        Map<String, Object> map = JSONObject.parseObject(s);
//    }
        List<String> idList = new ArrayList<>();
        if (StringUtils.isNotBlank(id)) {
            idList = Arrays.asList(id.split(","));
            System.out.println(idList);
        } else {
            System.out.println(false);
        }
    }
}

