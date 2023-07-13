/**  
 * @Title: RestResearchProjectRepository.java
 * @Package com.cnpc.epai.core.dataset.repository
 * @Description: 1090服务调用
 * @author cuijiaqi
 * @date 2018年5月7日 下午2:34:16 
 * {修改记录：cuijiaqi 2018年5月7日 下午2:34:16}
 */
package com.cnpc.epai.core.worktask.repository;

import com.alibaba.fastjson.JSONArray;
import com.cnpc.epai.core.worktask.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
 * @Description: TODO 
 * @author cuijiaqi
 * @version 1.0.9 
 */
@Slf4j
@Service
public class RestMetadataRepository {
	@Autowired
	private RestTemplate restTemplate;
	static String apigateway = "http://1019metadata/sys";

	/** 
	 * CPR删除业务数据
	 * @param dataGroup
	 * @param dataSetName
	 * @param ids
	 * @return
	 * {修改记录：cuijiaqi 2018年5月8日 下午5:23:51}
	 */
	public String crpDelete(String dataGroup, String dataSetName, List<String> ids) throws Exception{
		log.info("RestMetadataRepository.crpDelete");
		try{
			StringBuffer serviceName = new StringBuffer();
			serviceName.append(apigateway).append("/dataservice/crp/{dataGroup}/{dataSetName}/delete.json");

			HttpEntity<String> httpEntity = new HttpEntity<String>(JSONArray.toJSONString(ids), RestUtil.getHeaders());
			ParameterizedTypeReference<Map<String,Object>> ref = new ParameterizedTypeReference<Map<String,Object>>(){};

			log.info(new StringBuffer().append("-DELETE").append(" ").append(serviceName.toString())
					.append(" ")
					.append(JSONArray.toJSONString(dataGroup))
					.append(" ")
					.append(JSONArray.toJSONString(dataSetName))
					.append(" ")
					.append(JSONArray.toJSONString(ids)).toString());
			Map<String,Object> result = restTemplate.exchange(serviceName.toString(), HttpMethod.DELETE, httpEntity, ref, dataGroup, dataSetName).getBody();

			return result.get("resultState").toString();
		} catch(Exception e){
			log.error("RestError: "+e.getLocalizedMessage());
			throw e;
		}
	}
	

}
