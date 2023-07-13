package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ShortUUID;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.worktask.domain.SrPersonalTreedata;
import com.cnpc.epai.core.worktask.repository.PersonalTreedataRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonalTreedataServiceImpl implements PersonalTreedataService{
    @Autowired
    private PersonalTreedataRepository repository;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Override
    public boolean saveTreeData(JSONArray data) {
        SrPersonalTreedata byUserId = repository.findByUserIdAndBsflag(SpringManager.getCurrentUser().getUserId(),"N");
        try {
            if (byUserId!=null){
                byUserId.setTreeData(data.toJSONString());
                byUserId.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                byUserId.setUpdateDate(new Date(System.currentTimeMillis()));
                repository.saveAndFlush(byUserId);
            }else {
                SrPersonalTreedata srPersonalTreedata = new SrPersonalTreedata();
                srPersonalTreedata.setId(ShortUUID.randomUUID());
                srPersonalTreedata.setUserId(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setTreeData(data.toJSONString());
                srPersonalTreedata.setRemarks("");
                srPersonalTreedata.setBsflag("N");
                srPersonalTreedata.setCreateUser(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setCreateDate(new Date(System.currentTimeMillis()));
                srPersonalTreedata.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setUpdateDate(new Date(System.currentTimeMillis()));
                repository.save(srPersonalTreedata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public JSONArray getTreeData() {
        SrPersonalTreedata byUserId = repository.findByUserIdAndBsflag(SpringManager.getCurrentUser().getUserId(),"N");
        if (byUserId!=null){
            return JSONArray.parseArray(byUserId.getTreeData());
        }
        System.out.println("查询测试");
        return new JSONArray();
    }

    @Override
    public boolean matchNodeDataset(JSONArray data, HttpServletRequest request) {
        SrPersonalTreedata byUserId = repository.findByUserIdAndBsflag(SpringManager.getCurrentUser().getUserId(),"N");
        JSONArray jsonArray = matchDataset(data);
        List<JSONArray> jsonArrays = jsonArray.toJavaList(JSONArray.class);

        ArrayList<JSONObject> treeData2 = new ArrayList<>();
        for (JSONArray array : jsonArrays) {
            List<JSONObject> treeData1 = array.toJavaList(JSONObject.class);
            for (JSONObject treeData : treeData1) {
                treeData2.add(treeData);
            }
        }

        for (JSONObject jsonObject : treeData2) {
            JSONArray nodeDataset = getNodeDataset(request, jsonObject.getString("sourceNodeId"));
            if (nodeDataset!=null&&nodeDataset.size()>0){

                JSONArray rejsonarray = new JSONArray();
                List<Data> dataList = nodeDataset.toJavaList(Data.class);
                for (Data data1 : dataList) {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("nodeId",ShortUUID.randomUUID());
                    jsonObject1.put("nodeType","DATASET");
                    String extAttributes = data1.getExtAttributes();
                    JSONObject jsonObject2 = JSONObject.parseObject(extAttributes);
                    jsonObject1.put("nodeName",jsonObject2.getString("name"));
                    jsonObject1.put("pnodeId",jsonObject.getString("nodeId"));
                    jsonObject1.put("datasetId",data1.getResId());
                    rejsonarray.add(jsonObject1);
                }

                jsonObject.put("children",rejsonarray);
            }
        }

        ArrayList<JSONObject> list = new ArrayList<>();
        Iterator<JSONObject> iterator = treeData2.iterator();
        while (iterator.hasNext()){
            JSONObject next = iterator.next();
            if (next.getJSONArray("children").size()>0){
                list.add(next);
            }
        }

        try {
            if (byUserId!=null){
                byUserId.setTreeData(list.toString());
                byUserId.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                byUserId.setUpdateDate(new Date(System.currentTimeMillis()));
                repository.saveAndFlush(byUserId);
            }else {
                SrPersonalTreedata srPersonalTreedata = new SrPersonalTreedata();
                srPersonalTreedata.setId(ShortUUID.randomUUID());
                srPersonalTreedata.setUserId(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setTreeData(list.toString());
                srPersonalTreedata.setRemarks("");
                srPersonalTreedata.setBsflag("N");
                srPersonalTreedata.setCreateUser(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setCreateDate(new Date(System.currentTimeMillis()));
                srPersonalTreedata.setUpdateUser(SpringManager.getCurrentUser().getUserId());
                srPersonalTreedata.setUpdateDate(new Date(System.currentTimeMillis()));
                repository.save(srPersonalTreedata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public JSONObject wellDatasetData(String datasetId, JSONArray wellNames, HttpServletRequest request, Pageable pageable) {
        String viewCode = searchDataSet(datasetId, request);

        String body = "{ \"dataRegions\": [ \"TL\" ], \"filter\": { \"key\": \"WELL_COMMON_NAME\", \"logic\": \"AND\", \"realValue\": "+wellNames+", \"symbol\": \"IN\" }, \"sort\":{\"WELLBORE_LABEL\":\"ASC\"} }";

        System.out.println(body);
        String url = "http://"+ServerAddr+"/sys/dataservice/UDB/"+viewCode+"?page="+pageable.getPageNumber()+"&size="+pageable.getPageSize();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        JSONObject jsonObject = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        return jsonObject;
    }

    private JSONArray matchDataset(JSONArray jsonArray){
        JSONArray retuJsonarray = new JSONArray();
        List<TreeData> trees = jsonArray.toJavaList(TreeData.class);

        for (TreeData tree : trees) {
            List<TreeData> retree = retree(tree);
            if (retree.size()>0){
                retuJsonarray.add(retree);
            }
        }
        return retuJsonarray;
    }

    private List<TreeData> retree(TreeData tree){
        ArrayList<TreeData> relist = new ArrayList<>();
        TreeData[] children = tree.getChildren();
        List<TreeData> treeDatas = Arrays.stream(children).collect(Collectors.toList());
        if (treeDatas.size()>0){
            for (TreeData treeData : treeDatas) {
                if (treeData.getNodeType().equals("FOLDER")){
                    List<TreeData> retree = retree(treeData);
                    for (TreeData object : retree) {
                        relist.add(object);
                    }
                }else if(treeData.getNodeType().equals("WORKUNIT")){
                    relist.add(treeData);
                }
            }
        }
        return relist;
    }

    private JSONArray getNodeDataset(HttpServletRequest request,String nodeId){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        //ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://"+ServerAddr+"/core/objdataset/minBizUnit/"+nodeId+"/get", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://www.crp.tlm.pcep.cloud/core/objdataset/minBizUnit/"+nodeId+"/get", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);

        Map<String,Object> body = exchange.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        if (trees.size()>0){
            for (Map<String, Object> tree : trees) {
                if (tree.get("resType").equals("dataset")){
                    //System.out.println(tree);
                    JSONObject jsonObject = new JSONObject(tree);
                    //System.out.println(jsonObject);
                    JSONArray resInfo = jsonObject.getJSONArray("resInfo");
                    System.out.println(resInfo);
                    return resInfo;
                }
            }
        }
        return null;
    }

    private String searchDataSet(String datasetId, HttpServletRequest request){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://"+ServerAddr+"/core/dataset/"+datasetId+"?filterDisplay=true", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);

        Map<String,Object> body = exchange.getBody();
        System.out.println(body);
        Object viewCode = body.get("viewCode");
        return viewCode.toString();
    }

    public static void main(String[] args) {
        /*RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtZW5nbGluZ2ppYW4yMDA4IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrZ_ku6Tnrq0iLCJhdXRob3JpdGllcyI6WyJST0xFX0xFQURFUiJdLCJjbGllbnRfaWQiOiJ3ZWJhcHAiLCJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOCIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTiIsInNjb3BlIjpbIm9wZW5pZCJdLCJvcmdhbml6YXRpb24iOiJPUkdBSkQxMDAwMDAwMjMiLCJleHAiOjE2NTMwNDAxNTQsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjUwNDQ4MTU0Njc4LCJqdGkiOiJlYzRmMzE3Mi0yNzA5LTQ1NWMtODc0OS0wZDk0NGVlNjJlNDMifQ.kcdhIAL85y7l3nWC3J_5Ae9Ptdk6FqnkRevaw1d8F9LhuseWSnFwSFk27YRyLc3TxUwL9VLeIRtf7vr2YQbHjgdXqdETk7hAPCNnIFfcL2EISkmQ2JuyD2XkyhWAOuI_Vymp8SYlEGIbselISsh31Va4LM4EHxafOV8mqeeNP_MDN1D7pBQk7OlAjqdc4x9hya7zM60xTzSDOjKpRGSKTQP0tMJ9oAtMolHY9nUVCTTIiAgY_0MJJ6po4ZdrotZPH5Xrc3l3pP60Z-TqLjA2a78iPoxIkw2S3LmxofX8RskA64OesJZfv1G1msWRUOP4IEgDG-13DH2iDpX_MXuIrA");
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://www.crp.tlm.pcep.cloud/core/objdataset/minBizUnit/76afe35b1e3be0157ec1d6a31b8dd375/get", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);

        Map<String,Object> body = exchange.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        if (trees.size()>0){
            for (Map<String, Object> tree : trees) {
                if (tree.get("resType").equals("dataset")){
                    //System.out.println(tree);
                    JSONObject jsonObject = new JSONObject(tree);
                    //System.out.println(jsonObject);
                    JSONArray resInfo = jsonObject.getJSONArray("resInfo");
                    System.out.println(resInfo);
                }
            }
        }*/
        /*RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtZW5nbGluZ2ppYW4yMDA4IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrZ_ku6Tnrq0iLCJhdXRob3JpdGllcyI6WyJST0xFX0xFQURFUiJdLCJjbGllbnRfaWQiOiJ3ZWJhcHAiLCJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOCIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTiIsInNjb3BlIjpbIm9wZW5pZCJdLCJvcmdhbml6YXRpb24iOiJPUkdBSkQxMDAwMDAwMjMiLCJleHAiOjE2NTMwNDAxNTQsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjUwNDQ4MTU0Njc4LCJqdGkiOiJlYzRmMzE3Mi0yNzA5LTQ1NWMtODc0OS0wZDk0NGVlNjJlNDMifQ.kcdhIAL85y7l3nWC3J_5Ae9Ptdk6FqnkRevaw1d8F9LhuseWSnFwSFk27YRyLc3TxUwL9VLeIRtf7vr2YQbHjgdXqdETk7hAPCNnIFfcL2EISkmQ2JuyD2XkyhWAOuI_Vymp8SYlEGIbselISsh31Va4LM4EHxafOV8mqeeNP_MDN1D7pBQk7OlAjqdc4x9hya7zM60xTzSDOjKpRGSKTQP0tMJ9oAtMolHY9nUVCTTIiAgY_0MJJ6po4ZdrotZPH5Xrc3l3pP60Z-TqLjA2a78iPoxIkw2S3LmxofX8RskA64OesJZfv1G1msWRUOP4IEgDG-13DH2iDpX_MXuIrA");
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://www.dev.pcep.cloud/core/dataset/Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0?filterDisplay=true", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);

        Map<String,Object> body = exchange.getBody();
        System.out.println(body);*/
        String body = "{\n" +
                "    \"dataRegions\": [\n" +
                "        \"TL\"\n" +
                "    ],\n" +
                "    \"filter\": {\n" +
                "        \"key\": \"WELL_COMMON_NAME\",\n" +
                "        \"logic\": \"AND\",\n" +
                "        \"realValue\": [\n" +
                "            \"哈7-10H\",\"大北11\"\n" +
                "        ],\n" +
                "        \"symbol\": \"IN\"\n" +
                "    },\n" +
                "    \"sort\":{\"WELLBORE_LABEL\":\"ASC\"}\n" +
                "}";
        String url = "http://www.dev.pcep.cloud/sys/dataservice/UDB/X_CD_WELL_SOURCE?page=0&size=200";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtZW5nbGluZ2ppYW4yMDA4IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrZ_ku6Tnrq0iLCJhdXRob3JpdGllcyI6WyJST0xFX0xFQURFUiJdLCJjbGllbnRfaWQiOiJ3ZWJhcHAiLCJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOCIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTiIsInNjb3BlIjpbIm9wZW5pZCJdLCJvcmdhbml6YXRpb24iOiJPUkdBSkQxMDAwMDAwMjMiLCJleHAiOjE2NTMwNDAxNTQsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjUwNDQ4MTU0Njc4LCJqdGkiOiJlYzRmMzE3Mi0yNzA5LTQ1NWMtODc0OS0wZDk0NGVlNjJlNDMifQ.kcdhIAL85y7l3nWC3J_5Ae9Ptdk6FqnkRevaw1d8F9LhuseWSnFwSFk27YRyLc3TxUwL9VLeIRtf7vr2YQbHjgdXqdETk7hAPCNnIFfcL2EISkmQ2JuyD2XkyhWAOuI_Vymp8SYlEGIbselISsh31Va4LM4EHxafOV8mqeeNP_MDN1D7pBQk7OlAjqdc4x9hya7zM60xTzSDOjKpRGSKTQP0tMJ9oAtMolHY9nUVCTTIiAgY_0MJJ6po4ZdrotZPH5Xrc3l3pP60Z-TqLjA2a78iPoxIkw2S3LmxofX8RskA64OesJZfv1G1msWRUOP4IEgDG-13DH2iDpX_MXuIrA");
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        JSONObject jsonObject = restTemplate.postForObject(url, httpEntity, JSONObject.class);

    }
}

@Getter
@Setter
@ToString
class TreeData{
    String nodeId;
    String treeId;
    String sourceNodeId;
    String nodeName;
    String nodeType;
    String nodeIcon;
    String targetId;
    String sortSequence;
    String remarks;
    TreeData[] children;
    String pnodeId;
    String attribute;
    boolean display;
    boolean status;
}

@Getter
@Setter
@ToString
class DataSet{
    String resType;
    Data[] resInfo;
}

@Getter
@Setter
@ToString
class Data{
    String id;
    String treeNodeId;
    String resType;
    String resId;
    String extAttributes;
    String dataRegion;
    String createUser;
    String createDate;
    String updateUser;
    String updateDate;
    String bsflag;
    int sort;
}