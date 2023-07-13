package com.cnpc.epai.core.worktask.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.worktask.domain.SrFocusonLink;
import com.cnpc.epai.core.worktask.mapper.SrFocusonLinkMapper;
import com.cnpc.epai.core.worktask.service.SrFocusonLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 *
 */
@Service
public class SrFocusonLinkServiceImpl extends ServiceImpl<SrFocusonLinkMapper, SrFocusonLink>
    implements SrFocusonLinkService {
    @Autowired
    SrFocusonLinkMapper srFocusonLinkMapper;

    @Override
    public SrFocusonLink saveOrUpdateFocusLink(SrFocusonLink srFocusonLink) throws Exception {
        List<SrFocusonLink> allByResearchType = srFocusonLinkMapper.findAllByResearchType(srFocusonLink.getLinkId(),srFocusonLink.getResearchType());
        if(!CollectionUtils.isEmpty(allByResearchType)){
            throw new Exception("该研究类型已存在");
        }
        srFocusonLink.setBsflag(1);
        saveOrUpdate(srFocusonLink);
        return srFocusonLink;
    }

    @Override
    public SrFocusonLink findLinkByResearchType(String researchType,Integer isValid) {
        List<SrFocusonLink> allByResearchType = srFocusonLinkMapper.findAllByResearchType(null,researchType);
        if(CollectionUtils.isEmpty(allByResearchType)){
            return null;
        }else {
            return allByResearchType.get(0);
        }
    }
}




