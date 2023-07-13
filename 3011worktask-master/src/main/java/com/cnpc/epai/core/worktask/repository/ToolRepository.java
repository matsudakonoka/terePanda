package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.research.tool.domain.Tool;
import com.cnpc.epai.research.tool.domain.ToolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 常用工具服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Service
public class ToolRepository {

    private String serviceName = "4101tool/research/tool/";

    @Autowired
    private RestTemplate restClient;

    /**
     * 根据id获取常用工具
     * @param id
     * @return
     */
    public Tool findOne(String id){
        if(StringUtils.isEmpty(id)){
            return null;
        }

        Tool t = restClient.getForObject(
                "http://"+serviceName+"/{id}",
                Tool.class,id);
        if (t == null || t.getId() == null) {
            return null;
        }else{
            return t;
        }
    }

    /**
     * 获取常用工具树
     * @return
     */
    public List<ToolType> findToolTypeTree(){
        ToolType[] toolTypes = restClient.getForObject(
                "http://"+serviceName+"/findToolTypeTree",
                ToolType[].class);
        return Arrays.asList(toolTypes);
    }
}
