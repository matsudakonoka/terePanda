package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.SrWorkCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SrWorkCollectRepository extends StringIdRepository<SrWorkCollect> {

}
