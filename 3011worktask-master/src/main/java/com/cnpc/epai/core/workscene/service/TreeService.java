package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.*;

@Service
public class TreeService {
    @Autowired
    private WorkNavigateTreeNodeService workNavigateTreeNodeService;

    private final String serviceName = "3004objdataset";

    @Value("${epai.domainhost}")
    private String gateway;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createTreeInstance(String treeId) {
        final String url = "http://" + gateway + "/core/objdataset/navigate/{treeId}/instancetree";

        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<String>(headers()), JSONObject.class, treeId);

        JSONObject result = response.getBody();
        return (String) result.get("result");
    }

    public List<Map<String, Object>> getTree(String treeId, String nodeId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway)
                     .append("/core/objdataset/navigate/{treeId}/fulltree");
        if (nodeId != null) {
            stringBuilder.append("?nodeId=").append(nodeId);
        }

        final String url = stringBuilder.toString();
        System.out.println(url);
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, JSONObject.class, treeId);
        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("result");
    }

    public List<Map<String, Object>> getTree2(String treeId0, String pNodeId0) {
        List<Map<String,Object>>  retMaplist = new ArrayList<Map<String,Object>>();
        QueryWrapper<WorkNavigateTreeNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tree_id", treeId0);
        queryWrapper.orderByAsc("sort_sequence");
        List<WorkNavigateTreeNode> list = workNavigateTreeNodeService.list(queryWrapper);
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(list));
        //通过对象值：是地址的属性获取树
        JSONArray jsonArray1 = buildTree(jsonArray,pNodeId0);
        for(Object object :jsonArray1){
            retMaplist.add(JSON.parseObject(JSON.toJSONString(object),Map.class));
        }
        return retMaplist;
    }
    private JSONArray buildTree(JSONArray jsonArray,String pid){
        JSONArray ret = new JSONArray();
        for(Object wntn : jsonArray){
            JSONObject wntnObj = (JSONObject)wntn;
            if(StringUtils.equals(pid,wntnObj.getString("nodeId"))||                                 //枝丫节点逻辑
                    (StringUtils.isEmpty(pid) && StringUtils.isEmpty(wntnObj.getString("pNodeId")))){//根节点pid为空
                //保存根节点
                ret.add(wntnObj);
            }
            for(Object wntn2 : jsonArray){
                JSONObject wntnObj2 = (JSONObject)wntn2;
                if(wntnObj2.containsKey("pNodeId")){
                    if(StringUtils.equals(wntnObj2.getString("pNodeId"),wntnObj.getString("nodeId"))){
                        if(StringUtils.equals("WORKUNIT",wntnObj2.getString("nodeType"))){
                            wntnObj2.put("children",new ArrayList<>());
                        }
                        if(wntnObj.containsKey("children")){
                            wntnObj.getJSONArray("children").add(wntnObj2);
                        }else{
                            JSONArray array = new JSONArray();
                            array.add(wntnObj2);
                            wntnObj.put("children",array);
                        }
                    }
                }
            }
        }
        //FOLDER没有叶子节点处理
        for(Object wntn : jsonArray){
            JSONObject wntnObj = (JSONObject)wntn;
            if(StringUtils.equals("FOLDER",wntnObj.getString("nodeType"))){
                if(!wntnObj.containsKey("children")||wntnObj.get("children")==null){
                    wntnObj.put("children",new ArrayList<>());
                }
            }
        }
        return ret;
    }

    public boolean saveTree(String treeId, JSONArray tree) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway)
                .append("/core/objdataset/navigate/{treeId}/savefulltree");

        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        final String url = stringBuilder.toString();
        HttpEntity<String> entity = new HttpEntity<>(tree.toJSONString(), headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                entity, JSONObject.class, treeId);
        JSONObject result = response.getBody();
        return (boolean) result.get("flag");
    }

    public boolean updateTree(String nodeId, String name, String type, boolean attribute) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway)
                .append("/core/objdataset/navigate/").append(nodeId)
                .append("/updatenode?name=").append(name)
                .append("&type=").append(type)
                .append("&attribute=").append(attribute);
        final String url = stringBuilder.toString();
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, JSONObject.class);
        JSONObject result = response.getBody();
        return (boolean) result.get("flag");
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.set("Authorization", "Bearer " + TokenUtil.getToken());
        headers.set("Authorization", "Bearer " + TokenUtil.getToken());
        headers.set(ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
