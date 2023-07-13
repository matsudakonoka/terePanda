package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.communication.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description: 权限服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Slf4j
@Service
public class AuthorityRepository {
    private String serviceName = "1002user/sys/";

    String url_user = "http://" + serviceName + "/user/";


    /**
     * 缓存中获取用户信息
     * @param userId
     * @return 用户信息
     */
    public Map getUserInfo(String userId){
        return (Map) CacheManager.of().hOMGet("userscache", userId);
    }

    /**
     * 获取用户名
     * @param userId
     * @return 用户显示名
     */
    public String getUserDisplayName(String userId){
       try{
           Map map = getUserInfo(userId);
           return (String) map.get("displayName");
       }catch (Exception e){
           return "";
       }
    }
}
