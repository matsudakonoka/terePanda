package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.core.worktask.domain.SrPersonalTreedata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalTreedataRepository extends JpaRepository<SrPersonalTreedata,String> {

    SrPersonalTreedata findByUserIdAndBsflag(String userId, String bsFlag);
}
