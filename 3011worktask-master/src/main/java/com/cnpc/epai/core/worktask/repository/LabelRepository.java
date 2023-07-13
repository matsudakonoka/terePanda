package com.cnpc.epai.core.worktask.repository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
/**
 * @Description: 标签服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Slf4j
@Service
public class LabelRepository {


    private String serviceName = "4104labels/research/label";

    @Autowired
    RestTemplate restClient;

    String url = "http://" + serviceName+"/";


    /**
     * 添加用户
     * @param projectId
     * @param userId
     * @param userCode 用户标识(M:项目经理，N:项目成员，G:普通用户)
     * @return
     */
    public String addUser(String projectId, String userId, String userCode){
        Map<String,String> obj = new HashMap<String,String>();
        obj.put("uid",userId);
        obj.put("projectId",projectId);
        obj.put("userIdentity",userCode);

        HttpEntity<Object> formEntity = new HttpEntity("", genHeader());

        //todo 需要结果判断是否添加成功
        String str = restClient.postForObject(url+"addList?uid="+userId+"&projectId="+projectId+"&userIdentity="+userCode,formEntity,String.class);
        if (StringUtils.isEmpty(str) && !"\"1\"".equals(str)) {
            System.out.println("添加标签失败:"+ str);
            return "-1";
        }
        return "1";
    }

    /**
     * 删除用户
     * @param projectId
     * @param userId
     */
    public void deleteUser(String projectId, String userId){
        String delete_Url = "deleteList/{userId}/{projectId}";
        restClient.delete(url+delete_Url,userId,projectId);
    }


    public HttpHeaders genHeader(){
        HttpHeaders headers = new HttpHeaders();
        MediaType type2 = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type2);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }

    /**
     * 更新用户标签
     * @param projectId
     * @param workTaskId
     * @param action
     */
    public void updateLableStatus(String projectId,String workTaskId,String action){
        HttpEntity<String> formEntity = new HttpEntity("", genHeader());
        restClient.put(url+"/user/{projectId}/{action}/{workTaskId}", formEntity, projectId, action, workTaskId);
//
//        String serviceName = url+"/user/{projectId}/{action}";
//
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        String str = restSupport.getRestTemplate().exchange(serviceName, HttpMethod.PUT, new HttpEntity<String>("", headers),String.class,projectId, action, workTaskId).getBody();
//        System.out.println("标签服务返回:"+str);
    }


}
