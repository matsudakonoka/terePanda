package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrProjectTaskDataset;
import com.cnpc.epai.core.worktask.pojo.SrProjectTaskDatasetExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SrProjectTaskDatasetMapper {
    int deleteByExample(SrProjectTaskDatasetExample example);

    int deleteByPrimaryKey(String taskDatasetId);

    int insert(SrProjectTaskDataset record);

    int insertSelective(SrProjectTaskDataset record);

    List<SrProjectTaskDataset> selectByExample(SrProjectTaskDatasetExample example);

    SrProjectTaskDataset selectByPrimaryKey(String taskDatasetId);

    int updateByExampleSelective(@Param("record") SrProjectTaskDataset record, @Param("example") SrProjectTaskDatasetExample example);

    int updateByExample(@Param("record") SrProjectTaskDataset record, @Param("example") SrProjectTaskDatasetExample example);

    int updateByPrimaryKeySelective(SrProjectTaskDataset record);

    int updateByPrimaryKey(SrProjectTaskDataset record);
}