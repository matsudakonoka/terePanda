package com.cnpc.epai.core.worktask.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.ProjectPlan;

/**
 * @Description: 
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface ProjectPlanRepository extends StringIdRepository<ProjectPlan> {

	List<ProjectPlan> findByProjectIdAndBsflag(String projectId, String bsflag);
	List<ProjectPlan> findByProjectIdAndStateAndBsflag(String projectId, Integer state, String bsflag);
}
