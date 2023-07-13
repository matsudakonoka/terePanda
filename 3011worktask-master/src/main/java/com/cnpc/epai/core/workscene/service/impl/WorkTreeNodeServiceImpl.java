package com.cnpc.epai.core.workscene.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.workscene.entity.WorkNavigateTreeNode;
import com.cnpc.epai.core.workscene.mapper.WorkTreeNodeMapper;
import com.cnpc.epai.core.workscene.service.WorkNavigateTreeNodeService;
import org.springframework.stereotype.Service;

@Service
public class WorkTreeNodeServiceImpl extends ServiceImpl<WorkTreeNodeMapper, WorkNavigateTreeNode> implements WorkNavigateTreeNodeService {

}
