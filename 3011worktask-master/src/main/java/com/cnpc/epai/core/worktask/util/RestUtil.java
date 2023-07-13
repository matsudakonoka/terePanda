/**  
 * @Title: RestUtil.java
 * @Package com.cnpc.epai.core.dataset.util
 * @Description: Rest服务工具类
 * @author cuijiaqi
 * @date 2018年5月7日 下午2:34:16 
 * {修改记录：cuijiaqi 2018年5月7日 下午2:34:16}
*/
package com.cnpc.epai.core.worktask.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** 
 * @Description: TODO 
 * @author cuijiaqi
 * @version 1.0.9 
 */
public class RestUtil {
	/** 
	 * 生成HttpHeaders
	 * @return
	 * {修改记录：cuijiaqi 2018年5月8日 下午6:09:57}
	 */
	public static HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
		return headers;
	}
	
	public static HttpHeaders getFormHeaders(){
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
		return headers;
	}
}
