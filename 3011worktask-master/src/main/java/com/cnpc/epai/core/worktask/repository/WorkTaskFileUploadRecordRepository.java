package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.WorkTaskFileUploadRecord;
import org.springframework.stereotype.Repository;

/**
 * 文件上传仓库
 */
@Repository
public interface WorkTaskFileUploadRecordRepository extends StringIdRepository<WorkTaskFileUploadRecord> {

}
