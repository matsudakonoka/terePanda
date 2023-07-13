package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrTaskAssign;
import com.cnpc.epai.core.worktask.pojo.SrTaskAssignExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrTaskAssignMapper {
    int deleteByExample(SrTaskAssignExample example);

    int deleteByPrimaryKey(String taskAssignId);

    int insert(SrTaskAssign record);

    int insertSelective(SrTaskAssign record);

    List<SrTaskAssign> selectByExample(SrTaskAssignExample example);

    SrTaskAssign selectByPrimaryKey(String taskAssignId);

    int updateByExampleSelective(@Param("record") SrTaskAssign record, @Param("example") SrTaskAssignExample example);

    int updateByExample(@Param("record") SrTaskAssign record, @Param("example") SrTaskAssignExample example);

    int updateByPrimaryKeySelective(SrTaskAssign record);

    int updateByPrimaryKey(SrTaskAssign record);
}