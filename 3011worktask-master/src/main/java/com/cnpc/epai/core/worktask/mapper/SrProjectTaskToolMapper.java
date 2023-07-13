package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrProjectTaskTool;
import com.cnpc.epai.core.worktask.pojo.SrProjectTaskToolExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrProjectTaskToolMapper {
    int deleteByExample(SrProjectTaskToolExample example);

    int deleteByPrimaryKey(String taskToolId);

    int insert(SrProjectTaskTool record);

    int insertSelective(SrProjectTaskTool record);

    List<SrProjectTaskTool> selectByExample(SrProjectTaskToolExample example);

    SrProjectTaskTool selectByPrimaryKey(String taskToolId);

    int updateByExampleSelective(@Param("record") SrProjectTaskTool record, @Param("example") SrProjectTaskToolExample example);

    int updateByExample(@Param("record") SrProjectTaskTool record, @Param("example") SrProjectTaskToolExample example);

    int updateByPrimaryKeySelective(SrProjectTaskTool record);

    int updateByPrimaryKey(SrProjectTaskTool record);
}