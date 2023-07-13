package com.cnpc.epai.core.workscene.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.entity.Geo;

public interface GeoMulitService extends IService<Geo> {
    void configGeoObjects(String workId, Geo[] geos);

    Object getGeoTypeCount(String workId);
    Object getGeoTypeCountEx(String workId);

    Object getGeoObject(String workId, String name);

    Object getChildData(String realValue, String type, String name);
}
