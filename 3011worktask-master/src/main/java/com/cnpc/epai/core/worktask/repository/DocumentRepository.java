package com.cnpc.epai.core.worktask.repository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
/**
 * @Description: 文档集服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Slf4j
@Service
public class DocumentRepository {
    private String serviceName = "3003document/core/document/";

    String url_document = "http://" + serviceName + "/";

    @Autowired
    RestTemplate restClient;;

    /**
     * 关联成果
     * @param projectId 项目id
     * @param taskId 任务id
     * @param treeDataIds 文件id数组
     */
    public void relatedAchievement(String projectId,String taskId,String treeDataIds[]){
        String params = org.apache.commons.lang3.StringUtils.join(treeDataIds,",");
        params = "dataTreeId="+params;
        HttpEntity<String> formEntity = new HttpEntity("", genHeader());
        restClient.put(url_document+"/{projectId}/task/{taskId}?"+params,formEntity,projectId,taskId);
    }

    /**
     * 关联的成果审核
     * @param projectId
     * @param workTaskId
     * @param status 2:审核通过
     * @param remark
     */
    public void audit(String projectId, String workTaskId, String status,String remark){
        HttpEntity<String> formEntity = new HttpEntity("", genHeader());
        restClient.put(url_document+"/{projectId}/{taskId}/auditbytask/{status}?remark="+remark,formEntity,projectId,workTaskId,status);
    }

    public HttpHeaders genHeader() {
        HttpHeaders headers = new HttpHeaders();
        MediaType type2 = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type2);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return headers;
    }

    public static void main(String args[]){
        String[] fileIds = {"123"};
        String params = org.apache.commons.lang3.StringUtils.join(fileIds,"&dataTreeId[]=");
        System.out.println(params);
    }
}
