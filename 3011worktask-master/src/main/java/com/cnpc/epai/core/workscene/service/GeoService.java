package com.cnpc.epai.core.workscene.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.Geo;

import java.util.Map;

public interface GeoService extends IService<Geo> {
    void configGeoObjects(String workId, Map[] geos);

    Object getGeoTypeCount(String workId);

    Object getGeoObject(String workId, String name, String type);

    Object getChildData(String realValue, String type, String name);

}
