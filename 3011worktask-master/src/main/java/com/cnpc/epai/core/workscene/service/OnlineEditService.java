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
public class OnlineEditService {

    @Value("${epai.domainhost}")
    private String gateway;

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject findFile(String modelId, String reportTemplateId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(gateway)
                .append("/research/onlineoffice/bs/doc/getFile?docModelId=")
                .append(modelId).append("&")
                .append("entityId=").append(reportTemplateId);

        final String url = stringBuilder.toString();
        HttpEntity<String> entity = new HttpEntity<>(HttpUtils.header());
        ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, JSONObject.class);
        JSONObject result = response.getBody();
        return result;
    }

}
