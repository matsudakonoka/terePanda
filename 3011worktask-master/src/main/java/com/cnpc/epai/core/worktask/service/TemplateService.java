package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.worktask.domain.Template;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface TemplateService {
    List<Template> getAll(String templateId,String templatename, Date startTime, Date endTime);

    Template saveTemplate(Template template);


}
