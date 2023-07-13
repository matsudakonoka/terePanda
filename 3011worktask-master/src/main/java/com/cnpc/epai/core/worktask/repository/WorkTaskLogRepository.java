package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.WorkTaskLog;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 任务日志服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface WorkTaskLogRepository extends StringIdRepository<WorkTaskLog> {

    /**
     * 根据任务id删除日志
     * @param workTaskId
     * @return
     */
    @Transactional
    @Modifying
    @Query("delete from WorkTaskLog a where a.workTask.id=:workTaskId")
    int deleteByWorkTaskId(@Param("workTaskId") String workTaskId);

    /**
     * 根据任务id以及操作类型 删除日志
     * @param workTaskId
     * @param operType
     * @return
     */
    @Transactional
    @Modifying
    @Query("delete from WorkTaskLog a where a.workTask.id=:workTaskId and a.operType != :operType")
    int deleteByWorkTaskIdExceptOperType(@Param("workTaskId") String workTaskId,@Param("operType") String operType);
}
