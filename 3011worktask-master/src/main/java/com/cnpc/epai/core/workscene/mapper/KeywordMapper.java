package com.cnpc.epai.core.workscene.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnpc.epai.core.workscene.entity.Keyword;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper
public interface KeywordMapper extends BaseMapper<Keyword> {

    //统计使用次数
    int updateUsaCount1(List<Keyword> keywordList);
    //展示列表
    List<Keyword> getKwList();

    //添加
    int addKeyword(String keywordName,String keywordType,String remarks);
    //获取关键字列表
    List<Keyword> findAll();
    //模糊查询
    List<Keyword> findList(String keywordName);

    //判断是否含有关键字
    int isExit(String keywordName);

    boolean deleteById(String keyWordId);

}
