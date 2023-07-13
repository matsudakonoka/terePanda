package com.cnpc.epai.core.workscene.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.workscene.entity.WorkNode;
import com.cnpc.epai.core.workscene.entity.WorkTask;
import com.cnpc.epai.core.workscene.mapper.WorkNodeMapper;
import com.cnpc.epai.core.workscene.pojo.vo.ReportTemplateVo;
import com.cnpc.epai.core.workscene.service.WorkNodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkNodeServiceImpl extends ServiceImpl<WorkNodeMapper, WorkNode> implements WorkNodeService {


    @Override
    public List<WorkNode> getNode(String workId) {
        return list(new QueryWrapper<WorkNode>().eq("work_id", workId));
    }

    @Override
    public boolean isNodeStart(String workId, String nodeId) {
        QueryWrapper<WorkNode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id", workId).eq("tree_node_id", nodeId);
        WorkNode one = getOne(queryWrapper);
        if (one == null) {
            System.out.println("当前工作无此节点");
            return false;
        }
        return one.isStart();
    }

    @Override
    public void startNode(String workId, String nodeId) {
        UpdateWrapper<WorkNode> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("start", true).eq("work_id", workId).eq("tree_node_id", nodeId);
        update(updateWrapper);
    }
}
