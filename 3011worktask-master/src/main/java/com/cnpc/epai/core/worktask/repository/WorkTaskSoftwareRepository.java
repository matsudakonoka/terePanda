package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.WorkTaskSoftware;
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
 * @author 王淼
 * @Title: A6
 * @Package com.cnpc.epai.core.worktask.repository
 * @Description: 功能描述
 * @date 11:08 2018/7/10
 * {修改记录：修改人、修改时间、修改内容等}
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface WorkTaskSoftwareRepository extends StringIdRepository<WorkTaskSoftware> {

    /**
     * 根据任务id删除
     * @param workTaskId
     * @return
     */
    @Transactional
    @Modifying
    @Query("delete from WorkTaskSoftware a where a.taskId=:workTaskId")
    int deleteByWorkTaskId(@Param("workTaskId") String workTaskId);

    @Query("select a.softwareId from WorkTaskSoftware a where a.taskId=:taskId")
    List<String> findSoftwareIdByTaskId(@Param("taskId")String taskId);


    List<WorkTaskSoftware> findByTaskId(@Param("taskId")String taskId);
}
