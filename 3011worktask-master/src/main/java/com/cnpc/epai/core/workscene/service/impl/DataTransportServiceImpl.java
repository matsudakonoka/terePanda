package com.cnpc.epai.core.workscene.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.workscene.mapper.KeywordEntityMapper;
import com.cnpc.epai.core.workscene.pojo.SourceSatellite;
import com.cnpc.epai.core.workscene.service.DataService;
import com.cnpc.epai.core.workscene.service.DataTransportService;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.repository.SatellitesRepository;
import com.cnpc.epai.core.worktask.service.WorkMulitObjectService;
import com.cnpc.epai.core.worktask.util.GetSeisMapDataUtil;
import com.cnpc.epai.core.worktask.util.HeaderTool;
import com.cnpc.epai.core.worktask.util.ThreadLocalUtil;
import com.cnpc.epai.research.application.domain.InfoForTarget;
import com.richfit.epai.asi.internal.ws.DataSourceSatellite;
import com.richfit.epai.asi.servicefactory.AsiFactory;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class DataTransportServiceImpl implements DataTransportService {

    private final String KEY = "indexAndData";
    private final String JSJGSJ_DATASETID = "f6fdfcff66562d982d6e31a548f0cfcf";//井身结构数据
    private final String JGJ_DATASETID = "lRztc4SqHN3zQLVVBvPdIk5plFbb8isP";//井轨迹数据
    private final String JXJSCG_DATASETID = "eEQmE7Lmr4wX7U9VnxtVPPHggdJ7GNgj";//井斜解释成果

    private Lock lock = new ReentrantLock();

    @Autowired
    private DataService dataService;

    @Autowired
    private WorkMulitObjectService workMulitObjectService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SatellitesRepository satellitesRepository;

    @Autowired
    private RestTemplate restClient;
    @Autowired
    KeywordEntityMapper keywordMapper;
    private String apigateway = "http://3001dataset";
    @Value("${epai.domainhost}")
    private String gateway;


    @Value("${spring.datasource.password}")
    private String test;



    private final ConcurrentHashMap<String, DataWrapper>  INDEX_CACHE = new ConcurrentHashMap<>();

    @Override
    public void broadcast(JSONObject indexData) {
        System.out.println(test);
        String userId = getCurrentUserId();
        JSONObject index = JSONObject.parseObject(indexData.toJSONString(), JSONObject.class);
        DataWrapper dataWrappe = (DataWrapper) redisTemplate.opsForHash().get(KEY, userId);
        if (dataWrappe == null) {
            dataWrappe = new DataWrapper();
            INDEX_CACHE.put(userId, dataWrappe);
        }
        dataWrappe.setIndex(index);
        dataWrappe.setResult(fullData(indexData));

        redisTemplate.opsForHash().put(KEY, userId, dataWrappe);
    }

    @Override
    public Object getIndexData(JSONObject indexData) {
        String userId = getCurrentUserId();
        if (indexData != null && indexData.size() > 0) {
            return fullData(indexData);
        }
        DataWrapper dataWrapper = null;
        lock.lock();
        try {
            if (redisTemplate.hasKey(KEY) &&
                    (dataWrapper = (DataWrapper) redisTemplate.opsForHash().get(KEY, userId)) != null) {
                Object result = dataWrapper.getResult();
                redisTemplate.delete(KEY);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        System.out.println("没有待消费数据");
        return new JSONObject();
    }

    @Override
    public Object getIndex() {
        String userId = SpringManager.getCurrentUser().getUserId();
        return ((DataWrapper) redisTemplate.opsForHash().get(KEY, userId)).getIndex();
    }

    @Override
    public JSONObject sendAsi(String satelliteIdTarget,String sourceTarget,String localMac,String projectNameTarget,String dataSourceTarget,JSONObject indexData) {
        Map<String, Object> sendAsiNote = new HashMap<>();
        sendAsiNote.put(Constants.SEND_ASI_STATUS,Constants.SEND_ASI_BEGIN);
        sendAsiNote.put(Constants.SEND_WORK_TREE_DATA_IDS,new ArrayList<String>());
        ThreadLocalUtil.map(sendAsiNote);

        Object indexData1 = getIndexData(indexData);
        JSONObject index = JSON.parseObject(JSON.toJSONString(indexData1));

        //组装dataList
        JSONArray data = index.getJSONArray("data");
        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        for (int i = 0; i <data.size() ; i++) {
            JSONObject wellInfo = data.getJSONObject(i);
            JSONArray wellChildren = wellInfo.getJSONArray("children");
            wellChildren.stream().forEach(c ->{
                JSONObject c1 = (JSONObject) c;
                String datasetId = c1.getString("datasetId");
                if(CollectionUtils.isEmpty(dataMap.get(datasetId))){
                    List<Map<String, Object>> tempMap = new ArrayList<>();
                    dataMap.put(datasetId,tempMap);
                }
                List<Map<String, Object>> maps = dataMap.get(datasetId);
                List<Map<String,Object>> listObjectFir = (List<Map<String,Object>>) JSONArray.parse(c1.getJSONArray("children").toString());
                maps.addAll(listObjectFir);
                dataMap.put(datasetId,maps);
            });
        }

        JSONObject result = new JSONObject();
        String projectId = index.getString("projectId");
        String[] split = satelliteIdTarget.split(",");
        String sendAsiBatch = "0";
        for (int i = 0; i <split.length ; i++) {
            try{
                InfoForTarget infoForTarget = new InfoForTarget();
                infoForTarget.setSourceTarget(sourceTarget);
                infoForTarget.setSatelliteIdTarget(split[i]);
                infoForTarget.setProjectNameTarget(projectNameTarget);
                infoForTarget.setDataSourceCodeTarget(dataSourceTarget);
                sendAsiBatch = sendAsiBatch(projectId, dataMap, infoForTarget, localMac, "OGECRPResearch");//A6Research\OGECRPResearch\   F8-75-A4-AA-1B-46
                result.put(split[i],"1".equals(sendAsiBatch));
            }catch (Exception e){
                result.put(split[i],false);
                System.out.println("发送至："+split[i]+"出错，error: ");
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String saveObjectFromAsi(String groupID, String eoCode, String datasetId,
                                    String satelliteIdTarget, String dataSourceCodeTarget,
                                    String projectNameTarget, List<String> dataIdTargets,
                                    String objectId, String objectName,
                                    String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataTargetType,String dataBelong, String token) throws Exception {
        //surveyNameTarget surveyTypeTarget
        String result;
        result = saveSatelliteDataMap(groupID, eoCode, datasetId, satelliteIdTarget, dataSourceCodeTarget,
                projectNameTarget, dataIdTargets, objectId, objectName,
                dataSetName, workId, nodeId, nodeNames, taskId,dataTargetType, dataBelong, token);
        return result;
    }

    @Override
    public boolean broadCastFromTool(String wellId,String wellName, JSONArray dataSets, String projectId,HttpServletRequest httpServletRequest) throws IOException {
        try{
            //查询组件工具引用的所有数据
            JSONObject preChildren = new JSONObject();//key为viewCode，value为关于这个井的当前数据集的详细数据
            List<String> viewCodes = dataSets.stream().map(ds -> {
                JSONObject item = new JSONObject((LinkedHashMap)ds);
                return item.getString("viewCode");
            }).collect(Collectors.toList());
            for (int i = 0; i <viewCodes.size() ; i++) {
                String code = viewCodes.get(i);
                Map<String, Object> searchData = searchdata(code, wellName, 0, 999);
                if(searchData!=null && searchData.size()!=0){
                    Object content = searchData.get("content");
                    if (content instanceof JSONArray) {
                        preChildren.put(code,(JSONArray)content);
                    }
                }
            }

            //格式化
            JSONObject broadCastVars = new JSONObject();
            broadCastVars.put("projectId",projectId);
            broadCastVars.put("projectName","");
            broadCastVars.put("token", TokenUtil.getToken());
            broadCastVars.put("userId", SpringManager.getCurrentUser().getUserId());
            broadCastVars.put("userName", SpringManager.getCurrentUser().getDisplayName());

            JSONArray boardCastBodyA = new JSONArray();
            JSONObject wellJo = new JSONObject();
            wellJo.put("node_id","");
            wellJo.put("id",wellId);
            wellJo.put("name",wellName);
            wellJo.put("datasetId","Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0");//意向井固定值
            wellJo.put("datasetCode","1001001");//意向井code
            wellJo.put("size",viewCodes.size());//勾选了几类数据

            JSONArray childAry = new JSONArray();
            for (int i = 0; i <dataSets.size() ; i++) {
                JSONObject itemJo = dataSets.getJSONObject(i);
                String datasetId = itemJo.getString("datasetId");
                JSONObject childJo = new JSONObject();
                JSONArray childData = preChildren.getJSONArray(itemJo.getString("viewCode"));
                childJo.put("node_id","");
                childJo.put("id","");
                childJo.put("name",itemJo.getString("datasetName"));
                childJo.put("datasetId",datasetId);
                childJo.put("datasetCode",getDataSetInfo(datasetId).getEoCode());
                childJo.put("size",CollectionUtils.isEmpty(childData)?0:childData.size());
                childJo.put("children",CollectionUtils.isEmpty(childData)?new JSONArray():childData);
                childAry.add(childJo);
            }
            wellJo.put("children",childAry);
            boardCastBodyA.add(wellJo);
            broadCastVars.put("data",boardCastBodyA);

            String userId = SpringManager.getCurrentUser().getUserId();
            DataWrapper dataWrappe = (DataWrapper) redisTemplate.opsForHash().get(KEY, userId);
            if (dataWrappe == null) {
                dataWrappe = new DataWrapper();
                INDEX_CACHE.put(userId, dataWrappe);
            }
            dataWrappe.setIndex(broadCastVars);
            redisTemplate.opsForHash().put(KEY,userId , dataWrappe);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean broadCastFromSchDesign(String wellName, String projectId) throws BusinessException {
        //projectId = "ACTITL100003244";//塔里木井身结构与特殊层位研究
        String[] split = wellName.split(",");
        HashSet<String> wellNameSet = new HashSet<>();

        if(split.length>0){
            for (int i = 0; i <split.length ; i++) {
                wellNameSet.add(split[i]);
            }
            if(wellNameSet.size()>1){
                throw new BusinessException("400","请选择一个井的井身结构数据");
            }else {
                wellName = wellNameSet.iterator().next();
            }
        }else {
            throw new BusinessException("400","请先在工作室中添加井身结构数据");
        }
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJsb2dpbl9uYW1lIjoiNTA1Mjg5ODUiLCJ1c2VyX2lkIjoibXN2bjk4aW1rcnUxM3ZkYjBiMnpxNXdrIiwib3JnYW5pemF0aW9uIjoiT1JHQVRMMTAwMDAxODU5IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrovlm73lv5ciLCJyZWdpb24iOiJUTCIsImlhdCI6MTY2NDE2NDY4MCwiZXhwIjoxNjY2NzU2NjgwfQ.IUbJcSrNp78nlHySRRBIL4dQr5yAUFIPXoup0Nd64aPl6MC5NeIsqV-48La4ED68vy7Z8tyRRDJ_zuBhaf7NDnc9mzt8em_KO2H6b4eRfR3QcYM8TVA4S7ub9fJoqRxaNU0zXWJglQdWmSwObMuUuv3YGIBvxk8ewqqGqdWTYoOa2VkwKQhC9nIqz2lHQ-Wn3vLKWNKSMcmQlj6mQXYnpaeTlGVAta1r3keOQ0rL22KtNSjzkjJUTVO4tOa-hv_zsef9H91uEV-Xkd0rR1P5hak9tvv7sJ-4lho337hbsAQ0H-ECP1Frmkx39x-1mKtUxvfFft9DszpIsjs0azTehg";
        //HttpHeaders header = HeaderTool.getSimpleHeader(token);
        HttpHeaders header = HeaderTool.getSimpleHeader();
        JSONObject jsjgWellResult = findMasterDataSet(JSJGSJ_DATASETID, wellName,projectId);//井身结构数据
        JSONArray jsjgContent = jsjgWellResult.getJSONArray("content");
        if(CollectionUtils.isEmpty(jsjgContent)){
            System.out.println("查询结果 >>"+jsjgWellResult.toJSONString());
            throw new BusinessException("400","请先在工作室中添加井身结构数据");
        }
        Map jgjWellResult = findMasterDataSet(JGJ_DATASETID, wellName,projectId);//井轨迹数据
        ArrayList<HashMap> jgjContent = (ArrayList<HashMap>)jgjWellResult.get("content");
        String dsid ;

        LinkedList<HashMap> jxjscgArray = new LinkedList<>();
        if(!CollectionUtils.isEmpty(jgjContent)){
            dsid = (String)jgjContent.get(0).get("DSID");
            jxjscgArray = getSonData(JGJ_DATASETID,dsid,projectId,header);
            /*
            EAST_WEST_DISPLACE   东西向位移
            SOUTH_NORTH_DISPLACE    南北向位移
            X_AXIS  东坐标
            Y_AXIS  北坐标
            SPUDIN_NO  开钻次序
            BASE_CASING_MD  套管下深
            */
            jxjscgArray.stream().forEach(item ->{
                HashMap itemJo = (HashMap) item;
                itemJo.put("EAST_WEST_DISPLACE",itemJo.get("X_AXIS"));
                itemJo.put("SOUTH_NORTH_DISPLACE",itemJo.get("Y_AXIS"));
                itemJo.put("X_AXIS",null);
                itemJo.put("Y_AXIS",null);
            });
        }

        //去除导管
        for (int i = 0; i <jsjgContent.size() ; i++) {
            JSONObject item = jsjgContent.getJSONObject(i);
            if("导管".equals(item.getString("CASING_NAME"))){
                jsjgContent.remove(i);
            }
        }
        //井眼尺寸最大的   套管顶部深度为0
        List<Object> collect = jsjgContent.stream().sorted((i1, i2) -> {
            HashMap<String,Object> map1 = (HashMap) i1;
            HashMap<String,Object> map2 = (HashMap) i2;
            Double double1 = (Double) (null == map1.get("WELLBORE_SIZE")?0:map1.get("WELLBORE_SIZE"));
            Double double2 = (Double) (null == map2.get("WELLBORE_SIZE")?0:map2.get("WELLBORE_SIZE"));
            return Double.compare(double1, double2);
        }).collect(Collectors.toList());
        jsjgContent = JSONArray.parseArray(JSON.toJSONString(collect));
        jsjgContent.getJSONObject(jsjgContent.size()-1).put("CASING_TOP_MD",0);//套管顶部深度
        collect = jsjgContent.stream().sorted(
                Comparator.comparing((Object o) -> {//先按开钻次序升序
                    JSONObject oh = (JSONObject) o;
                    int spudin_no = Integer.parseInt((String) oh.get("SPUDIN_NO"));
                    return spudin_no;
                }).thenComparing((Object o) ->{//开钻次序相同，按照套管下深升序
                    JSONObject oh = (JSONObject) o;
                    BigDecimal base_casing_md = (BigDecimal) oh.get("BASE_CASING_MD");
                    return base_casing_md;
                })
                //.reversed()
        ).collect(Collectors.toList());
        jsjgContent = JSONArray.parseArray(JSON.toJSONString(collect));
        //格式化
        JSONObject broadCastVars = new JSONObject();
        broadCastVars.put("projectId",projectId);
        broadCastVars.put("projectName","");
        broadCastVars.put("token", TokenUtil.getToken());
        broadCastVars.put("userId", SpringManager.getCurrentUser().getUserId());
        broadCastVars.put("userName", SpringManager.getCurrentUser().getDisplayName());

        JSONObject wellJo = new JSONObject();
        wellJo.put("node_id","");
        wellJo.put("id",jsjgContent.getJSONObject(0).getString("WELL_ID"));
        wellJo.put("name",wellName);
        wellJo.put("datasetId","Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0");//井基本信息
        wellJo.put("datasetCode","1001001");//井基本信息code
        wellJo.put("size",1);//勾选了几类数据

        //井身结构数据
        JSONObject jsjgJo = new JSONObject();
        jsjgJo.put("node_id","");
        jsjgJo.put("id","");
        jsjgJo.put("name","井身结构数据");
        jsjgJo.put("datasetId",JSJGSJ_DATASETID);//井身结构数据
        jsjgJo.put("datasetCode","1003051");//井身结构数据code
        jsjgJo.put("size",jsjgContent.size());//勾选了几类数据
        jsjgJo.put("children",jsjgContent);
        //井斜解释成果
        JSONObject jsjscgJo = new JSONObject();
        jsjscgJo.put("node_id","");
        jsjscgJo.put("id","");
        jsjscgJo.put("name","井斜解释成果");
        jsjscgJo.put("datasetId",JXJSCG_DATASETID);//井斜解释成果
        jsjscgJo.put("datasetCode","1005002");//井身结构数据code
        jsjscgJo.put("size",jxjscgArray.size());//勾选了几类数据
        jsjscgJo.put("children",jxjscgArray);

        JSONArray tempArray = new JSONArray();
        tempArray.add(jsjgJo);
        tempArray.add(jsjscgJo);
        wellJo.put("children",tempArray);

        JSONArray boardCastBodyA = new JSONArray();
        boardCastBodyA.add(wellJo);
        broadCastVars.put("data",boardCastBodyA);

        String userId = SpringManager.getCurrentUser().getUserId();
        DataWrapper dataWrappe = (DataWrapper) redisTemplate.opsForHash().get(KEY, userId);
        if (dataWrappe == null) {
            dataWrappe = new DataWrapper();
            INDEX_CACHE.put(userId, dataWrappe);
        }
        dataWrappe.setResult(broadCastVars);
        redisTemplate.opsForHash().put(KEY,userId , dataWrappe);
        return  true;
    }

    private Object fullData(JSONObject indexData) {
        JSONObject originData = indexData;
        Parser parser = new Parser();
        parser.parse(originData);
        searchData(parser.id, parser.dataMap);
        return originData;
    }

    private void searchData(String pid, Map<String, List<Map<String, Object>>> dataMap) {
        Set<String> ids = dataMap.keySet();
        for (String id : ids) {
            List<Map<String, Object>> list = dataMap.get(id);
            list.clear();
        }
        for (String id : ids) {
            try {
                List<SrTaskTreeData> recordDetails = workMulitObjectService.getNewObjectDataRecordDetails(pid, Arrays.asList(id));
                SrTaskTreeData srTaskTreeData = recordDetails.get(0);
                JSONObject object = (JSONObject) JSONObject.toJSON(srTaskTreeData);
                List<Map<String, Object>> list = dataMap.get(id);
                List<Map<String, Object>> dataContent = (List<Map<String, Object>>) object.get("dataContent");
                if (dataContent != null) {
                    list.addAll(dataContent);
                }
            } catch (IOException e) {
                System.out.println();
            }
        }
    }

    @Deprecated
    private void searchData(String pid, Map<String, List<String>> dataSetMap, Map<String, Map<String, Object>> datas) {
        for (Map.Entry<String, List<String>> entry : dataSetMap.entrySet()) {
            String dataSetId = entry.getKey();
            List<String> value = entry.getValue();
            int size = value.size();
            int fragment = size / 20;
            if (fragment == 0) {
                searchData0(pid, value, dataSetId, datas);
                continue;
            }
            for (int i = 0; i < fragment; i++) {
                int index = i * 20;
                if (i == fragment - 1) {
                    searchData0(pid, value.subList(index , size), dataSetId, datas);
                } else {
                    searchData0(pid, value.subList(index, index + 20), dataSetId, datas);
                }
            }
        }
    }

    @Deprecated
    private void searchData0(String pid, List<String> values, String dataSetId, Map<String, Map<String, Object>> datas) {
        StringBuilder builder = new StringBuilder();
        builder.append("DSID in");
        for (String id : values) {
            builder.append(" ").append(id);
        }
        List<Map<String, Object>> res = dataService.searchData(pid, dataSetId, builder.toString());
        for (Map<String, Object> data : res) {
            String id = (String) data.get(Constants.DSID);
            if (datas.containsKey(id)) {
                datas.get(id).putAll(data);
            }
        }
    }

    private void searchData(String pid, Set<String> datasetIds, Map<String, Map<String, Object>> datas) {
        for (String datasetId : datasetIds) {
            List<Map<String, Object>> res = dataService.searchData(pid, datasetId);
            for (Map<String, Object> data : res) {
                String id = (String) data.get(Constants.DSID);
                if (datas.containsKey(id)) {
                    datas.get(id).putAll(data);
                }
            }
        }
    }

    private String getCurrentUserId() {
        return SpringManager.getCurrentUser().getUserId();
    }

    static class Parser {
        private String id;
        private Map<String, Map<String, Object>> datas;
        private Set<String> datasetIds;

        private Map<String, List<String>> dataSetMap;
        private Map<String, List<Map<String, Object>>> dataMap;

        Parser() {
            datas = new HashMap<>();
            datasetIds = new HashSet<>();
            dataSetMap = new HashMap<>();
            dataMap = new HashMap<>();
        }

        void parse(JSONObject indexData) {
            String pid = (String) indexData.get(Constants.PROJECT_ID);
            id = pid;
            List<Map<String, Object>> data = (List<Map<String, Object>>) indexData.get(Constants.DATA);
            doParse(data, null, null);
        }

        private void doParse(List<Map<String, Object>> data, String pDataSet, List<Map<String, Object>> pObject) {
            for (Map<String, Object> m : data) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) m.get(Constants.CHILDREN);

                String id = (String) m.get(Constants.ID);
                String datasetId = (String) m.get(Constants.DATASET_ID);
                if (children == null || children.size() == 0) {
                    if (!StringUtils.isEmpty(id)) {
                        if(ThreadLocalUtil.get(Constants.SEND_ASI_STATUS) !=null
                        && StrUtil.equals((String)ThreadLocalUtil.get(Constants.SEND_ASI_STATUS),Constants.SEND_ASI_BEGIN)){
                            ArrayList<String> sendWorkTreeDataIds = (ArrayList<String>) ThreadLocalUtil.get(Constants.SEND_WORK_TREE_DATA_IDS);
                            sendWorkTreeDataIds.add(id);
                        }
                        datas.put(id, m);
                        List<String> dataIds = dataSetMap.get(pDataSet);
                        if (dataIds == null) {
                            dataIds = new ArrayList<>();
                        }
                        dataIds.add(id);
                        dataSetMap.put(pDataSet, dataIds);
                        dataMap.put(id, pObject);
                        continue;
                    }

                }
                doParse(children, datasetId, children);
            }
        }

        @Deprecated
        private void doParse(List<Map<String, Object>> data) {
            for (Map<String, Object> m : data) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) m.get(Constants.CHILDREN);

                String id = (String) m.get(Constants.ID);
                String datasetId = (String) m.get(Constants.DATASET_ID);
                if (!StringUtils.isEmpty(id)) {
                    datas.put(id, m);
                }
                if (!StringUtils.isEmpty(datasetId)) {
                    datasetIds.add(datasetId);
                }
                if (children == null || children.size() == 0) {
                    continue;
                }
                doParse(children);
            }
        }
    }

    static class DataWrapper implements Serializable {
        private JSONObject index;
        private Object result;

        public JSONObject getIndex() {
            return index;
        }

        public void setIndex(JSONObject index) {
            this.index = index;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }

    public String sendAsiBatch(String prjId, Map<String, List<Map<String, Object>>> dataMap, InfoForTarget infoForTarget, String localMac,String satelliteId) {
        try {
            Map<String, Object> infoForSource = new HashMap<String, Object>();
            infoForSource.put("dataSourceCode", "A6DataBase");
            infoForSource.put("projectName", "塔里木协同研究");
            infoForSource.put("satelliteId", satelliteId);
            infoForSource.put("source", "DataPlatform");

            Map<String, Object> sendMap = new HashMap<String, Object>();
            sendMap.put("workroomID", prjId);
            sendMap.put("isSendable", infoForTarget.getIsSendable());
            sendMap.put("infoForTarget", infoForTarget);
            sendMap.put("infoForSource", infoForSource);
            Map<String, List> structBatchDataList = new HashMap<>();//结构化数据走批量发送，且必须包含井基本信息
            List<Map<String, Object>> noStructDataLists = new ArrayList<Map<String, Object>>();//非结构化数据走单独发送

            String otherEocode = "-1";//结构化数据除了井基本信息的eocode
            String otherDataSetId = "-1";//结构化数据除了井基本信息的dataSetId
            for (Map.Entry<String, List<Map<String, Object>>> dataEntry : dataMap.entrySet()) {
                List<Map<String, Object>> dataList = dataEntry.getValue();
                if (dataList == null || dataList.size() <= 0) {
                    continue;
                }
                String datasetId = dataEntry.getKey();
                String serviceName = String.format(apigateway+"/core/dataset/%s",datasetId);
                SrMetaDataset dataset = restClient.getForObject(serviceName, SrMetaDataset.class, datasetId);
                String dataIdKey = "";
                if ("结构化".equals(dataset.getType())) {
                    dataIdKey = "DSID";
                }
                if ("非结构化".equals(dataset.getType())) {
                    dataIdKey = "file_id";
                }
                String eocode = dataset.getEoCode();
                List<Map<String, Object>> structDataLists = new ArrayList<Map<String, Object>>();


                for (Map<String, Object> data : dataList) {
                    //有WELL_COMMON_NAME走批量发送，没有走单独发送
                    String wellName = (String) data.get("WELL_COMMON_NAME");
                    Map<String, Object> mapParam = new HashMap<String, Object>();
                    mapParam.put("projectId", prjId);
                    mapParam.put("datasetId", datasetId);
                    if(!"WELLMasterData".equals(eocode)){
                        otherEocode = eocode;
                        otherDataSetId = datasetId;
                        ArrayList<String> ids = (ArrayList<String>) ThreadLocalUtil.get(Constants.SEND_WORK_TREE_DATA_IDS);
                        mapParam.put("dsid", Constants.SEND_ASI_PRE+ids.get(0));
                    }else {
                        mapParam.put("dsid", Constants.SEND_ASI_PRE+ThreadLocalUtil.get(Constants.WELL_MASTER_DATA_ID));
                    }
                    /*if(!StringUtils.isEmpty(data.get("fileId"))){//专业软件引用的数据key为fileId
                        mapParam.put("dsid", data.get("fileId"));
                    }else {
                        mapParam.put("dsid", data.get(dataIdKey));
                    }*/
                    mapParam.put("wellName",wellName);

                    if(org.apache.commons.lang3.StringUtils.isNotEmpty(wellName)){
                        structDataLists.add(mapParam);
                        structBatchDataList.put(eocode,structDataLists);
                    }else {
                        if(!StringUtils.isEmpty(data.get("fileName"))){
                            //來自专业软件引用的数据中，key为fileName    wellLayer
                            mapParam.put("fileName", StringUtils.isEmpty(data.get("fileName")) ? "" : data.get("fileName"));
                            mapParam.put("wellLayer", StringUtils.isEmpty(data.get("wellLayer")) ? "" : data.get("wellLayer"));
                        }else{
                            mapParam.put("fileName", StringUtils.isEmpty(data.get("file_name")) ? "" : data.get("file_name"));
                            mapParam.put("wellLayer", StringUtils.isEmpty(data.get("WELL_TIMES")) ? "" : data.get("WELL_TIMES"));
                        }
                        sendMap.put("eoCode", eocode);//SEISHorizon3DDate,SEISHorizon2DDate
                        noStructDataLists.add(mapParam);
                    }
                }

//                if(org.apache.commons.lang.StringUtils.isNotEmpty(eocode)){
//                    if (structBatchDataList.containsKey(eocode)) {
//                        structDataLists.addAll(structBatchDataList.get(dataset.getEoCode()));
//                    }
//                    structBatchDataList.put(eocode, structDataLists);
//                }else {
//                    continue;
//                }
            }
            if(structBatchDataList.keySet()!=null && structBatchDataList.keySet().size() > 0){
                if(!structBatchDataList.keySet().contains("WELLMasterData")){
                    throw new BusinessException("400","结构化数据必须选择井基本信息");
                }
                if(structBatchDataList.keySet().size() > 2){
                    throw new BusinessException("400","请选择井基本信息与另一个结构化数据集");
                }
            }
            sendMap.put("batchDataList", structBatchDataList);
            sendMap.put("dataList", noStructDataLists);
            sendMap.put("isSendable","Y");
            if(!CollectionUtils.isEmpty(structBatchDataList)){
                if(structBatchDataList.keySet().size() == 1){//井基本信息的发送
                    satellitesRepository.sendAsi(sendMap, localMac, false);
                }else if(!("-1".equals(otherEocode)) && !(isSonDataSet(otherDataSetId))){
                    //父数据集发送多次
                    List parentDataL = structBatchDataList.get(otherEocode);
                    List wellMasterDataL = structBatchDataList.get("WELLMasterData");
                    System.out.println("父数据集批量发送");
                    for (int i = 0; i < parentDataL.size(); i++) {
                        Map<String, List> tempStructBatchDataList = new HashMap<>();
                        tempStructBatchDataList.put("WELLMasterData",wellMasterDataL);
                        tempStructBatchDataList.put(otherEocode, Arrays.asList(parentDataL.get(i)));
                        sendMap.put("batchDataList", tempStructBatchDataList);
                        satellitesRepository.sendAsi(sendMap, localMac, false);
                    }
                }else {//子数据集发送一次
                    List sonDataL = structBatchDataList.get(otherEocode);
                    List<String> workTreeDataIds = (List<String>) ThreadLocalUtil.get(Constants.SEND_WORK_TREE_DATA_IDS);
                    String sendWorkTaskTreeDataId = Constants.SEND_ASI_PRE+workTreeDataIds.get(0);
                    Map<String, Object> sonMap = (Map<String, Object>) sonDataL.get(0);
                    sonMap.put("dsid",sendWorkTaskTreeDataId);
                    structBatchDataList.put(otherEocode,Arrays.asList(sonMap));
                    satellitesRepository.sendAsi(sendMap, localMac, false);
                }
                ThreadLocalUtil.set(Constants.SEND_ASI_STATUS,Constants.SEND_ASI_END);
            }else if(!CollectionUtils.isEmpty(noStructDataLists)){
                //非结构化的走单独返送
                satellitesRepository.sendAsi(sendMap, localMac, true);
            }
            ThreadLocalUtil.clear();
            return "1";
        } catch (Exception e) {
            System.out.println(e.toString());
            return "-1";
        }
    }

    /**
     * 将卫星端内数据保存到研究环境数据集
     * <p>
     * saveSatelliteDataMap
     *
     * @param groupID              项目工作室ID
     * @param eoCode               数据交换实体代码
     * @param satelliteIdTarget    卫星端ID
     * @param dataSourceCodeTarget 账户配置ID
     * @param projectNameTarget    工区名称
     * @param dataIdTargets        数据ID列表
     * @param objectId              对象id
     * @param objectName            对象名称
     * @param dataSetName           数据集名称
     * @param workId                工作id
     * @param nodeId                节点id
     * @param nodeNames             节点名称
     * @param taskId                任务名称
     * @param token                 token
     * @return String
     * @return 是否保存成功
     * @throws BusinessException
     */
    public String saveSatelliteDataMap(String groupID, String eoCode, String datasetId, String satelliteIdTarget, String dataSourceCodeTarget,
                                            String projectNameTarget, List<String> dataIdTargets, String objectId, String objectName,
                                            String dataSetName, String workId, String nodeId,String nodeNames,String taskId,String dataTargetType, String dataBelong, String token) throws Exception {
        // 解释层位数据体：SEISHorizon3DDate,SEISHorizon2DDate
        // 解释断层数据体：SEISFault
        // 解释多边形数据体：SEISPolygonSet
        // 卫星端使用  geoEast201
        String dataRegion = "TL";
        AsiFactory asiFactory = getAsiFactory();
        GetSeisMapDataUtil getSeisMapDataUtil = new GetSeisMapDataUtil();
        JSONArray resultForSave = new JSONArray();
        if (eoCode.indexOf("SEIS") != -1) {
            if(org.apache.commons.lang3.StringUtils.isEmpty(dataTargetType)){
                dataTargetType = "研究资料";
            }
            if(org.apache.commons.lang3.StringUtils.isEmpty(dataBelong)){
                dataTargetType = "工程院";
            }
            nodeNames = nodeNames+"|"+dataTargetType;
            SourceSatellite satellite = getSatellite(satelliteIdTarget,asiFactory);
            String source = satellite.getSource();
            // 解释断层：SEISFault
            //if ("SEISFault".equals(eoCode) && null != source && (source.toLowerCase().contains("mgms") || source.toLowerCase().contains("ires"))) {
            if ("SEISFault".equals(eoCode)) {
                try {
                    for (String dataIdTarget:dataIdTargets) {
                        resultForSave = getSeisMapDataUtil.getSEISFaultMap(asiFactory, satelliteIdTarget, dataSourceCodeTarget, projectNameTarget, dataIdTarget, datasetId, groupID,objectId, objectName,
                                dataSetName, workId, nodeId, nodeNames, taskId, dataBelong,token);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new BusinessException("400","SEISFault保存失败");
                }
            } else if(eoCode.contains("SEISHorizon3DDate")){
                try {
                    for (String dataIdTarget:dataIdTargets) {
                        resultForSave = getSeisMapDataUtil.getSEISHorizon3DDateMap(asiFactory, satelliteIdTarget, dataSourceCodeTarget, projectNameTarget, dataIdTarget, datasetId, groupID,objectId, objectName,
                                dataSetName, workId, nodeId, nodeNames, taskId,dataBelong, token);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new BusinessException("400","SEISHorizon3DDate保存失败");
                }
            }else if(eoCode.equals("SEISPolygonSet")){
                try {
                    for (String dataIdTarget:dataIdTargets) {
                        resultForSave = getSeisMapDataUtil.getSEISPolygonSetMap(asiFactory, satelliteIdTarget, dataSourceCodeTarget, projectNameTarget, dataIdTarget, datasetId, groupID,objectId, objectName,
                                dataSetName, workId, nodeId, nodeNames, taskId,dataBelong, token);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new BusinessException("400","SEISHorizon3DDate保存失败");
                }
            }else{
                throw new BusinessException("400","暂不支持该数据类型");
            }
        }else{
            throw new BusinessException("400","暂不支持该数据类型");
        }
        return "保存成功";
    }

    private  SrMetaDataset getDataSetInfo(String dataSetId){
        String serviceName = String.format(apigateway+"/core/dataset/%s",dataSetId);
        SrMetaDataset dataset = restClient.getForObject(serviceName, SrMetaDataset.class, dataSetId);
        return dataset;
    }

    private JSONObject searchdata(String datasetCode, String wellName, Integer page, Integer size) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        String fileter = "";
        if (wellName!=null && wellName!=""){
            fileter = "{" +
                    "\"dataRegions\": [" +
                    "\"TL\"" +
                    "]," +
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
        }
        MediaType reqBody = MediaType.parse("application/json; charset=utf-8");
        RequestBody filterBody = RequestBody.create(reqBody,fileter );
        Request request = new Request.Builder()
                .post(filterBody)
                .header("Authorization", "Bearer  "+TokenUtil.getToken())
                .url("http://" + gateway + "/sys/dataservice/udb/"+datasetCode+"?page="+page+"&size="+size)
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String res = body.string();
        return JSONObject.parseObject(res);
    }


    private AsiFactory getAsiFactory(){
        String platUrl = "";
        if(gateway.contains("dev")){
            platUrl = Constants.devPlatUrl;
        }else if(gateway.contains("uat")){
            platUrl = Constants.uatPlatUrl;
        }else if(gateway.contains("tlm")){
            platUrl = Constants.tlmPlatUrl;
        }else {
            platUrl = Constants.prdPlatUrl;
        }
        System.out.println("平台端地址："+platUrl);
        AsiFactory instance = AsiFactory.createInstance(platUrl, 60000, 60000);
        return instance;
    }
    /*
     * @Description 获取卫星短信息
     * @Param id
     * @return SourceSatellite
     */
    private SourceSatellite getSatellite(String id,AsiFactory asiFactory) throws BusinessException {
        SourceSatellite sate = null;
        try {
            List<DataSourceSatellite> sateList = asiFactory.getSatellites();
            if (sateList != null) {
                for (int i = 0; i < sateList.size(); i++) {
                    if (sateList.get(i).getId().equals(id)) {
                        boolean enable = true;
//                    boolean enable = ASIServiceFactory.getAsiServerManagerInstance().
//                            getSatelliteStatus(sateList.get(i).getId());
                        sate = new SourceSatellite();
                        sate.setEnable(enable);
                        sate.setId(sateList.get(i).getId());
                        sate.setSatelliteName(sateList.get(i).getSatelliteName());
                        sate.setHostIP(sateList.get(i).getHostIP());
                        sate.setLastActive(sateList.get(i).getLastActive());
                        sate.setPort(sateList.get(i).getPort());
                        sate.setServiceUrl(sateList.get(i).getServiceUrl());
                        sate.setSource(sateList.get(i).getSource());
                        break;
                    }
                }
            } else {
                System.out.println("X1140000W003:获取的卫星端列表信息为空。");
//                throw new BusinessException(X1140000W003.getErrorCode(), X1140000W003.getErrorMessage());
            }
        } catch (Exception ex) {
            System.out.println("X1140000W004:查询发生异常：" + ex.toString());
        }
        if (sate == null) {
            System.out.println("X1140000W005:没有查询到名为" + id + "的卫星端");
//            throw new BusinessException(X1140000W005.getErrorCode(), X1140000W005.getErrorMessage());
        }
        return sate;
    }

    /**
     * 根据eoCode和测网类型获取地震EoCode
     *
     * @param eoCode           数据交换实体代码
     * @param surveyTypeTarget 测网类型
     * @return数据交换实体代码
     */
    private String getEoCodeSeis(String eoCode, String surveyTypeTarget) {
        String eoCodeSeis = "";
        if (!eoCode.contains(",")) {
            eoCodeSeis = eoCode;
        } else {
            String[] eoCodeArray = eoCode.split(",");
            boolean s2d = true;
            if (surveyTypeTarget.contains("2D")) {
                s2d = true;
            } else {
                s2d = false;
            }
            for (String oEo : eoCodeArray) {
                if (s2d) {
                    if (oEo.contains("2D")) {
                        eoCodeSeis = oEo;
                        break;
                    }
                } else {
                    if (oEo.contains("3D")) {
                        eoCodeSeis = oEo;
                        break;
                    }
                }
            }
        }
        return eoCodeSeis;
    }

    /*
     * @Description  查询主数据
     * @Param datasetId 数据集id
     * @param wellName  井名
      * @param projectId 项目id
     * @return JSONObject  查询结果
     */
    private JSONObject findMasterDataSet(String datasetId,String wellName,String projectId){
        RestTemplate restTemplate = new RestTemplate();
        String param = "WELL_COMMON_NAME = "+wellName;
        String url = String.format("http://www.pcep.cloud/core/dataset/project/%s/%s/searchdata",projectId,datasetId);
        HttpHeaders headers = new HttpHeaders();
        org.springframework.http.MediaType type = org.springframework.http.MediaType.parseMediaType("multipart/form-data");
        headers.setContentType(type);
        headers.add("Authorization", HeaderTool.getToken());
        headers.add("Accept", org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("param[]", param);
        form.add("projectId", projectId);
        form.add("page", "0");
        form.add("size", "50");
        form.add("rows", "50");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(form, headers);

        JSONObject body = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class).getBody();


        return body;
    }

    /*
     * @Description  查询子数据集
     * @Param datasetId  数据集id
     * @param dsId 主数据id
     * @param projectId 项目id
     * @param header 
     * @return List  子数据查询结果
     */
    private LinkedList<HashMap> getSonData(String datasetId,String dsId,String projectId,HttpHeaders header){
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("http://www.pcep.cloud/core/dataset/project/%s/%s/getsondata?dsid=%s",projectId,datasetId,dsId);
        HttpEntity<String> httpEntity = new HttpEntity(null,header);

        List<Map<String,Object>> body = restTemplate.exchange(url, HttpMethod.GET, httpEntity, List.class).getBody();
        LinkedList<HashMap> jxjscgList = new LinkedList<>();
        for(int j=0;j<body.size();j++){
            if(body.get(j).get("name").equals("井斜解释成果")){
                LinkedHashMap<String,Object> attributes = (LinkedHashMap)body.get(j).get("attributes");
                List data = (List)attributes.get("DATA");
                jxjscgList.addAll(data);
            }
        }
        return jxjscgList;
    }

    /*
     * @Description 判断当前数据集是否是子数据集
     * @Param dataSetId 数据集id
     * @return boolean true表示是子数据集，false表示不是
     */
    private boolean isSonDataSet(String dataSetId){
        return !(0 == keywordMapper.countDataRelation(dataSetId));
    }
}
