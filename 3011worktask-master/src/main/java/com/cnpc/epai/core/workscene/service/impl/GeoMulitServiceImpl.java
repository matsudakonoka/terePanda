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
import com.cnpc.epai.core.workscene.service.GeoMulitService;
import com.cnpc.epai.core.workscene.service.WorkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeoMulitServiceImpl extends ServiceImpl<GeoMapper, Geo> implements GeoMulitService {
    private static final String WELL = "井";
    private static final String MTID = "CD_GEO_UNIT";
    private static final String KEY = "PROJECT_ID";


    @Autowired
    private DataService dataService;

    @Autowired
    private WorkService workService;

    @Transactional
    @Override
    public void configGeoObjects(String workId, Geo[] geos) {
        QueryWrapper deleteWrapper = new QueryWrapper();
        deleteWrapper.eq("work_id", workId);
        deleteWrapper.eq("source", Constants.DATA_SELECT);
        remove(deleteWrapper);

        for (Geo geo : geos) {
            QueryWrapper<Geo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("work_id", workId).eq("geo_obj_id", geo.getGeoObjId());
            Geo one = getOne(queryWrapper);
            if (one == null) {
                geo.setWorkId(workId);
                geo.setSource(Constants.DATA_SELECT);
                save(geo);
            }
        }
    }
    public Object getGeoTypeCountEx(String workId){
        Work work = workService.getById(workId);
        if (work == null)
            throw new IllegalArgumentException("工作不存在");
        QueryWrapper<Geo> queryWrapper = new QueryWrapper<Geo>();
        queryWrapper.eq("work_id",workId);
        List<Geo> list = list(queryWrapper);
        if(list==null || list.isEmpty()){
            return null;
        }
        Geo geo = list.get(0);
        List<Object> geoList = new ArrayList<>();
        geoList.add(geo);
        if(StringUtils.isNotEmpty(geo.getChildObjects())){
            JSONArray jsonArray = JSON.parseArray(geo.getChildObjects());
            for(Object o : jsonArray){
                geoList.add(o);
            }
        }
//        Map<String, Object> map1 = new HashMap<>(2);
//        map1.put("unit_name", "井");
//        map1.put("number", 0);
//        map1.put("content", new ArrayList<>());
//        count.add(map1);
        List<Map<String,Object>> retList = new ArrayList<>();
        countInit(retList);
        //分类
        for(Object o : geoList){
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            for(Map<String,Object> map : retList){
                String typeName = typeName = jsonObject.getString("geoType");;

                if(StringUtils.equals(map.get("unit_name").toString(),typeName)){
                    ((List)map.get("content")).add(jsonObject);
                    map.put("number",Integer.parseInt(map.get("number").toString())+1);
                }
            }
        }

        return retList;
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
    public Object getGeoObject(String workId, String name) {
        // 查询已有地质体对象
        List<Geo> geos = list(new QueryWrapper<Geo>().eq("work_id", workId));
        /*List<Geo> geos = list(new QueryWrapper<Geo>().eq("work_id", workId).eq("geo_type", type));

        if (geos == null || geos.size() == 0) {
            return new ArrayList<>();
        }*/

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Map<String, Object>> oldResult = new HashMap<>();
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
                if (!oldResult.containsKey(projectId)) {
                    oldResult.put(projectId, object);
                }
                list.add(object);
            }
        }

        searchParent(list, result);

        // 过滤掉最小等级
        for (Map.Entry<String, Map<String, Object>> entry : oldResult.entrySet()) {
            String projectId = entry.getKey();
            result.remove(projectId);
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

            if (parentId == null && wellId != null) {
                parentId = (String) value.get("PROJECT_ID");
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

//        Map<String, Object> map4 = new HashMap<>(2);
//        map4.put("unit_name", "坳陷");
//        map4.put("number", 0);
//        map4.put("content", new ArrayList<>());
//        count.add(map4);

        Map<String, Object> map5 = new HashMap<>(2);
        map5.put("unit_name", "盆地");
        map5.put("number", 0);
        map5.put("content", new ArrayList<>());
        count.add(map5);

        Map<String, Object> map6 = new HashMap<>(2);
        map6.put("unit_name", "储量区块");
        map6.put("number", 0);
        map6.put("content", new ArrayList<>());
        count.add(map6);

        Map<String, Object> map7 = new HashMap<>(2);
        map7.put("unit_name", "评价单元");
        map7.put("number", 0);
        map7.put("content", new ArrayList<>());
        count.add(map7);

        Map<String, Object> map8 = new HashMap<>(2);
        map8.put("unit_name", "一级构造");
        map8.put("number", 0);
        map8.put("content", new ArrayList<>());
        count.add(map8);

        Map<String, Object> map9 = new HashMap<>(2);
        map9.put("unit_name", "二级构造");
        map9.put("number", 0);
        map9.put("content", new ArrayList<>());
        count.add(map9);

        Map<String, Object> map10 = new HashMap<>(2);
        map10.put("unit_name", "三级构造");
        map10.put("number", 0);
        map10.put("content", new ArrayList<>());
        count.add(map10);

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
        List<String> list = new ArrayList<>();
        list.add("井");
        list.add("油气藏");
        list.add("圈闭");
        list.add("盆地");
        list.add("储量区块");
        list.add("评价单元");
        list.add("一级构造");
        list.add("二级构造");
        list.add("三级构造");

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
            if (unitLevel.equals("ATSVZB100003440")) {
                type = "一级构造";
            }
            if (unitLevel.equals("ATSVZB100003441")) {
                type = "二级构造";
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
