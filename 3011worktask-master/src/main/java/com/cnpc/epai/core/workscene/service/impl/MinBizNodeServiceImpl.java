package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.MinBizNode;
import com.cnpc.epai.core.workscene.mapper.MinBizNodeMapper;
import com.cnpc.epai.core.workscene.service.MinBizNodeService;
import com.cnpc.epai.core.workscene.service.TreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MinBizNodeServiceImpl extends ServiceImpl<MinBizNodeMapper, MinBizNode> implements MinBizNodeService {

    @Autowired
    private TreeService treeService;

    @Override
    public Object searchNode(String nodeName) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like("node_name", nodeName);
        return list(queryWrapper);
    }

    @Override
    public Object allMinBizNode(Boolean isAll) {
        if (isAll) {
            return list();
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("finish", true);
        return list(queryWrapper);
    }

    @Override
    public Object updateNode(String treeId, String bizNodeId, String type, String bizNodeName) {

        List<Map<String, Object>> tree = treeService.getTree(treeId, null);
        recursionUpdate(tree, bizNodeId, bizNodeName);
        JSONArray newTree = JSONArray.parseArray(JSON.toJSONString(JSON.toJSON(tree)));

        boolean result = false;
        if (treeService.saveTree(treeId, newTree)) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.set("node_name", bizNodeName);
            updateWrapper.eq("node_id", bizNodeId);
            result = update(updateWrapper);
        }
        return result;
    }

    private void recursionUpdate(List<Map<String, Object>> tree, String bizNodeId, String bizNodeName) {
        for (Map<String, Object> node : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) node.get("children");

            String sourceNodeId = (String) node.get("sourceNodeId");
            if (bizNodeId.equals(sourceNodeId)) {
                node.put("nodeName", bizNodeName);
            }

            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            recursionUpdate(subTree, bizNodeId, bizNodeName);
        }
    }

    @Override
    public Object syncBizNode(Map<String, Object> data) {
        List<MinBizNode> minBizNodes = new ArrayList<>();
        List<MinBizNode> addMinBizNodes = new ArrayList<>();
        recursion(minBizNodes, data);
        for (MinBizNode node : minBizNodes) {
            MinBizNode byId = getById(node.getNodeId());
            if (byId == null || (!byId.getNodeName().equals(node.getNodeName()))) {
                addMinBizNodes.add(node);
            }
        }
        return saveOrUpdateBatch(addMinBizNodes);
    }

    private void recursion(List<MinBizNode> minBizNodes, Map<String, Object> data) {
        List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");
        if (children == null || children.size() == 0) {
            String nodeType = (String) data.get("nodeType");
            if (Constants.WORK_UNIT.equals(nodeType)) {
                String nodeId = (String) data.get("nodeId");
                String nodeName = (String) data.get("nodeName");
                MinBizNode byIdNode = getById(nodeId);
                if (byIdNode == null || (!byIdNode.getNodeName().equals(nodeName))) {
                    MinBizNode minBizNode = new MinBizNode();
                    minBizNode.setNodeId(nodeId);
                    minBizNode.setNodeName(nodeName);
                    minBizNode.setFinish(false);
                    minBizNodes.add(minBizNode);
                }
            }
            return;
        }
        for (Map<String, Object> object : children) {
            recursion(minBizNodes, object);
        }
    }

    @Override
    public Object addMinBizNode(String bizNodeName) {
        MinBizNode minBizNode = new MinBizNode();
        minBizNode.setFinish(false);
        minBizNode.setNodeName(bizNodeName);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("node_name", bizNodeName);
        List<MinBizNode> nodes = list(queryWrapper);
        if (nodes != null && nodes.size() > 0) {
            return false;
        }
        save(minBizNode);
        return minBizNode;
    }

    @Override
    public Object completeConfig(String bizNodeId) {
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("node_id", bizNodeId);
        updateWrapper.set("finish", true);
        return update(updateWrapper);
    }

}
