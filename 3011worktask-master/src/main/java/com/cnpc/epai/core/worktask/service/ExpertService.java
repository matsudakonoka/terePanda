package com.cnpc.epai.core.worktask.service;

import com.cnpc.epai.core.worktask.domain.ExpertContent;
import com.cnpc.epai.core.worktask.domain.Profession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public interface ExpertService {
    boolean saveExpertContent(ExpertContent expertContent);

    Page<ExpertContent> getExpertContent(String keyWord, Pageable pageable);

    Profession saveProfession(Profession profession);

    Map<String,Object> getProfession(String unit, String keyWord,String rule, Pageable pageable);

    void delExpertContent(String id);

    void delProfession(String id);

    List<Map<String,Object>> getExpertProfessionList();
}
