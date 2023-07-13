/**  
 * @Title: ProjectPlanServiceImpl.java 
 * @Package com.cnpc.epai.core.worktask.service 
 * @Description: TODO 
 * @author cuijiaqi
 * @date 2021年3月19日 下午1:38:25 
 * {修改记录：cuijiaqi 2021年3月19日 下午1:38:25}
*/

package com.cnpc.epai.core.worktask.service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.worktask.domain.ProjectPlan;
import com.cnpc.epai.core.worktask.repository.ProjectPlanRepository;
import com.cnpc.epai.core.worktask.repository.ProjectRepository;

/** 
 * @Description: TODO 
 * @author cuijiaqi
 * @version 1.0.11 
 */
@Service
public class ProjectPlanServiceImpl implements ProjectPlanService {

	@Autowired
	ProjectPlanRepository planRepo;
	@Autowired
	ProjectRepository projectRepo;
	
	@Override
	public List<ProjectPlan> findProjectPlan(String projectId, Integer state) throws BusinessException{
		if(state == null) {
			return planRepo.findByProjectIdAndBsflag(projectId, "N");
		} else {
			return planRepo.findByProjectIdAndStateAndBsflag(projectId, state, "N");
		}
	}


	public List<ProjectPlan> saveProjectPlan(String projectId, List<ProjectPlan> planList) throws BusinessException{
		for(ProjectPlan plan: planList) {
			plan.setProjectId(projectId);
			if(plan.getState()==null) {
				plan.setState(1);
			}
		}
		List<ProjectPlan> planListForDelete = planRepo.findByProjectIdAndBsflag(projectId, "N");
		planRepo.delete(planListForDelete);
		planList = planRepo.save(planList);
		return planList;
	}
	
	public ProjectPlan updateProjectPlan(ProjectPlan plan) throws BusinessException{
		return planRepo.save(plan);
	}
	
	public ProjectPlan updatePlanProcess(String planId, Integer planProcess) throws BusinessException{
		ProjectPlan projectPlan = planRepo.findOne(planId);
		projectPlan.setPlanProgress(planProcess);
		projectPlan = planRepo.save(projectPlan);
		List<ProjectPlan> planList = planRepo.findByProjectIdAndBsflag(projectPlan.getProjectId(), "N");
		
		int schedule = 0;
		for(ProjectPlan plan : planList) {
			schedule = schedule + (plan.getPlanProgress()==null?0:plan.getPlanProgress())*plan.getPlanWeightings();
		}	
		schedule = schedule<=0?0: schedule>=10000?100: schedule/100;
		projectRepo.updateProjectSchedule(projectPlan.getProjectId(), schedule);
		return projectPlan;
	}
	
	public List<ProjectPlan> submitProjectPlan(String projectId) throws BusinessException{
		List<ProjectPlan> planList = planRepo.findByProjectIdAndBsflag(projectId, "N");
		planList = planList.stream().map(p->{p.setState(2); return p;}).collect(Collectors.toList());
		planList = planRepo.save(planList);
		projectRepo.updateProjectSchedule(projectId, 0);
		projectRepo.delAllmilestone(projectId);
		return planList;
	}
	
	public Date[] getDate(String projectId) throws BusinessException{
		Date[] rtnArr = new Date[2];
		List<ProjectPlan> planList = findProjectPlan(projectId, 2);
		if(planList!=null && planList.size()>0) {
			rtnArr[0] = planList.stream().map(i->i.getStartDate()).min(Comparator.comparingLong(j->j.getTime())).get();
			rtnArr[1] = planList.stream().map(i->i.getEndDate()).max(Comparator.comparingLong(j->j.getTime())).get();
		} else {
			List<Map<String,Object>> projectList = projectRepo.findByids(projectId);
			if(projectList!=null && projectList.size()>0) {
				rtnArr[0] =new Date(Long.parseLong(projectList.get(0).get("planStartTime").toString()));
				rtnArr[1] = new Date(Long.parseLong(projectList.get(0).get("planEndTime").toString()));
			}
		}
		return rtnArr;
	}
}
