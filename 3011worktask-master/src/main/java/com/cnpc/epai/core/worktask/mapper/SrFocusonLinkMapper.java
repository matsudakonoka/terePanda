package com.cnpc.epai.core.worktask.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnpc.epai.core.worktask.domain.SrFocusonLink;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity generator.domain.SrFocusonLink
 */
public interface SrFocusonLinkMapper extends BaseMapper<SrFocusonLink> {
    List<SrFocusonLink> findAllByResearchType(@Param("linkId") String linkId,@Param("researchType") String researchType);
}




