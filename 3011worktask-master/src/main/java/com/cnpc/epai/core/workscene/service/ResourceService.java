package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.commom.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ResourceService {

    @Value("${epai.domainhost}")
    private String gateway;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getResource(String nodeId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway)
                .append("/core/objdataset/minBizUnit/getByTreeNodeIds?treeNodeIds=")
                .append(nodeId);
        final String url = stringBuilder.toString();
        System.out.println(url);
        HttpEntity<String> entity = new HttpEntity<>(HttpUtils.header());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST,
                entity, JSONObject.class);
        JSONObject result = response.getBody();
        return (List<Map<String, Object>>) result.get("result");
    }
}
