package com.cnpc.epai.core.workscene.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.MinBizNode;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface MinBizNodeService extends IService<MinBizNode> {
    Object searchNode(String nodeName);

    Object addMinBizNode(String bizNodeName);

    Object completeConfig(String bizNodeId);

    Object allMinBizNode(Boolean isAll);

    Object updateNode(String treeId, String bizNodeId, String type, String bizNodeName);

    Object syncBizNode(Map<String, Object> data);
}
