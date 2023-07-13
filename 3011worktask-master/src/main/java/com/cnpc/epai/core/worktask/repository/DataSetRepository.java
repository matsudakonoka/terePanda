package com.cnpc.epai.core.worktask.repository;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 数据集服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Service
public class DataSetRepository {
    @Autowired
    RestTemplate restClient;
	@Value("${epai.domainhost:www.dev.pcep.cloud}")
	private String domainhost;

    private String serviceName = "3001dataset/core/dataset";

    /**
     * 根据数据集id获取数据集
     * @param id
     * @return 数据集
     */
    public SrMetaDataset findOne(String id){
        SrMetaDataset d = restClient.getForObject(
                "http://"+serviceName+"/{id}",
                SrMetaDataset.class,id);

        if (d!=null && d.getId() == null) {
            return null;
        }else{
            return d;
        }
    }

    /**
     * 获取所有数据集
     * @return 数据集列表
     */
    public List<SrMetaDataset> findAll(String dataRegion){
        SrMetaDataset[] d = restClient.getForObject(
                "http://"+serviceName+"/?filterDisplay=true&dataRegion="+dataRegion,SrMetaDataset[].class);
        return Arrays.asList(d);
    }

    /**
     * 根据数据集ids,返回对应结构
     * @param ids 当前数据集id
     * @return 数据集结构
     */
    public List<SrMetaDataset> findTree(String ids, String dataRegion){
        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<String, String>();
        requestParam.add("datasetIds", ids);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<MultiValueMap<String, String>>(requestParam, headers);
        SrMetaDataset[] d = restClient.postForObject(
                "http://"+serviceName+"/getRoot?dataRegion="+dataRegion,formEntity,
                SrMetaDataset[].class,ids);
        return Arrays.asList(d);
    }
    

//同步导航树数据集
   public void syncIndex(String projectId, String taskId, Map<String, Object> data,String token){
	   
       String serviceNamePath = "http://"+domainhost+"/core/dataset/udb/"+projectId+"/"+taskId+"/syncIndexs";

       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       //返回json
       headers.add("Accept",MediaType.APPLICATION_JSON_VALUE.toString());
       headers.add("Authorization", "Bearer " + token);
       String body = "";
       RestTemplate restTemplate = new RestTemplate();
       try {
    	   body = restTemplate.exchange(serviceNamePath.toString(), HttpMethod.POST, new HttpEntity<>(data, headers), String.class).getBody();
       } catch(Exception e) {
    	   e.printStackTrace();
       }
       System.out.println(body);
   }
   //删除任务数据集索引数据
    public void delete(String projectId, String datasetId, String taskId){
//        http://www.dev.pcep.cloud/core/dataset/task/ACTIYJ100000011/Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0/j5oac9o80t2ffb59404q1xb7/delete
        String servicePath = "http://"+serviceName+"/task/"+projectId+"/"+datasetId+"/"+taskId+"/delete";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        String body = restClient.exchange(servicePath, HttpMethod.DELETE, new HttpEntity<>("", headers), String.class).getBody();
        System.out.println(body);
    }

    //查询任务数据集数据
    public List<Map<String,Object>> searchData(String projectId, String datasetId, String taskId, Pageable pageable){
//        http://www.dev.pcep.cloud/core/dataset/task/ACTIYJ100000011/Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0/j5oac9o80t2ffb59404q1xb7/searchdata
        String servicePath = "http://"+serviceName+"/task/"+projectId+"/"+datasetId+"/"+taskId+"/searchdata?page="+pageable.getPageNumber()+"&size="+pageable.getPageSize();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        ParameterizedTypeReference<Map<String, Object>> ref = new ParameterizedTypeReference<Map<String, Object>>() {
        };
        Map<String, Object> result = restClient.exchange(servicePath, HttpMethod.POST, new HttpEntity<>("", headers), ref).getBody();
        List<Map<String,Object>> mapList = (List<Map<String,Object>>)result.get("content");
        return mapList;
    }
}
