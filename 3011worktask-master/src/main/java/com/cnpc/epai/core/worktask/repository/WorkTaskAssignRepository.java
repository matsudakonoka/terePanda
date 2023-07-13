package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.WorkTask;
import com.cnpc.epai.core.worktask.domain.WorkTaskAssign;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

/**
 * @Description: 人员分配服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface WorkTaskAssignRepository extends StringIdRepository<WorkTaskAssign> {

    /**
     * 根据id获取任务分配
     * @param workTaskId
     * @return
     */
    List<WorkTaskAssign> findByWorkTaskId(String workTaskId);

    /**
     * 获取某个人的分配列表
     * @param projectId
     * @param userId
     * @param page
     * @return 分配列表
     */
    @Query("select a.workTask from WorkTaskAssign a where a.projectId = :projectId and a.userId = :userId and a.bsflag='N' and a.workTask.bsflag='N' ")
    Page<WorkTask> findByProjectIdAndUserId(@Param("projectId")String projectId, @Param("userId")String userId, Pageable page);

    /**
     * 获取某个人的某些状态的分配列表
     * @param projectId
     * @param userId
     * @param statusList 状态列表
     * @param page
     * @return 分配列表
     */
    @Query("select a.workTask from WorkTaskAssign a where a.projectId = :projectId and a.userId = :userId and a.workTask.currentState in :statusList and a.bsflag='N' and a.workTask.bsflag='N' ")
    Page<WorkTask> findByProjectIdAndUserIdAndStatus(@Param("projectId")String projectId, @Param("userId")String userId, @Param("statusList")List<String> statusList, Pageable page);

    /**
     * 根据任务id 获取某个人的分配情况
     * @param workTaskId
     * @param userId
     * @return 分配列表
     */
    @Query("select a from WorkTaskAssign a where a.workTask.id = :worTaskId and a.userId = :userId and a.bsflag='N'")
    List<WorkTaskAssign> findByWorkTaskIdAndUserId(@Param("worTaskId")String workTaskId,@Param("userId")String userId);

    /**
     * 获取某个任务的分配情况
     * @param projectId
     * @param page
     * @return 分配列表
     */
    @Query("select a from WorkTaskAssign a where a.projectId = :projectId and a.workTask.bsflag='N'")
    Page<WorkTaskAssign> findByProjectId(@Param("projectId")String projectId, Pageable page);

    /**
     * 根据任务id以及任务状态获取分配情况
     * @param projectId
     * @param statusList 状态列表
     * @param page
     * @return 分配列表
     */
    @Query("select a from WorkTaskAssign a where a.projectId = :projectId and a.workTask.currentState in :statusList and a.workTask.bsflag='N' ")
    Page<WorkTaskAssign> findByProjectIdAndStatus(@Param("projectId")String projectId,@Param("statusList")List<String> statusList, Pageable page);

    /**
     * 根据任务id删除人员分配
     * @param workTaskId
     * @return
     */
    @Transactional
    @Modifying
    @Query("delete from WorkTaskAssign a where a.workTask.id=:workTaskId")
    int deleteByWorkTaskId(@Param("workTaskId") String workTaskId);
}
