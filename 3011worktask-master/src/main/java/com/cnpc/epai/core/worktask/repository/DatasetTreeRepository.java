package com.cnpc.epai.core.worktask.repository;


import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DatasetTreeRepository {
    @Autowired
    private RestTemplate restTemplate;

    private String serviceName = "http://3004objdataset/core/objdataset";

    public void saveSceneBusinessTree(String taskId, String purpose, List<Map<String, Object>> nodes){
        String url = serviceName + "/navigate/"+taskId+"/"+purpose+"/saveprojecttree";
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        String result = "";
        try {
            ParameterizedTypeReference<Map<String,Object>> typeRef = new ParameterizedTypeReference<Map<String,Object>>(){};

            Map<String,Object> body = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(JSON.toJSONString(nodes), headers), typeRef).getBody();
            System.out.println(body);
        }catch (Exception e){
            result = "1";
            e.printStackTrace();
        }

    }

    public String getDatasetName(String datasetId){
        String url = serviceName + "/"+datasetId+"/info";
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        String datasetName = "";
        try {
            ParameterizedTypeReference<Map<String, Object>> ref = new ParameterizedTypeReference<Map<String, Object>>() {
            };
            Map<String, Object> result = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), ref).getBody();
            Map<String, Object> map =(Map<String, Object>)result.get("result");
            Map<String, Object> datasetMap = (Map<String, Object>)map.get("objDataset");
            if (datasetMap!=null){
                datasetName = datasetMap.get("datasetName").toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return datasetName;
    }

    public List<Map<String,Object>> getIndexList(String projectId,String fileIds,String taskId){
        List<Map<String,Object>> list = new ArrayList<>();
        String url = serviceName +"/index/getindexs?projectId="+projectId+"&fileIds="+fileIds+"&taskId="+taskId;
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        ParameterizedTypeReference<List<Map<String,Object>>> ref = new ParameterizedTypeReference<List<Map<String,Object>>>() {
        };
        try {
             list = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), ref).getBody();
        }catch (Exception e){
            list = null;
            e.printStackTrace();
        }
        return list;
    }

}
