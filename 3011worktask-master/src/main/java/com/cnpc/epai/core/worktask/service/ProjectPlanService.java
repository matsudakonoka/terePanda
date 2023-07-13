package com.cnpc.epai.core.worktask.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.worktask.domain.ProjectPlan;

/** 
 * @Description: TODO 
 * @author cuijiaqi
 * @version 1.0.11 
 */
@Service
public interface ProjectPlanService {
   List<ProjectPlan> findProjectPlan(String projectId, Integer state) throws BusinessException;
   List<ProjectPlan> saveProjectPlan(String projectId, List<ProjectPlan> planList) throws BusinessException;
   ProjectPlan updateProjectPlan(ProjectPlan plan) throws BusinessException;
   ProjectPlan updatePlanProcess(String planId, Integer planProcess) throws BusinessException;
   List<ProjectPlan> submitProjectPlan(String projectId) throws BusinessException;
   Date[] getDate(String projectId) throws BusinessException;
}
