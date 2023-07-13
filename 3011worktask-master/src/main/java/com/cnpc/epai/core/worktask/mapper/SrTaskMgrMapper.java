package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrTaskMgr;
import com.cnpc.epai.core.worktask.pojo.SrTaskMgrExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrTaskMgrMapper {
    int deleteByExample(SrTaskMgrExample example);

    int deleteByPrimaryKey(String taskId);

    int insert(SrTaskMgr record);

    int insertSelective(SrTaskMgr record);

    List<SrTaskMgr> selectByExample(SrTaskMgrExample example);

    SrTaskMgr selectByPrimaryKey(String taskId);

    int updateByExampleSelective(@Param("record") SrTaskMgr record, @Param("example") SrTaskMgrExample example);

    int updateByExample(@Param("record") SrTaskMgr record, @Param("example") SrTaskMgrExample example);

    int updateByPrimaryKeySelective(SrTaskMgr record);

    int updateByPrimaryKey(SrTaskMgr record);
}