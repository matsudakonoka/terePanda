package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.domain.ToolDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToolDataetRepository extends JpaRepository<ToolDataset, String>, JpaSpecificationExecutor<ToolDataset> {

    @Query(value = "select * from SR_META_TOOL_DATASET where TOOL_ID = ?1 and bsflag = 'N'",nativeQuery = true)
    List<ToolDataset> findByToolId(String toolId);
}
