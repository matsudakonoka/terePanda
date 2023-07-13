package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.SrWorkMsg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SrWorkMsgRepository extends StringIdRepository<SrWorkMsg> {

    List<SrWorkMsg> findByChargeUser(String chargeUser);
}
