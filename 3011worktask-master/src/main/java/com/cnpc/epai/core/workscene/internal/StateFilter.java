package com.cnpc.epai.core.workscene.internal;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.service.WorkObjectService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateFilter implements Filter {

    private ApplicationContext applicationContext;

    private Filter filter;

    public StateFilter() {

    }

    public StateFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Node invoke(Node result) {
        try {
            recursion(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter == null ? result : filter.invoke(result);
    }

    private void recursion(Node node) {
        if (node == null) {
            return;
        }
        List<Node> child = node.getChild();
        for (Node cNode : child) {
            recursion(cNode);
        }
        calculateState(node);
    }

    private void calculateState(Node node) {
        List<Object> outputResource = node.getOutputResource();
        String workId = (String) node.getAttachment("workId");
        String nodeId = (String) node.getAttachment("nodeId");

        int size = outputResource.size();
        WorkObjectService workObjectService = applicationContext.getBean(WorkObjectService.class);

        // 进行数据分类
        List<Map<String, Object>> classify = new ArrayList<>();
        for (Object res : outputResource) {
            Map<String, Object> resource = (Map<String, Object>) res;
            String datasetId = (String) resource.get("resId");

            String extAttributes = (String) resource.get("extAttributes");
            JSONObject jsonObject = (JSONObject) JSONObject.parse(extAttributes);
            String resName = jsonObject.getString("resName");
            String datasetName = resName;

            List<SrTaskTreeData> data = workObjectService.getAllObjectBy(workId, nodeId, datasetId);
            doCalculateState(data, node);

            Map<String, Object> map = new HashMap<>(4);
            map.put(Constants.DATASET_ID, datasetId);
            map.put(Constants.DATASET_NAME, datasetName);
            map.put(Constants.DATA_RESULT, data);
            classify.add(map);
        }
        node.addAttachment(Constants.DATA_CLASSIFY, classify);
    }

    private void doCalculateState(List<SrTaskTreeData> data, Node node) {
        String user = SpringManager.getCurrentUser().getUserId();
        // 状态计算
        System.out.println("doCalculateState Thread: " + Thread.currentThread().getName());
        Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) InternalThreadLocal.get();
        Map<String, Object> count;
        boolean finish = true;
        if (data == null || data.size() == 0) {
            node.addAttachment("finish", !finish);
            return;
        }
        for (SrTaskTreeData taskTreeData : data) {
            String dataStatus = taskTreeData.getDataStatus();
            String createUser = taskTreeData.getCreateUser();
            if (user.equals(createUser)) {
                count = map.get("create");
            } else {
                count = map.get("refer");
            }

            if (!Constants.FINAL_STATE.equals(dataStatus)) {
                count.put("undone", (Integer) count.get("undone") + 1);
                finish = false;
            } else {
                count.put("done", (Integer) count.get("done") + 1);
            }
        }
        node.addAttachment("finish", finish);
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
