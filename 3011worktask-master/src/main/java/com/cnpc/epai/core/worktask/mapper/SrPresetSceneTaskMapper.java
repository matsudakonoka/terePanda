package com.cnpc.epai.core.worktask.mapper;

import com.cnpc.epai.core.worktask.pojo.SrPresetSceneTask;
import com.cnpc.epai.core.worktask.pojo.SrPresetSceneTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SrPresetSceneTaskMapper {
    int deleteByExample(SrPresetSceneTaskExample example);

    int deleteByPrimaryKey(String presetSceneTaskId);

    int insert(SrPresetSceneTask record);

    int insertSelective(SrPresetSceneTask record);

    List<SrPresetSceneTask> selectByExample(SrPresetSceneTaskExample example);

    SrPresetSceneTask selectByPrimaryKey(String presetSceneTaskId);

    int updateByExampleSelective(@Param("record") SrPresetSceneTask record, @Param("example") SrPresetSceneTaskExample example);

    int updateByExample(@Param("record") SrPresetSceneTask record, @Param("example") SrPresetSceneTaskExample example);

    int updateByPrimaryKeySelective(SrPresetSceneTask record);

    int updateByPrimaryKey(SrPresetSceneTask record);
}