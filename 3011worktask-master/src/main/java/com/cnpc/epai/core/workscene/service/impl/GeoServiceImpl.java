package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.workscene.commom.Constants;
import com.cnpc.epai.core.workscene.entity.Geo;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.mapper.GeoMapper;
import com.cnpc.epai.core.workscene.service.DataService;
import com.cnpc.epai.core.workscene.service.GeoService;
import com.cnpc.epai.core.workscene.service.WorkService;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GeoServiceImpl extends ServiceImpl<GeoMapper, Geo> implements GeoService {
    private static final String WELL = "井";
    private static final String MTID = "CD_GEO_UNIT";
    private static final String KEY = "PROJECT_ID";


    @Autowired
    private DataService dataService;

    @Autowired
    private WorkService workService;

    @Transactional
    @Override
    public void configGeoObjects(String workId, Map[] geos) {
        QueryWrapper deleteWrapper = new QueryWrapper();
        deleteWrapper.eq("work_id", workId);
        deleteWrapper.eq("source", Constants.DATA_SELECT);
        remove(deleteWrapper);
//        for (Geo geo : geos) {
//            QueryWrapper<Geo> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("work_id", workId).eq("geo_obj_id", geo.getGeoObjId());
//            Geo one = getOne(queryWrapper);
//            if (one == null) {
//                geo.setWorkId(workId);
//                geo.setSource(Constants.DATA_SELECT);
//                save(geo);
//            }
//        }
        QueryWrapper<Geo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("work_id", workId);
        Geo one = getOne(queryWrapper);
        one.setChildObjects(JSON.toJSONString(geos));
        saveOrUpdate(one);
    }

    @Override
    public Object getGeoTypeCount(String workId) {
        Work work = workService.getById(workId);
        if (work == null)
            throw new IllegalArgumentException("工作不存在");

        QueryWrapper<Geo> queryWrapper = new QueryWrapper<Geo>();

        List<String> types = geoTypeSuper(work.getGeoType());
        queryWrapper.in("geo_type", types).eq("work_id", workId);
        List<Geo> geos = list(queryWrapper);
        List<Map<String, Object>> count = new ArrayList<>();
        countInit(count);

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Geo geo : geos) {
            String geoType = geo.getGeoType();
            List<Map<String, Object>> data;
            if (WELL.equals(geoType)) {
                String wellId = geo.getGeoObjId();
                data = dataService.getData("CD_WELL", "WELL_ID", wellId);
            } else {
                String projectId = geo.getGeoObjId();
                data = dataService.getData(MTID, KEY, projectId);
            }
            for (Map<String, Object> object : data) {
                String projectId = (String) object.get("PROJECT_ID");
                String wellId = (String) object.get("WELL_ID");
                if (wellId != null) {
                    projectId = wellId;
                }
                if (!result.containsKey(projectId)) {
                    result.put(projectId, object);
                }
                list.add(object);
            }
        }
        searchParent(list, result);
        countObject(result, count);

        int csize = count.size();
        int rsize = csize - types.size();
        return count.subList(rsize, csize);
    }

    @Override
    public Object getGeoObject(String workId, String name, String type) {
        // 查询已有地质体对象
        List<Geo> geos = new ArrayList<Geo>();
        List<Geo> geos00 = list(new QueryWrapper<Geo>().eq("work_id", workId));
        JSONArray jsonArray = JSON.parseArray(geos00.get(0).getChildObjects());
        {//研究对象添加进去
            Geo obj = geos00.get(0);
            if(type==null || obj.getGeoType().equals(type)){
                obj.setChildObjects("");
                geos.add(obj);
            }
        }
        if(jsonArray!=null){
            for(Object o : jsonArray){
                Map <String,Object> ret = (Map<String, Object>) o;//取出list里面的值转为map
                Geo obj= new Geo();//取出list里面的值转为map
                if(ret.get("geoObjId")!=null && ret.get("geoObjName")!=null && ret.get("geoType")!=null){
                    if(type==null || ret.get("geoType").toString().equals(type)){
                        obj.setGeoObjId(ret.get("geoObjId").toString());
                        obj.setGeoObjName(ret.get("geoObjName").toString());
                        obj.setGeoType(ret.get("geoType").toString());
                        geos.add(obj);
                    }
                }
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Map<String, Object>> oldResult = new HashMap<>();
        Map<String, Map<String, Object>> result = new HashMap<>();
        List<Map<String, Object>> data=new ArrayList<>();
        for (Geo geo : geos) {
            List<Map<String, Object>> dataa;
            String geoType = geo.getGeoType();
            if (WELL.equals(geoType)) {
                String wellId = geo.getGeoObjId();
                dataa = dataService.getData("CD_WELL", "WELL_ID", wellId);
            }else if("储量区块".equals(type)||"圈闭".equals(type)){
                String dsid = geo.getGeoObjId();
                dataa=dataService.getXreList(dsid, type);
                for(Map<String, Object> ret : dataa){
                    String PROJECT_ID=ret.get("structuralLevel2Id")==null?"":ret.get("structuralLevel2Id").toString();
                    String PROJECT_NAME=ret.get("structuralLevel2")==null?"":ret.get("structuralLevel2").toString();
                    String structuralLevel1=ret.get("structuralLevel1")==null?"":ret.get("structuralLevel1").toString();
                    String PARENT_ID=ret.get("structuralLevel1Id")==null?"":ret.get("structuralLevel1Id").toString();
                    String basinId=ret.get("basinId")==null?"":ret.get("basinId").toString();
                    String basinName=ret.get("basinName")==null?"":ret.get("basinName").toString();
                    if(PROJECT_ID!=null && !PROJECT_ID.equals("")){
                        ret.put("PROJECT_ID",PROJECT_ID);
                        ret.put("PROJECT_NAME",PROJECT_NAME);
                        ret.put("PARENT_ID",PARENT_ID);
                    }
                }
            } else{
                String projectId = geo.getGeoObjId();
                dataa = dataService.getData(MTID, KEY, projectId);
            }
            for (Map<String, Object> object : dataa) {
                data.add(object) ;
            }
        }
        for (Map<String, Object> object : data) {
            String projectId = (String) object.get("PROJECT_ID");
            String wellId = (String) object.get("WELL_ID");
            if (wellId != null) {
                projectId = wellId;
            }
            if (!result.containsKey(projectId)) {
                result.put(projectId, object);
            }
            if (!oldResult.containsKey(projectId)) {
                oldResult.put(projectId, object);
            }
            list.add(object);
        }
        searchParent(list, result);

        // 过滤掉最小等级
        if ("井".equals(type)) {
            for (Map.Entry<String, Map<String, Object>> entry : oldResult.entrySet()) {
                String projectId = entry.getKey();
                result.remove(projectId);
            }
        }

        // 根据名称过滤，如果需要
        List<String> remove = new ArrayList<>();
        if (name != null) {
            for (Map.Entry<String, Map<String, Object>> entry : result.entrySet()) {
                Map<String, Object> value = entry.getValue();
                String wellName = (String) value.get("WELL_COMMON_NAME");
                String projectName = (String) value.get("PROJECT_NAME");
                if (!((wellName != null && wellName.indexOf(name) != -1) ||
                        (projectName != null && projectName.indexOf(name) != -1))) {
                    remove.add(entry.getKey());
                }
            }
        }
        for (String key : remove) {
            result.remove(key);
        }

        // 生成地质树
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : result.entrySet()) {

            Map<String, Object> value = entry.getValue();
            value.put("UNIT_TYPE", getType(value));

            String wellId = (String) value.get("WELL_ID");
            String parentId = (String) value.get("PARENT_ID");
            String PROJECT_ID = (String) value.get("PROJECT_ID");

            //if ("井".equals(type)) {
            if (parentId == null && wellId != null) {
                parentId = PROJECT_ID;
            }
            if ((parentId == null && wellId == null)) {
                tree.add(value);
            }
            if (parentId != null && !result.containsKey(parentId)) {
                tree.add(value);
            }
            if (parentId != null && result.containsKey(parentId)) {
                Map<String, Object> map = result.get(parentId);
                List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
                if (children == null) {
                    children = new ArrayList<>();
                }
                children.add(value);
                map.put("children", children);
            }
        }

        Map<String, Object> res = new HashMap<>(2);
        res.put("tree", tree);
        return res;
    }

    private Map<String, Object> mapCopy(Map<String, Object> map) {
        HashMap<String, Object> copyMap = new HashMap<>();
        copyMap.putAll(map);
        return copyMap;
    }

    private void searchParent(List<Map<String, Object>> list, Map<String, Map<String, Object>> result) {
        for (Map<String, Object> object : list) {
            String parentId = (String) object.get("PARENT_ID");
            String singleValue;
            String wellId = (String) object.get("WELL_ID");
            if (parentId == null && wellId == null)
                continue;

            if (wellId != null) {
                singleValue = (String) object.get("PROJECT_ID");
            } else {
                singleValue = (String) object.get("PARENT_ID");
            }
            List<Map<String, Object>> data = dataService.getData(MTID, KEY, singleValue);
            for (Map<String, Object> obj : data) {
                String projectId = (String) obj.get("PROJECT_ID");
                if (!result.containsKey(projectId)) {
                    result.put(projectId, obj);
                }
            }
            searchParent(data, result);
        }
    }
    // 地质体对象分类
    private void countInit(List<Map<String, Object>> count) {
        Map<String, Object> map1 = new HashMap<>(2);
        map1.put("unit_name", "井");
        map1.put("number", 0);
        map1.put("content", new ArrayList<>());
        count.add(map1);

        Map<String, Object> map2 = new HashMap<>(2);
        map2.put("unit_name", "油气藏");
        map2.put("number", 0);
        map2.put("content", new ArrayList<>());
        count.add(map2);

        Map<String, Object> map3 = new HashMap<>(2);
        map3.put("unit_name", "圈闭");
        map3.put("number", 0);
        map3.put("content", new ArrayList<>());
        count.add(map3);

        Map<String, Object> map4 = new HashMap<>(2);
        map4.put("unit_name", "坳陷");
        map4.put("number", 0);
        map4.put("content", new ArrayList<>());
        count.add(map4);

        Map<String, Object> map5 = new HashMap<>(2);
        map5.put("unit_name", "盆地");
        map5.put("number", 0);
        map5.put("content", new ArrayList<>());
        count.add(map5);
    }



    private void countObject(Map<String, Map<String, Object>> result, List<Map<String, Object>> count) {

        for (Map.Entry<String, Map<String, Object>> entry : result.entrySet()) {
            Map<String, Object> object = entry.getValue();
            String type = getType(object);
            if (type != null) {
                if (type.equals("井")) {
                    Map<String, Object> map = count.get(0);
                    fillValue(map, object);
                }
                if (type.equals("油气藏")) {
                    Map<String, Object> map = count.get(1);
                    fillValue(map, object);
                }
                if (type.equals("圈闭")) {
                    Map<String, Object> map = count.get(2);
                    fillValue(map, object);
                }
                if (type.equals("坳陷")) {
                    Map<String, Object> map = count.get(3);
                    fillValue(map, object);
                }
                if (type.equals("盆地")) {
                    Map<String, Object> map = count.get(4);
                    fillValue(map, object);
                }
            }
        }

    }

    private List<String> geoTypeSuper(String type) {
        List<String> list = new ArrayList<>();list.add("井");
        list.add("油气藏");
        list.add("圈闭");
        list.add("坳陷");
        list.add("盆地");

        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).equals(type)) {
                return list.subList(i, size);
            }
        }
        return null;
    }

    private void fillValue(Map<String, Object> map, Map<String, Object> value) {
        map.put("number", (int) map.get("number") + 1);
        List<Object> content = (List<Object>) map.get("content");
        content.add(value);
    }

    @Override
    public Object getChildData(String realValue, String type, String name) {
        List<Object> result = new ArrayList<>();
        List<String> candidate = new ArrayList<>();
        candidate.add(realValue);
        searchChildData(result, candidate);

        List<Object> remove = new ArrayList<>();
        if (name != null) {
            for (Object obj : result) {
                Map<String, String> map = (Map<String, String>) obj;
                String wellName = map.get("WELL_COMMON_NAME");
                String projectName = map.get("PROJECT_NAME");
                if (!((wellName != null && wellName.indexOf(name) != -1) ||
                        (projectName != null && projectName.indexOf(name) != -1))) {
                    remove.add(obj);
                }
            }
        }
        // 根据名称过滤
        result.removeAll(remove);
        // 根据类型过滤
        result.removeAll(filterType(type, result));


        for (Object obj : result) {
            Map<String, Object> map = (Map<String, Object>) obj;
            map.put("UNIT_TYPE", getType(map));
        }


        return result;
    }


    public void searchChildData(List<Object> result, List<String> candidate) {
        List<Map<String, Object>> childData = dataService.getChildData("CD_GEO_UNIT", "PARENT_ID", candidate);
        List<String> newCandidate = new ArrayList<>();

        if (childData == null || childData.size() == 0) {
            childData = dataService.getChildData("CD_WELL", "PROJECT_ID", candidate);
            if (childData != null && childData.size() > 0) {

                for (Map<String, Object> data : childData) {
                    result.add(data);
                }
            }
            return;
        }

        for (Map<String, Object> data : childData) {
            String projectId = (String) data.get("PROJECT_ID");
            result.add(data);
            newCandidate.add(projectId);
        }
        searchChildData(result, newCandidate);
    }

    private String getType(Map<String, Object> object) {
        String type = null;
        String unitLevel = (String) object.get("UNIT_LEVEL");
        String wellId = (String) object.get("WELL_ID");

        if (unitLevel == null && wellId != null) {
            type = "井";
        }
        if (unitLevel != null) {
            if (unitLevel.equals("ATSVZB100003447")) {
                type = "油气藏";
            }
            if (unitLevel.equals("ATSVZB100003441") || unitLevel.equals("ATSVZB100003443")
                    || unitLevel.equals("ATSVZB100005606")) {
                type = "圈闭";
            }
            if (unitLevel.equals("ATSVZB100003440")) {
                type = "坳陷";
            }
            if (unitLevel.equals("ATSVZB100003439")) {
                type = "盆地";
            }
        }
        return type;
    }

    private List<Object> filterType(String type, List<Object> result) {
        List<Object> filter = new ArrayList<>();
        for (Object object : result) {
            Map<String, Object> map = (Map<String, Object>) object;
            if (!type.equals(getType(map))) {
                filter.add(object);
            }
        }
        return filter;
    }


}
