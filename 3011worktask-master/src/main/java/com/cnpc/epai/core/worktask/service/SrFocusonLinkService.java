package com.cnpc.epai.core.worktask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.worktask.domain.SrFocusonLink;

/**
 *
 */
public interface SrFocusonLinkService extends IService<SrFocusonLink> {

    SrFocusonLink saveOrUpdateFocusLink(SrFocusonLink srFocusonLink) throws Exception;


    SrFocusonLink findLinkByResearchType(String researchType,Integer isValid);
}
