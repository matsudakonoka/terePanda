package com.cnpc.epai.core.worktask.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.worktask.domain.ProjectPlan;
import com.cnpc.epai.core.worktask.service.ProjectPlanService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @Description: controller
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */

@RestController
@RequestMapping("/core/worktask/plan")
public class ProjectPlanController {
    @Autowired
    ProjectPlanService projectPlanService;

    @RequestMapping(value = "/{projectId}/find", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取项目计划",  notes = "", code = 200, produces = "application/json")
    public List<ProjectPlan> findProjectPlan(@ApiParam(name = "projectId", value = "项目id", required = true) @PathVariable("projectId") String projectId,
    		@ApiParam(name = "state", value = "状态") @RequestParam(required = false, defaultValue = "") String state) throws BusinessException {
    	Integer intState = StringUtils.isEmpty(state)?null:Integer.parseInt(state);
    	return projectPlanService.findProjectPlan(projectId, intState);
    }

    @RequestMapping(value = "/{projectId}/save", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存项目计划(批量)",  notes = "", code = 200, produces = "application/json")
    public List<ProjectPlan> saveProjectPlan(@ApiParam(name = "projectId", value = "项目id", required = true) @PathVariable("projectId") String projectId,
    		@ApiParam(name = "planList", value = "[{\"k1\":\"v1\",...},{\"k2\":\"v2\",...},...]") @RequestParam(required = false) String planList) throws BusinessException {
    	List<ProjectPlan> projectPlanList = JSONObject.parseArray(planList, ProjectPlan.class);
    	return projectPlanService.saveProjectPlan(projectId, projectPlanList);
    }
    
    @RequestMapping(value = "/{planId}/updateprocess", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "修改计划进度",  notes = "", code = 200, produces = "application/json")
    public ProjectPlan updatePlan(@ApiParam(name = "planId", value = "计划id", required = true) @PathVariable("planId") String planId,
    		@ApiParam(name = "planProgress", value = "计划进度", required = true) @RequestParam("planProgress") int planProgress) throws BusinessException {
    	return projectPlanService.updatePlanProcess(planId, planProgress);
    }

    @RequestMapping(value = "/{projectId}/submit", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "提交项目计划",  notes = "", code = 200, produces = "application/json")
    public boolean submitProjectPlan(@ApiParam(name = "projectId", value = "项目id", required = true) @PathVariable("projectId") String projectId) throws BusinessException {
    	projectPlanService.submitProjectPlan(projectId);
    	return true;
    }
    
    @RequestMapping(value = "/{projectId}/getdate", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "提取项目日期 ",  notes = "", code = 200, produces = "application/json")
    public Date[] getDate(@ApiParam(name = "projectId", value = "项目id", required = true) @PathVariable("projectId") String projectId) throws BusinessException {
    	return projectPlanService.getDate(projectId);
    }
}
