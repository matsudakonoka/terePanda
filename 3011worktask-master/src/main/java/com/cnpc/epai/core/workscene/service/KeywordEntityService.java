package com.cnpc.epai.core.workscene.service;

import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.entity.KeywordEntity;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordVo;

import java.util.List;

/**
 * @author liuTao
 * @version 1.0
 * @name KeywordEntityService
 * @description
 * @date 2021/10/14 10:20
 */
public interface KeywordEntityService {
    Result addOrUpdateKeyWordNew(String keywordId, String keywordName, String typeId, String keywordStatus,Integer keywordSortNum,String remarks);

    //boolean deleteById(String keywordId,Integer keywordSortNum);

    boolean deleteById(String keywordId);

    boolean isExit(String keywordName);

    boolean isExitSort(Integer keywordSortNum);

    List<KeywordVo> findAll();

    List<KeywordVo> findList(String keywordName);

    Result updateUsaCount1(List<KeywordEntity> keywordList);

    Result findPageListAll(int pageNO,int size);

    Result findPageList(int pageNO,int size,String typeId);

}
