package com.cnpc.epai.core.workscene.service;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.pojo.vo.SceneKeywordRelationVo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 *  @Name: KeywordRelationService
 *  @Description:
 *  @Version: V1.0.0
 *  @Author: 陈淑造
 *  @create 2021/11/19 17:48
 */
@Service
public interface KeywordRelationService {

   Result insertOrUpdate(String relationId,String type, String keywordId, String keywordName, String sortNum, String applicationId, String remarks);

    int logicDeleteRelation(String applicationId, String keywordId);
    List<SceneKeywordRelationVo> queryRelationSortedVo(String type, String applicationId);

    List<String> queryRelationKeywordSorted(String type, String applicationId);
}
