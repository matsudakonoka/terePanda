package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.SrWorkNode;
import com.cnpc.epai.core.worktask.domain.SrWorkTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public interface SrWorkTaskService {

    //首页-业务场景筛选列表
    JSONArray getScenesTemplate(HttpServletRequest request);

    //首页-工作列表筛选、搜索按钮
    Page<SrWorkTask> getWorkList(Pageable page, JSONObject data);

    //工作查看-工作成果导航&&工作成果导航关键词搜索
    Object getResultsTree(HttpServletRequest request, JSONObject data);

    //工作查看-工作成果导航-数据集状态显示
    List<Tree> getResultsTreeStatus(HttpServletRequest request, JSONObject data);

    //工作查看-工作详情
    JSONObject getWorkInfoList(HttpServletRequest request, JSONObject data, Pageable page);

    //工作查看-任务分析
    Map getResultList(HttpServletRequest request, JSONObject data, Pageable page);

    //工作查看-成果浏览条件筛选/分页/跳转成果界面
    Map getResultListBy(HttpServletRequest request, JSONObject data, Pageable page);

    //工作查看-项目成员
    Map getResultListUser(HttpServletRequest request, JSONObject data, Pageable page);

    JSONObject getWorkInfoList1(HttpServletRequest request, JSONObject data, Pageable page);

    Map getResultList1(HttpServletRequest request, JSONObject data, Pageable page);

    Map getResultListBy1(HttpServletRequest request, JSONObject data, Pageable page);

    Map getResultListUser1(HttpServletRequest request, JSONObject data, Pageable page);

    List<JSONObject> getResultPage(HttpServletRequest request, JSONObject data, Pageable page);

    Map getFrontPageResult(HttpServletRequest request, Pageable page);

    List<JSONObject> searchData(String projectId, String datasetIds, String wellNames, HttpServletRequest request);
}
