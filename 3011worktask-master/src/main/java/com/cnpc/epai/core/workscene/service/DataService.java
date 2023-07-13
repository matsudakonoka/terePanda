package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.commom.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.springframework.http.HttpHeaders.ACCEPT;

@Service
public class DataService {

    @Value("${epai.domainhost}")
    private String gateway;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getXreList(String ids, String type) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if("储量区块".equals(type)) {
            String [] idss=ids.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://").append(gateway).append("/research/geologicalengineering/attentionMultiObject/getXreListByIds")
//            stringBuilder.append("http://localhost:8080/research/geologicalengineering/attentionMultiObject/getXreListByIds")
//                .append(ids);
//                .append("?ids[]=").append(idss)
            ;
            final String url = stringBuilder.toString();
            List<String> ints = Arrays.asList(idss);
//            JSONObject valueMap = new JSONObject();
//            valueMap.put("key", key);
//            valueMap.put("logic", "AND");
//            valueMap.put("realValue", realValue);
//            valueMap.put("symbol", "IN");
            HttpEntity<String> entity = new HttpEntity<>(ints.toString(), headers());
//            HttpEntity<String> entity = new HttpEntity<>(valueMap.toJSONString(), headers());
            ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                    entity, JSONObject.class);
            JSONObject result = response.getBody();
            list= (List<Map<String, Object>>) result.get("result");
        }else if("圈闭".equals(type)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://").append(gateway).append("/research/trap/findtrapbytrapnamepage")
                    .append("?page=0&size=999&trapYear=")
                    .append("&ids=").append(ids);
            final String url = stringBuilder.toString();
            HttpEntity<String> entity = new HttpEntity<>(headers());

            ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                    entity, JSONObject.class);
            JSONObject result = response.getBody();
            list= (List<Map<String, Object>>) result.get("content");
        }
        return list;
    }
    public List<Map<String, Object>> getData(String mtId, String key, String singleValue) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway).append("/sys/masterdata/masterdata/{mtId}/getpagedata")
                .append("?key=").append(key)
                .append("&singleValue=").append("%").append(singleValue).append("%")
                .append("&symbol=like");
        final String url = stringBuilder.toString();
        HttpEntity<String> entity = new HttpEntity<>(headers());

        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, JSONObject.class, mtId);
        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("content");
    }
    public List<Map<String, Object>> getDocument(String projectId, String userId, String dataStatus,
                                                 String startDate, String endDate) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway).append("/core/document/{projectId}/byCondition/")
                .append("&userId=").append(userId)
                .append("&status=").append(dataStatus)
                .append("&startDate=").append(startDate)
                .append("&endDate=").append(endDate);
        final String url = stringBuilder.toString();
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, JSONObject.class, projectId);
        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("content");
    }

    public List<Map<String, Object>> getChildData(String mtId, String key, List<String> realValue) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway).append("/sys/masterdata/masterdata/{mtId}/getpagedata.json?number=0&size=10000");
        final String url = stringBuilder.toString();

        JSONObject valueMap = new JSONObject();
        valueMap.put("key", key);
        valueMap.put("logic", "AND");
        valueMap.put("realValue", realValue);
        valueMap.put("symbol", "IN");

        HttpEntity<String> entity = new HttpEntity<>(valueMap.toJSONString(), headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                entity, JSONObject.class, mtId);

        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("content");
    }

    public List<Map<String, Object>> searchData(String projectId, String datasetId) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway).append("/core/dataset/project/")
                .append(projectId).append("/").append(datasetId).append("/searchdata");
        final String url = stringBuilder.toString();

        JSONObject valueMap = new JSONObject();
        /*valueMap.put("key", key);
        valueMap.put("logic", "AND");
        valueMap.put("realValue", realValue);
        valueMap.put("symbol", "IN");*/

        HttpEntity<String> entity = new HttpEntity<>(valueMap.toJSONString(), headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                entity, JSONObject.class);

        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("content");
    }

    public List<Map<String, Object>> searchData(String projectId, String datasetId, String param) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway).append("/core/dataset/project/")
                .append(projectId).append("/").append(datasetId).append("/searchdata?param[]=")
                .append(param).append("&number=0&size=1000");
        final String url = stringBuilder.toString();

        JSONObject valueMap = new JSONObject();

        HttpEntity<String> entity = new HttpEntity<>(valueMap.toJSONString(), headers());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                entity, JSONObject.class);

        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("content");
    }

    public Object search(String projectId) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://www.dev.pcep.cloud/core/objdataset/")
                .append(projectId)
                .append("/qd91ofipfsw369gzrsc0agqh/search");
        String url = urlBuilder.toString();

        JSONObject valueMap = new JSONObject();
        HttpEntity<String> entity = new HttpEntity<>(valueMap.toJSONString(), headers());
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, entity, JSONObject.class);
        return response.getBody().get("result");
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
