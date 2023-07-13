package com.cnpc.epai.core.workscene.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.commom.StatusCode;
import com.cnpc.epai.core.workscene.entity.Keyword;
import com.cnpc.epai.core.workscene.mapper.KeywordMapper;
import com.cnpc.epai.core.workscene.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, Keyword> implements KeywordService {
    @Autowired
    private KeywordMapper keywordMapper;

    @Override
    public Result updateUsaCount1(List<Keyword> keywordList) {
        Result result = new Result();
        int count = keywordMapper.updateUsaCount1(keywordList);

        return count != keywordList.size()?
                result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！") :
                result.setStatus(StatusCode.NORMAL).setMessage("更新成功！");

    }

    @Override
    public Result getKwList() {
        Result<List> result = new Result<>();
        //这里本来是从数据库中查询数据的，但是现在没有数据库,从本地模拟的数据中取值
        List<Keyword> kwList = keywordMapper.getKwList();
        if (kwList != null || kwList.size() !=0){
            return result.setStatus(StatusCode.NORMAL).setMessage("查询成功！").setBody(kwList);
        }
        return result.setStatus(StatusCode.QUERY_FAILED).setMessage("查询失败！");
    }

    public int totals(){
        return 10;
    }



    @Override
    public List<Keyword> findAll() {
        return keywordMapper.findAll();
    }

    @Override
    public List<Keyword> findList(String keywordName) {

        return keywordMapper.findList(keywordName);
    }

    @Override
    public Result addAndUpdateKeyWord(String keywordId,String keywordName,String keywordType,String remarks) {
        Result result = new Result();
        Keyword keyword = new Keyword();
        keyword.setKeywordName(keywordName).setKeywordType(keywordType).setRemarks(remarks);
        if (keywordId==null){
            return keywordMapper.insert(keyword)==1?result.setStatus(StatusCode.NORMAL).setMessage("保存成功！")
                :result.setStatus(StatusCode.ADD_FAILED).setMessage("保存失败！");
        }
        keyword.setKeywordId(keywordId);
        return keywordMapper.updateById(keyword)==1?result.setStatus(StatusCode.NORMAL).setMessage("更新成功！")
                :result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！");

    }

    @Override
    public boolean isExit(String keywordName) {
        return keywordMapper.isExit(keywordName)>0;
    }

    @Override
    public boolean deleteById(String keywordId) {
        return keywordMapper.deleteById(keywordId);
    }
}
