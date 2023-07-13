package com.cnpc.epai.core.workscene.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.commom.StatusCode;
import com.cnpc.epai.core.workscene.entity.KeywordEntity;
import com.cnpc.epai.core.workscene.mapper.KeywordEntityMapper;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordMaintenanceVo;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordVo;
import com.cnpc.epai.core.workscene.service.KeywordEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liuTao
 * @version 1.0
 * @name KeywordEntityServiceImpl
 * @description
 * @date 2021/10/14 10:38
 */
@Service
public class KeywordEntityServiceImpl implements KeywordEntityService {
    @Autowired
    KeywordEntityMapper keywordMapper;

    /**
     * 添加或修改关键字信息
     * @param keywordId
     * @param keywordName
     * @param typeId
     * @param keywordStatus
     * @param keywordSortNum
     * @param remarks
     * @return
     */
    @Override
    public Result addOrUpdateKeyWordNew(String keywordId, String keywordName, String typeId, String keywordStatus, Integer keywordSortNum, String remarks) {
        Result result = new Result();
        KeywordEntity keyword = new KeywordEntity();
        keyword.setKeywordName(keywordName).setTypeId(typeId).setKeywordStatus(keywordStatus).setKeywordSortNum(keywordSortNum).setRemarks(remarks);
        if (keywordId == null) {
            keywordMapper.updateAddKeywordSortNum(keywordSortNum, typeId);
            return keywordMapper.insert(keyword) == 1 ? result.setStatus(StatusCode.NORMAL).setMessage("保存成功！")
                    : result.setStatus(StatusCode.ADD_FAILED).setMessage("保存失败！");
        }
        keyword.setKeywordId(keywordId);
        KeywordEntity keywordEntity = keywordMapper.selectById(keywordId);
        if(keywordEntity.getKeywordSortNum()!=keywordSortNum) {
            keywordMapper.updateKeywordSortNum(keywordSortNum, keywordId);
        }
        return keywordMapper.updateById(keyword) == 1 ? result.setStatus(StatusCode.NORMAL).setMessage("更新成功！")
                : result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！");
    }

    /**
     * 逻辑删除关键字信息
     * @param keywordId
     * @return
     */
    @Override
    public boolean deleteById(String keywordId) {
        keywordMapper.delAndUpdateSort(keywordId);
        return keywordMapper.updateBsflagByKeywordId(keywordId)>0;
    }

    /**
     * 关键字唯一校验
     * @param keywordName
     * @return
     */
    @Override
    public boolean isExit(String keywordName) {
        return keywordMapper.isExit(keywordName)>0;
    }

    /**
     * 排序号唯一性校验
     * @param keywordSortNum
     * @return
     */
    @Override
    public boolean isExitSort(Integer keywordSortNum) {
        return keywordMapper.isExitSort(keywordSortNum)>0;
    }


    @Override
    public List<KeywordVo> findAll() {
        return keywordMapper.findAll();
    }

    @Override
    public List<KeywordVo> findList(String keywordName) {
        return keywordMapper.findList(keywordName);
    }

    @Override
    public Result updateUsaCount1(List<KeywordEntity> keywordList) {
        Result result = new Result();
        int count = keywordMapper.updateUsaCount1(keywordList);

        return count != keywordList.size()?
                result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！") :
                result.setStatus(StatusCode.NORMAL).setMessage("更新成功！");
    }

    /**
     * 分页查询数据(全部)
     * @param pageNO
     * @param size
     * @return
     */
    @Override
    public Result findPageListAll(int pageNO, int size) {
        Result result = new Result();
        Page<KeywordMaintenanceVo> page = new Page<>(pageNO+1,size);
        return result.setBody(keywordMapper.findPageListAll(page));
    }

    /**
     * 根据类型id分页查询
     * @param pageNO
     * @param size
     * @param typeId
     * @return
     */
    @Override
    public Result findPageList(int pageNO, int size, String typeId) {
        Result result = new Result();
        Page<KeywordMaintenanceVo> page = new Page<>(pageNO+1,size);
        return result.setBody(keywordMapper.findPageList(page,typeId));
    }

    //todo
    public Result addAndUpdateKeyWord(String keywordId,String keywordName,String keywordType,String remarks) {
        Result result = new Result();
        KeywordEntity keyword = new KeywordEntity();
        keyword.setKeywordName(keywordName).setTypeId(keywordType).setRemarks(remarks);
        if (keywordId==null){
            return keywordMapper.insert(keyword)==1?result.setStatus(StatusCode.NORMAL).setMessage("保存成功！")
                    :result.setStatus(StatusCode.ADD_FAILED).setMessage("保存失败！");
        }
        keyword.setKeywordId(keywordId);
        return keywordMapper.updateById(keyword)==1?result.setStatus(StatusCode.NORMAL).setMessage("更新成功！")
                :result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！");

    }
}
