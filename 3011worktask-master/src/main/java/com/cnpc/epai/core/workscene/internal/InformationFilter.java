package com.cnpc.epai.core.workscene.internal;

import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.pojo.vo.ChargeUserVo;
import com.cnpc.epai.core.workscene.service.ResourceService;
import com.cnpc.epai.core.workscene.service.WorkSceneTaskService;
import com.cnpc.epai.core.worktask.service.WorkObjectService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformationFilter implements Filter {

    private ApplicationContext applicationContext;
    private Filter filter;

    public InformationFilter() {

    }

    public InformationFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Node invoke(Node result) {
        Map<String, List<Object>> nodeMap = nodeInfo(result);
        try {
            recursion(result, nodeMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter == null ? result : filter.invoke(result);
    }

    private Map<String, List<Object>> nodeInfo(Node node) {
        Map<String, List<Object>> result = new HashMap<>();
        String workId = (String) node.getAttachment("workId");
        WorkSceneTaskService workSceneTaskService = applicationContext.getBean(WorkSceneTaskService.class);
        List<WorkTask> workTasks = workSceneTaskService.getWorkTask(workId);
        for (WorkTask task : workTasks) {
            String chargeUserId = task.getChargeUserId();
            String chargeUserName = task.getChargeUserName();
            ChargeUserVo chargeUserVo = new ChargeUserVo();
            chargeUserVo.setChargeUserId(chargeUserId);
            chargeUserVo.setChargeUserName(chargeUserName);
            String treeNodeIds = task.getTreeNodeIds();
            if (StringUtils.isEmpty(treeNodeIds))
                continue;
            String[] nodes = treeNodeIds.split(",");
            for (String n : nodes) {
                List<Object> list;
                if (!result.containsKey(n)) {
                    list = new ArrayList<>();
                } else {
                    list = result.get(n);
                }
                list.add(chargeUserVo);
                result.put(n, list);
            }
        }
        return result;
    }

    private void recursion(Node node, Map<String, List<Object>> nodeMap) {
        if (node == null) {
            return;
        }
        List<Node> child = node.getChild();
        for (Node cNode : child) {
            recursion(cNode, nodeMap);
        }
        addInformation(node, nodeMap);
    }

    private void addInformation(Node node, Map<String, List<Object>> nodeMap) {
        Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) InternalThreadLocal.get();
        Map<String, Object> objectMap = map.get(node.getAttachment(Constants.NODE_ID));
        node.addAttachment(Constants.RESOURCE_EXPERT, objectMap.get(Constants.RESOURCE_EXPERT));
        node.addAttachment(Constants.RESOURCE_STATION, objectMap.get(Constants.RESOURCE_STATION));
        node.addAttachment(Constants.CHARGEUSERS, nodeMap.get(node.getAttachment(Constants.NODE_ID)));
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
