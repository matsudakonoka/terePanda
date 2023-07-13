package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.internal.FilterConfig;
import com.cnpc.epai.core.workscene.internal.InternalMap;
import com.cnpc.epai.core.workscene.internal.InternalThreadLocal;
import com.cnpc.epai.core.workscene.internal.Node;
import com.cnpc.epai.core.workscene.service.PanoramaService;
import com.cnpc.epai.core.workscene.service.ResourceService;
import com.cnpc.epai.core.workscene.service.TreeService;
import com.cnpc.epai.core.workscene.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PanoramaServiceImpl implements PanoramaService {

    @Autowired
    WorkService workService;

    @Autowired
    TreeService treeService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    FilterConfig filterConfig;

    private static final ConcurrentHashMap DATA_CACHE = new ConcurrentHashMap();
    public Map<String, String> T0_CACHE = new HashMap<>();

    @Override
    public Node panorama(String workId, String nodeId) {
        initCache();

        Work work = workService.getById(workId);
        if (work == null) {
            throw new IllegalStateException("工作不存在");
        }

        String treeId = work.getInstanceId();
        List<Map<String, Object>> tree = treeService.getTree2(work.getWorkId(), null);
        beforeProcess();

        Node filterResult = filterConfig.chain().invoke(buildNode(tree, nodeId, workId));
        afterProcess(workId, nodeId);
        return filterResult;
    }

    @Override
    public Object getAchieve(String workId, String nodeId) {
        return DATA_CACHE.get(workId + "_" + nodeId);
    }

    private void initCache() {
        List<Map<String, Object>> tree = treeService.getTree(Constants.T0_TREE, null);
        doInit(tree);
    }

    private void doInit(List<Map<String, Object>> tree) {
        for (Map<String, Object> m : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) m.get(Constants.CHILDREN);

            String nodeType = (String) m.get(Constants.NODE_TYPE);
            String nodeId = (String) m.get(Constants.NODE_ID);
            String sourceNodeId = (String) m.get(Constants.SOURCE_NODE_ID);
            if (nodeType != null && nodeType.equals(Constants.WORK_UNIT)) {
                T0_CACHE.put(nodeId, sourceNodeId);
            }

            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            doInit(subTree);
        }
    }

    private void afterProcess(String workId, String nodeId) {
        Map<String, Object> value = (Map<String, Object>) InternalThreadLocal.get();
        Map<String, Object> result = new HashMap<>(2);
        result.put("create", value.get("create"));
        result.put("refer", value.get("refer"));
        final String KEY = workId + "_" + nodeId;
        DATA_CACHE.put(KEY, result);
    }

    private void beforeProcess() {
        Map<String, Map<String, Object>> value = new HashMap<>(2);
        Map<String, Object> create = new HashMap<>(2);
        create.put("done", 0);
        create.put("undone", 0);
        Map<String, Object> refer = new HashMap<>(2);
        refer.put("done", 0);
        refer.put("undone", 0);
        value.put("create", create);
        value.put("refer", refer);
        System.out.println("Thread: " + Thread.currentThread().getName());
        InternalThreadLocal.set(value);
    }

    private Node buildNode(List<Map<String, Object>> tree, String nodeId, String workId) {
        InternalMap internalMap = new InternalMap();
        Map<String, Map<String, Object>> nodes = new HashMap<>();
        searchTree(tree, nodes, workId);

        Map<String, Map<String, Object>> localMap = (Map<String, Map<String, Object>>) InternalThreadLocal.get();
        for (Map.Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
            String nId = entry.getKey();
            Map<String, Object> node = entry.getValue();
            String sourceNodeId = (String) node.get(Constants.SOURCE_NODE_ID);
            String rNodeId = T0_CACHE.get(sourceNodeId);
            // 使用模板数的 sourceNodeId 查询资源
            List<Map<String, Object>> resources = resourceService.getResource(sourceNodeId);

            List<Object> expert = new ArrayList<>();
            List<Object> station = new ArrayList<>();

            if (resources == null || resources.size() == 0) {
                continue;
            } else {
                for (Map<String, Object> resource : resources) {
                    String rType = (String) resource.get(Constants.RESOURCE_TYPE);
                    if (Constants.RESOURCE_IN_TYPE.equals(rType)) {
                        internalMap.put(node, resource, true);
                    }
                    if (Constants.RESOURCE_OUT_TYPE.equals(rType)) {
                        internalMap.put(node, resource, false);
                    }
                    if (Constants.RESOURCE_EXPERT.equals(rType)) {
                        expert.add(resource);
                    }
                    if (Constants.RESOURCE_STATION.equals(rType)) {
                        station.add(resource);
                    }
                }
            }

            Map<String, Object> map = new HashMap<>(2);
            map.put("expert", expert);
            map.put("station", station);
            localMap.put(nId, map);

        }
        return doBuildNode(nodeId, nodes, internalMap);

    }

    private static Node doBuildNode(String nodeId, Map<String, Map<String, Object>> nodes, InternalMap internalMap) {
        Node node = new Node(nodes.get(nodeId), internalMap.getInResource(nodeId), internalMap.getOutResource(nodeId));
        List<Object> inputResource = node.getInputResource();
        for (Object object : inputResource) {
            Map<String, Object> resource = (Map<String, Object>) object;
            String resourceId = (String) resource.get("id");

            String extAttributes = (String) resource.get("extAttributes");
            JSONObject jsonObject = (JSONObject) JSONObject.parse(extAttributes);
            String resName = jsonObject.getString("resName");
            Map<String, String> inOutResource = internalMap.getInOutResource(resName);
            String outResource = inOutResource.get("output");
            List<Object> outNode = internalMap.getOutNode(outResource);

            if (outNode == null || outNode.size() == 0) {
                continue;
            }
            for (Object obj : outNode) {
                Map<String, Object> n = (Map<String, Object>) obj;
                String nId = (String) n.get(Constants.NODE_ID);
                // System.out.println(n.get("nodeName"));
                node.addChild(doBuildNode(nId, nodes, internalMap));
            }
        }
        return node;
    }

    private void searchTree(List<Map<String, Object>> tree, Map<String, Map<String, Object>> nodes, String workId) {
        for (Map<String, Object> m : tree) {
            List<Map<String, Object>> subTree = (List<Map<String, Object>>) m.get(Constants.CHILDREN);

            String nodeType = (String) m.get(Constants.NODE_TYPE);
            String nodeId = (String) m.get(Constants.NODE_ID);
            String nodeName = (String) m.get("nodeName");
            if (nodeType != null && nodeType.equals(Constants.WORK_UNIT)) {
                System.out.println(nodeName + "   " + nodeId);
                m.put(Constants.WORK_ID, workId);
                nodes.put(nodeId, m);
            }
            if (subTree == null || subTree.size() == 0) {
                continue;
            }
            searchTree(subTree, nodes, workId);
        }
    }

    public static void main(String[] args) {
        InternalMap internalMap = new InternalMap();
        Map<String, Map<String, Object>> nodes = new HashMap<>();

        Map<String, Object> node1 = new HashMap<>(2);
        node1.put("nodeId", "node1");
        node1.put("nodeName", "node1");
        Map<String, Object> node2 = new HashMap<>(2);
        node2.put("nodeId", "node2");
        node2.put("nodeName", "node2");
        Map<String, Object> node3 = new HashMap<>(2);
        node3.put("nodeId", "node3");
        node3.put("nodeName", "node3");
        Map<String, Object> node4 = new HashMap<>(2);
        node4.put("nodeId", "node4");
        node4.put("nodeName", "node4");
        Map<String, Object> node5 = new HashMap<>(2);
        node5.put("nodeId", "node5");
        node5.put("nodeName", "node5");

        nodes.put("node1", node1);
        nodes.put("node2", node2);
        nodes.put("node3", node3);
        nodes.put("node4", node4);
        nodes.put("node5", node5);

        Map<String, Object> resourceIn1 = new HashMap<>(2);
        resourceIn1.put("resourceId", "resource1");
        resourceIn1.put("resourceName", "resource1");
        resourceIn1.put("type", "input");
        internalMap.put(node1, resourceIn1, true);

        Map<String, Object> resourceOut1 = new HashMap<>(2);
        resourceOut1.put("resourceId", "resource1");
        resourceOut1.put("resourceName", "resource1");
        resourceOut1.put("type", "output");
        internalMap.put(node2, resourceOut1, false);
        internalMap.put(node3, resourceOut1, false);

        Map<String, Object> resourceIn2 = new HashMap<>(2);
        resourceIn2.put("resourceId", "resource2");
        resourceIn2.put("resourceName", "resource2");
        resourceIn2.put("type", "input");
        internalMap.put(node2, resourceIn2, true);

        Map<String, Object> resourceOut2 = new HashMap<>(2);
        resourceOut2.put("resourceId", "resource2");
        resourceOut2.put("resourceName", "resource2");
        resourceOut2.put("type", "output");
        internalMap.put(node4, resourceOut2, false);

        Map<String, Object> resourceIn3 = new HashMap<>(2);
        resourceIn3.put("resourceId", "resource3");
        resourceIn3.put("resourceName", "resource3");
        resourceIn3.put("type", "input");
        internalMap.put(node3, resourceIn3, true);

        Map<String, Object> resourceOut3 = new HashMap<>(2);
        resourceOut3.put("resourceId", "resource3");
        resourceOut3.put("resourceName", "resource3");
        resourceOut3.put("type", "output");
        internalMap.put(node5, resourceOut3, false);

        Node node11 = doBuildNode("node1", nodes, internalMap);

        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(node11);
        System.out.println(jsonObject.toJSONString());
    }
}
