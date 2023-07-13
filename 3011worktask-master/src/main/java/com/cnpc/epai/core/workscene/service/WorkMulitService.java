package com.cnpc.epai.core.workscene.service;

import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.pojo.vo.WorkMulitVo;

public interface WorkMulitService {
    ApiResult createWorkByReport(WorkMulitVo workMulitVo);

}
