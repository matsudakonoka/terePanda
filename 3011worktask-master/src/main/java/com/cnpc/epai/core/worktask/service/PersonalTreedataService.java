package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;

public interface PersonalTreedataService {
    boolean saveTreeData(JSONArray data);

    JSONArray getTreeData();

    boolean matchNodeDataset(JSONArray data, HttpServletRequest request);

    JSONObject wellDatasetData(String datasetId, JSONArray wellNames, HttpServletRequest request, Pageable pageable);
}
