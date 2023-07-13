package com.cnpc.epai.core.workscene.service.impl;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.commom.StatusCode;
import com.cnpc.epai.core.workscene.entity.SceneKeywordRelation;
import com.cnpc.epai.core.workscene.mapper.KeywordRelationMapper;
import com.cnpc.epai.core.workscene.pojo.vo.SceneKeywordRelationVo;
import com.cnpc.epai.core.workscene.service.KeywordRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Name: KeywordRelationServiceImpl
 * @Description:
 * @Version: V1.0.0
 * @Author: 陈淑造
 * @create 2021/11/19 18:10
 */
@Service
public class KeywordRelationServiceImpl implements KeywordRelationService {
    @Autowired
    KeywordRelationMapper keywordRelationMapper;


    @Override
    public Result insertOrUpdate(String relationId, String type, String keywordId, String keywordName, String sortNum, String applicationId, String remarks) {
        Result result = new Result();
        //添加
        if (StringUtils.isEmpty(relationId)) {
            boolean flag = false;
            String[] split = keywordId.split(",");
            String[] split2 = keywordName.split(",");
            String[] sortNumArr = sortNum.split(",");
            for (int i = 0; i < split.length; i++) {
                type = StringUtils.isEmpty(type) ? null : type;
                keywordId = StringUtils.isEmpty(keywordId) ? null : split[i];
                keywordName = StringUtils.isEmpty(keywordName) ? null : split2[i];
                sortNum = StringUtils.isEmpty(sortNum) ? null : sortNumArr[i];
                applicationId = StringUtils.isEmpty(applicationId) ? null : applicationId;

                SceneKeywordRelation sceneKeywordRelation = new SceneKeywordRelation();
                sceneKeywordRelation.setApplicationId(applicationId).setKeywordId(keywordId).setKeywordName(keywordName).setSortNum(Long.valueOf(sortNum)).setType(type).setRemarks(remarks);
                flag = true;
                keywordRelationMapper.insert(sceneKeywordRelation);
            }
            if (flag) {
                return result.setMessage("插入成功").setStatus(StatusCode.NORMAL);
            } else {
                return result.setMessage("数据插入失败").setStatus(StatusCode.ADD_FAILED);
            }
        }//修改
        else if(!StringUtils.isEmpty(relationId)){

              return keywordRelationMapper.updateByRelationId(relationId, type,keywordId,keywordName,sortNum,applicationId, remarks) == 1 ? result.setMessage("修改成功").setStatus(StatusCode.NORMAL):result.setMessage("修改失败").setStatus(StatusCode.UPDATE_FAILED);

        }
        return null;
    }

    @Override
    public  int logicDeleteRelation(String applicationId, String keywordId) {
        List keywordIdList = new ArrayList();
        String[] split = keywordId.split(",");
        for(int i=0;i<split.length;i++){
            keywordIdList.add(split[i]);
        }
        return keywordRelationMapper.batchDeleteRelation(applicationId,keywordIdList);
    }

    @Override
        public List<SceneKeywordRelationVo> queryRelationSortedVo (String type, String applicationId){
            return keywordRelationMapper.selectRelationSortedVo(type, applicationId);
        }

        @Override
        public List<String> queryRelationKeywordSorted (String type, String applicationId){
            List<String> result = new LinkedList<>();
            List<SceneKeywordRelation> ls = keywordRelationMapper.selectRelationSorted(type, applicationId);
            for (SceneKeywordRelation s : ls) {
                result.add(s.getKeywordName());
            }
            return result;
        }
    }
