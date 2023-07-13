package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.Template;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface TemplateRepository extends StringIdRepository<Template> {
}
