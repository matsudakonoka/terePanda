package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.core.worktask.domain.ExpertContent;
import com.cnpc.epai.core.worktask.domain.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, String>, JpaSpecificationExecutor<Profession> {
}
