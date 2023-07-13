package com.cnpc.epai.core.workscene.internal;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.commom.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalMap {
    private final Map<String, Map<String, String>> RESOURCE_CACHE = new HashMap<>();
    private final Map<String, List<Object>> inputRaNode = new HashMap<>();
    private final Map<String, List<Object>> outputRaNode = new HashMap<>();
    private final Map<String, List<Object>> nodeRaInput = new HashMap<>();
    private final Map<String, List<Object>> nodeRaOutput = new HashMap<>();

    public void put(Map<String, Object> node, Map<String, Object> resource, boolean isInput) {
        String nodeId = (String) node.get(Constants.NODE_ID);
        String resourceId = (String) resource.get("id");

        String extAttributes = (String) resource.get("extAttributes");
        JSONObject object = (JSONObject) JSONObject.parse(extAttributes);
        String resName = object.getString("resName");
        if (!RESOURCE_CACHE.containsKey(resName)) {
            RESOURCE_CACHE.put(resName, new HashMap<>(2));
        }
        if (isInput) {
            RESOURCE_CACHE.get(resName).put("input", resourceId);
            if (nodeRaInput.containsKey(nodeId)) {
                nodeRaInput.get(nodeId).add(resource);
            } else {
                List<Object> value = new ArrayList<>();
                value.add(resource);
                nodeRaInput.put(nodeId, value);
            }
            if (inputRaNode.containsKey(resourceId)) {
                inputRaNode.get(resourceId).add(node);
            } else {
                List<Object> value = new ArrayList<>();
                value.add(node);
                inputRaNode.put(resourceId, value);
            }
        } else {
            RESOURCE_CACHE.get(resName).put("output", resourceId);
            if (nodeRaOutput.containsKey(nodeId)) {
                nodeRaOutput.get(nodeId).add(resource);
            } else {
                List<Object> value = new ArrayList<>();
                value.add(resource);
                nodeRaOutput.put(nodeId, value);
            }
            if (outputRaNode.containsKey(resourceId)) {
                outputRaNode.get(resourceId).add(node);
            } else {
                List<Object> value = new ArrayList<>();
                value.add(node);
                outputRaNode.put(resourceId, value);
            }
        }
    }

    public List<Object> getInResource(String node) {
        return nodeRaInput.get(node);
    }
    public List<Object> getOutResource(String node) {
        return nodeRaOutput.get(node);
    }

    public List<Object> getInNode(String resourceId) {
        return inputRaNode.get(resourceId);
    }
    public List<Object> getOutNode(String resourceId) {
        return outputRaNode.get(resourceId);
    }

    public Map<String, String> getInOutResource(String resName) {
        return RESOURCE_CACHE.get(resName);
    }

}
