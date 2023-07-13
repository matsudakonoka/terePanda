package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.worktask.domain.Template;
import com.cnpc.epai.core.worktask.service.TemplateService;
import com.cnpc.epai.core.worktask.util.RestPageImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/core/worktask/template")
public class TemplateController {

    @Autowired
    TemplateService templateService;

    @RequestMapping(value = "/getall", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询所有场景模板", notes = "", code =200,produces="application/json")
    public List<Template> getAllTemplate(
            @ApiParam(name = "templateId", value = "模板ID", required = false) @RequestParam(name = "templateId", defaultValue="") String templateId,
            @ApiParam(name = "templateName", value = "模板名称", required = false) @RequestParam(name = "templateName", defaultValue="") String templateName,
            @ApiParam(name = "startTime", value = "开始时间", required = false) @RequestParam(name="startTime", defaultValue="") Date startTime,
            @ApiParam(name = "endTime", value = "结束时间", required = false) @RequestParam(name="endTime", defaultValue="") Date endTime,
            Pageable pageable) {
        Page<Template> rtnPage = new RestPageImpl<>();
        try {
            return templateService.getAll(templateId,templateName,startTime,endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/savetemplate", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存业务场景模板", notes = "", code =200,produces="application/json")
    public Template saveTemplate(Template template) {
        Page<Template> rtnPage = new RestPageImpl<>();
        try {
            return templateService.saveTemplate(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }







}
