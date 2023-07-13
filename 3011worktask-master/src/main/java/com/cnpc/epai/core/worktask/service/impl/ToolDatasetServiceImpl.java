package com.cnpc.epai.core.worktask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.worktask.domain.ToolDataset;
import com.cnpc.epai.core.worktask.repository.ToolDataetRepository;
import com.cnpc.epai.core.worktask.service.ToolDatasetService;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ToolDatasetServiceImpl implements ToolDatasetService {

    @Autowired
    ToolDataetRepository toolDataetRepository;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Override
    public List<Map> getDatasetByToolId(String toolId) {
        List<Map> maps = new ArrayList<>();
        List<ToolDataset> toolDatasets = toolDataetRepository.findByToolId(toolId);
        for (ToolDataset toolDataset:toolDatasets){
            Map map = new HashMap();
            map.put("id",toolDataset.getId());
            map.put("datasetId",toolDataset.getDatasetId());
            map.put("datasetName",toolDataset.getDatasetName());
            map.put("viewCode",toolDataset.getRemarks());
            maps.add(map);
        }
        return maps;
    }

    @Override
    public boolean saveAll(String toolId, String toolTypeId, String name, String toolPath, String configParam, List<Map> dataList, HttpServletRequest httpServletRequest) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50000, TimeUnit.MILLISECONDS)
                .readTimeout(50000, TimeUnit.MILLISECONDS)
                .build();
        RequestBody requestBody = new FormBody.Builder().add("id",toolId).add("toolType.id",toolTypeId).add("name",name).add("toolPath",toolPath).add("configParam",configParam).add("isValid","Y").add("showOrder","0").build();
        MediaType JSONs = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://"+ServerAddr+"/research/tool/config/tool")
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s = body.string();
        JSONObject  tool = JSON.parseObject(s);
        List<ToolDataset> toolDatasets = toolDataetRepository.findByToolId((String) tool.get("id"));
        List<String> datasetIds = new ArrayList<>();
        if (toolDatasets != null && toolDatasets.size()!=0){
           datasetIds = toolDatasets.stream().map(ToolDataset::getDatasetId).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
        }
        if (dataList==null || dataList.size()==0){
            return true;
        }

        if (dataList==null || dataList.size()==0){
            return true;
        }
        List<ToolDataset> list = new ArrayList<>();
        toolDataetRepository.delete(toolDatasets);
        for (Map map:dataList){
            ToolDataset toolDataset = new ToolDataset();
            toolDataset.setToolId(toolId);
            toolDataset.setDatasetId((String) map.get("datasetId"));
            toolDataset.setDatasetName((String) map.get("datasetName"));
            toolDataset.setRemarks((String) map.get("viewCode"));
            list.add(toolDataset);
        }


        if (list !=null && list.size()>0){
            toolDataetRepository.save(list);
        }
        return true;
    }

    @Override
    public boolean deleteDataByIds(String dataIds) {
        List<String> dataIdList = new ArrayList<>();
        if(StringUtils.isNotBlank(dataIds)) {
            dataIdList = Arrays.asList(dataIds.split(","));
        }else {
            return true;
        }
        List<ToolDataset> list = new ArrayList<>();
        list = toolDataetRepository.findAll(dataIdList);
        toolDataetRepository.delete(list);
        return true;
    }

    @Override
    public Map<String,Object> searchdata(String datasetCode, String wellName, Integer page, Integer size, HttpServletRequest httpServletRequest) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build();
        String fileter = "";
        if (wellName!=null && wellName!=""){
            fileter = "{" +
                    "\"dataRegions\": [" +
                    "\"TL\"" +
                    "]," +
                    "\"filter\": {" +
                    "\"key\": \"WELL_COMMON_NAME\"," +
                    "\"logic\": \"AND\"," +
                    "\"realValue\": " +
                    "[\"" +
                    wellName +
                    "\"]," +
                    "\"symbol\": \"=\"" +
                    "}" +
                    "}";
        }
        MediaType reqBody = MediaType.parse("application/json; charset=utf-8");
        RequestBody filterBody = RequestBody.create(reqBody,fileter );
        Request request = new Request.Builder()
                .post(filterBody)
                .header("Authorization", httpServletRequest.getHeader("Authorization"))
                .url("http://" + ServerAddr + "/sys/dataservice/udb/"+datasetCode+"?page="+page+"&size="+size)
                .build();
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String res = body.string();
        Map<String,Object> dataMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(res);
        return dataMap;
    }
}
