package com.cnpc.epai.core.worktask.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import io.swagger.annotations.ApiParam;

import java.util.List;

/**
 * @Description: 项目工作室服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Service
public class ProjectRepository {

    private String serviceName = "4103project/research/project/";

    @Autowired
    private RestTemplate restClient;

    /**
     * 根据id获取项目列表
     * @param ids
     * @return
     */
    public List findByids(String ids){
        if(StringUtils.isEmpty(ids)){
            return null;
        }
        return restClient.postForObject(
                "http://"+serviceName+"/getProjectsById?ids="+ids,null,
                List.class);
    }
    
    public String updateProjectSchedule(String projectId, int schedule){
    	return restClient.postForObject("http://"+serviceName+"/"+projectId+"/updateProjectSchedule?schedule="+schedule, null ,String.class);
    }
    
    public String delAllmilestone(String projectId){
    	restClient.delete("http://"+serviceName+"/milestone/"+projectId+"/delallmilestone");
    	return "1";
    }
    
}
