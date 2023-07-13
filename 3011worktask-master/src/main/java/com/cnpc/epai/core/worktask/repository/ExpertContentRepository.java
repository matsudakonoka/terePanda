package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.core.worktask.domain.ExpertContent;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpertContentRepository extends JpaRepository<ExpertContent, String>, JpaSpecificationExecutor<ExpertContent> {
}
