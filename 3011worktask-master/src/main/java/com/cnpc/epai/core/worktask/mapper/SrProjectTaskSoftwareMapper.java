package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrProjectTaskSoftware;
import com.cnpc.epai.core.worktask.pojo.SrProjectTaskSoftwareExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrProjectTaskSoftwareMapper {
    int deleteByExample(SrProjectTaskSoftwareExample example);

    int deleteByPrimaryKey(String taskSoftwareId);

    int insert(SrProjectTaskSoftware record);

    int insertSelective(SrProjectTaskSoftware record);

    List<SrProjectTaskSoftware> selectByExample(SrProjectTaskSoftwareExample example);

    SrProjectTaskSoftware selectByPrimaryKey(String taskSoftwareId);

    int updateByExampleSelective(@Param("record") SrProjectTaskSoftware record, @Param("example") SrProjectTaskSoftwareExample example);

    int updateByExample(@Param("record") SrProjectTaskSoftware record, @Param("example") SrProjectTaskSoftwareExample example);

    int updateByPrimaryKeySelective(SrProjectTaskSoftware record);

    int updateByPrimaryKey(SrProjectTaskSoftware record);
}