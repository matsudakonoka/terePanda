package com.cnpc.epai.core.workscene.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.entity.Keyword;

import java.util.List;

public interface KeywordService extends IService<Keyword> {

    //统计使用次数
    Result updateUsaCount1(List<Keyword> keywordList);
    //展示列表
    Result getKwList();

    int totals();

    List<Keyword> findAll();

    List<Keyword> findList(String keywordName);

    Result addAndUpdateKeyWord(String keywordId,String keywordName,String keywordType,String remarks);

    boolean isExit(String keywordName);

    boolean deleteById(String keywordId);

}
