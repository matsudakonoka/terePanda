package com.cnpc.epai.core.workscene.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cnpc.epai.common.util.SpringManager;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AutomaticMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = SpringManager.getCurrentUser().getUserId();
        this.setFieldValByName("createUser", userId, metaObject);
        this.setFieldValByName("updateUser", userId, metaObject);
        this.setFieldValByName("createDate", new Date(), metaObject);
        this.setFieldValByName("updateDate", new Date(), metaObject);
        this.setFieldValByName("usageCount", 0, metaObject);
        this.setFieldValByName("reportOutput", false, metaObject);
        this.setFieldValByName("bsflag", "N" , metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = SpringManager.getCurrentUser().getUserId();
        this.setFieldValByName("updateDate", new Date(), metaObject);
        this.setFieldValByName("updateUser", userId, metaObject);
    }
}
