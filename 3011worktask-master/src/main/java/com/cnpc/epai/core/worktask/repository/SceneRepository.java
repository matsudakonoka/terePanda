package com.cnpc.epai.core.worktask.repository;

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
public class SceneRepository {

    @Autowired
    private RestTemplate restTemplate;

    private String serviceName = "http://3005smartmicroscene/core/smartmicroscene";


    public List<Map<String,Object>> getSceneList(String sceneIds){
        List<Map<String,Object>>  list = new ArrayList<>();
        String url = serviceName + "/scene/scenelist?sceneIds="+sceneIds;

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);

        try {
            ParameterizedTypeReference<List<Map<String,Object>>> typeRef = new ParameterizedTypeReference<List<Map<String,Object>>>(){};
            list = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), typeRef).getBody();

        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }
}
