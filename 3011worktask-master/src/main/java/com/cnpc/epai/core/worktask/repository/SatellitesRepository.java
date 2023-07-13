package com.cnpc.epai.core.worktask.repository;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.research.application.domain.InfoForTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 卫星端服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Service
@Slf4j
public class SatellitesRepository {

    @Autowired
    private RestTemplate restClient;


    private String serviceName = "3013asi/core/asi/";


    /**
     * 获取所有卫星端信息
     * @param eoCode
     * @return
     */
    public List<Map<String, Object>> findList(String eoCode,String dataRegion) {
        String url = "http://" + serviceName + "/a6research/getSatelliteInfoList.json?dataRegion="+dataRegion;
        if (!StringUtils.isEmpty(eoCode)) {
            url += "&eoCode=" + eoCode;
        }
        String str = restClient.getForObject(
                url,
                String.class);

        JSONArray jsonArray = JSON.parseArray(str);

        List<Map<String, Object>> resultList = new ArrayList<>();
        if (!jsonArray.isEmpty()) {
            Map<String, Object> map = null;
            for (int i = 0; i < jsonArray.size(); i++) {
                map = (Map<String, Object>) jsonArray.get(i);
                resultList.add(map);
//                if ((boolean)map.get("enable")) {
//                    resultList.add(map);
//                    map.put("name",map.get("satelliteName"));
//                    map.put("softwareTypeId",softwareTypeId);
//                }
            }
        }
        return resultList;
    }

    /**
     * 获取卫星端信息
     * @param satelliteId
     * @return 卫星端信息
     */
    public Map<String,Object> findBySatelliteId(String satelliteId){
        Map<String,Object> map = restClient.getForObject(
                "http://"+serviceName+"/a6research/getSatelliteInfo.json?satelliteId={type}",
                Map.class,satelliteId);
        return map;
    }

    /**
     * 向专业软件发送数据
     * @param sendMap
     * @param flag 是否发送单条
     */
    public void sendAsi(Map<String,Object> sendMap, String localMac, Boolean flag){
        //获取token信息并存入ThreadContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String token= details.getTokenValue();

        new Thread(() -> {
            HttpHeaders headers = new HttpHeaders();
            String asiUrl = "";
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
            headers.add("Authorization", "Bearer " + token);

            if ("Y".equalsIgnoreCase(sendMap.get("isSendable").toString())) {   //“isSendable”： “Y”--发送数据，“N”--下载数据
                asiUrl = "http://3013asi/core/asi/a6research/"+ (flag?"saveData":"batchSaveData")+"?localMac="+localMac;
            } else if("N".equalsIgnoreCase(sendMap.get("isSendable").toString())){
                asiUrl = "http://3013asi/core/asi/a6research/getSatelliteDataFile";
            }
            sendMap.put("batchFlag", flag? "N": "Y");                           //“batchFlag”："Y"--批量，"N"--单条
            HttpEntity<String> httpEntity = new HttpEntity<String>(new JSONObject(sendMap).toString(), headers);
            System.out.println("ASI-SEND-BEGIN: ");
            System.out.println("serviceUrl: "+asiUrl);
            System.out.println("sendMap: "+sendMap.toString());
            System.out.println("ASI-SEND-END");
            String str = restClient.exchange(asiUrl, HttpMethod.POST, httpEntity, String.class).getBody();
            System.out.println("sendAsiMessage result:"+str);
        }).start();
    }
}
