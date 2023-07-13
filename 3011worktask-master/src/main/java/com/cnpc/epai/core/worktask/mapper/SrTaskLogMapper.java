package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrTaskLog;
import com.cnpc.epai.core.worktask.pojo.SrTaskLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrTaskLogMapper {
    int deleteByExample(SrTaskLogExample example);

    int deleteByPrimaryKey(String taskLogId);

    int insert(SrTaskLog record);

    int insertSelective(SrTaskLog record);

    List<SrTaskLog> selectByExample(SrTaskLogExample example);

    SrTaskLog selectByPrimaryKey(String taskLogId);

    int updateByExampleSelective(@Param("record") SrTaskLog record, @Param("example") SrTaskLogExample example);

    int updateByExample(@Param("record") SrTaskLog record, @Param("example") SrTaskLogExample example);

    int updateByPrimaryKeySelective(SrTaskLog record);

    int updateByPrimaryKey(SrTaskLog record);
}