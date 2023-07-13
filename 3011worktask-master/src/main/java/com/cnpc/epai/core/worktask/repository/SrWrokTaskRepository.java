package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.SrWorkTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SrWrokTaskRepository extends StringIdRepository<SrWorkTask> {

    @Query(value = "select TASK_ID from SR_WORK_TASK where CHARGE_USER_ID = ?1",nativeQuery = true)
    List<String> selectByUser(String userId);
}
