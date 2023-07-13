package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.WorkTask;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.Map;

/**
 * @Description: 任务服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface WorkTaskRepository extends StringIdRepository<WorkTask> {

    /**
     * 根据标志位获取所有任务
     * @param bsflag N/Y
     * @param pageable 分页
     * @return 分页任务列表
     */
    Page<WorkTask> findByBsflag(String bsflag, Pageable pageable);


    @Query(value = "select b.data_region from SR_TASK_MGR a,SR_PROJECT_WORKROOM b where a.WORKROOM_ID=b.WORKROOM_ID and a.TASK_ID= ?1",nativeQuery = true)
    String findDataRegion(String taskId);

    /**
     * 根据projectId获取任务
     * @param projectId
     * @param bsflag N/Y
     * @param pageable
     * @return 分页任务列表
     */
    @Query("select a from WorkTask a where a.projectId = :projectId and a.bsflag = :bsflag")
    Page<WorkTask> findByProjectId(@Param("projectId")String projectId,@Param("bsflag")String bsflag,Pageable pageable);


    /**
     * 根据projectIdList获取任务
     * @param projectIdList
     * @return 分页任务列表
     */
    @Query("select a.projectId,count(a.projectId) from WorkTask a where a.projectId in :projectIdList and a.bsflag='N' group by a.projectId")
    List<Object[]> countAllByProjectIds(@Param("projectIdList")List projectIdList);


    @Query("select a from WorkTask a where a.projectId = :projectId and a.currentState in :statusList and a.bsflag='N' ")
    Page<WorkTask> findByProjectIdAndStatus(@Param("projectId")String projectId,@Param("statusList")List<String> statusList, Pageable page);

    /**
     * 根据projectId获取任务数量
     * @param projectId
     */
    @Query("select a.currentState,count(a.currentState) from WorkTask a where a.bsflag = 'N' and a.projectId = :projectId GROUP BY a.currentState")
    List<Object[]> findByprojectIdForStatus(@Param("projectId")String projectId);

    @Query("select a.currentState,count(a.currentState) from WorkTask a where a.projectId=:projectId and a.projectId is not null and a.bsflag=:bsflag group by a.currentState ")
    List<Object> findGroupByCurrentState(@Param("projectId")String projectId, @Param("bsflag")String bsflag);

    @Query("select a.userId, b.currentState, count(b.currentState) from WorkTaskAssign a, WorkTask b where a.workTask.id = b.id and a.projectId = :projectId and b.bsflag = 'N' group by b.currentState,a.userId")
    List<Object[]> countUserTaskState(@Param("projectId")String projectId);
}
