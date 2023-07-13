package com.cnpc.epai.core.worktask.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.template.service.TreeJsonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author 王淼
 * @Title: A6
 * @Package com.cnpc.epai.research.project.repository
 * @Description: 功能描述
 * @date 18:54 2019/1/22
 * {修改记录：修改人、修改时间、修改内容等}
 */
@Slf4j
@Service
public class ApplicationRepository {

    @Autowired
    private RestTemplate restClient;

    private String serviceName = "http://4100applications/research/application/";


    public HttpHeaders genHeader() {
        HttpHeaders headers = new HttpHeaders();
        MediaType type2 = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type2);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }

    public List<TreeJsonDto> getApplicationTree(String dataRegion){
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, genHeader());
        String result = restClient.exchange(serviceName+"?dataRegion="+dataRegion, HttpMethod.GET,requestEntity,String.class).getBody();;
        List<TreeJsonDto> list =  JSON.parseArray(result,TreeJsonDto.class);
        return list;
    }

    public List<JSONObject> getCloudPlatformAppList(Integer displayWidth,Integer displayHeight){
        MultiValueMap<String, Object> requestParam = new LinkedMultiValueMap<String, Object>();
        requestParam.add("displayHeight", displayHeight);
        requestParam.add("displayWidth", displayWidth);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(requestParam, genHeader());
        String result = restClient.exchange(serviceName+"/cloudPlatform/all.json", HttpMethod.POST,requestEntity,String.class).getBody();
        List<JSONObject> list =  JSON.parseArray(result,JSONObject.class);
        return list;
    }

    public List<JSONObject> formatCloudPlatformAppList(Integer displayWidth, Integer displayHeight){
//        String str = "[\n" +
//                "  {\n" +
//                "    \"softwareList\": [\n" +
//                "      {\n" +
//                "        \"softwareId\": \"342\",\n" +
//                "        \"softwareName\": \"GeoEast-201\",\n" +
//                "        \"osType\": \"linux\",\n" +
//                "        \"softwareIp\": \"10.88.110.201\",\n" +
//                "        \"softwarePort\": 6,\n" +
//                "        \"softwareNodeName\": \"9FAEEA42A50C45075242689C50EAA565\",\n" +
//                "        \"softwarePassword\": \"9FAEEA42A50C45075242689C50EAA565\",\n" +
//                "        \"openUrl\": \"MyApplication:/linux&11.10.189.32#9FAEEA42A50C45075242689C50EAA565#9FAEEA42A50C45075242689C50EAA565#6#1440x900#9FAEEA42A50C45075242689C50EAA565#\",\n" +
//                "        \"customProperties\": null\n" +
//                "      }\n" +
//                "    ],\n" +
//                "    \"name\": \"软件云桌面\",\n" +
//                "    \"id\": \"dg\"\n" +
//                "  }\n" +
//                "]";
//        JSONArray list1 = JSON.parseArray(str);
//        List<JSONObject> list = list1.toJavaList(JSONObject.class);

        List<JSONObject> list = getCloudPlatformAppList(displayWidth,displayHeight);
        for (JSONObject obj : list) {
            String rootName = obj.getString("name");
            JSONArray array = obj.getJSONArray("softwareList");
            JSONObject cloudApp = null;
            for (Object objApp : array) {
                cloudApp = (JSONObject) objApp;
                String destop_softname = cloudApp.get("softwareName").toString();
                cloudApp.put("desktop_clouded",true);
                cloudApp.put("id",destop_softname);
                cloudApp.put("satelliteName",destop_softname);
                cloudApp.put("name",destop_softname);
                cloudApp.put("cloudApp_rootName",rootName);
            }
        }
        return list;
    }
}
