package com.cnpc.epai.core.worktask.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface ToolDatasetService {
    List<Map> getDatasetByToolId(String toolId);

    boolean saveAll(String toolId, String toolTypeId, String name, String toolPath, String configParam, List<Map> dataList, HttpServletRequest httpServletRequest) throws IOException;

    boolean deleteDataByIds(String dataIds);

    Map<String,Object> searchdata(String datasetCode, String wellName, Integer page, Integer size, HttpServletRequest httpServletRequest) throws IOException;
}
