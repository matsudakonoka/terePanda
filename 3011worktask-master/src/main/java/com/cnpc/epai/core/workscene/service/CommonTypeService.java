package com.cnpc.epai.core.workscene.service;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordTypeVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  @Name: CommonTypeService
 *  @Description:
 *  @Version: V1.0.0
 *  @Author: 陈淑造
 *  @create 2021/10/14 20:16
 */
@Service
public interface CommonTypeService {

    //    查询所有关键词类型（排序）
    List<CommonType> queryAllSorted(String cType,String typeStatus);

    // 添加huo更新
    Result insertOrUpdate(String cType,String typeId,String typeName,int typeSortNum,String typeStatus,String remarks);

    // 逻辑删除
    ApiResult logicDeleteType(String cType, String typeId);

    boolean isExistTypeName(String typeName);

    List<KeywordTypeVo> keywordTypeDownList();

    List<KeywordTypeVo> keywordTypeQueryDownList();

    Page<CommonType> pageQueryAllSorted(int pageNo, int pageSize, String typeNum, String typeStatus,String typeName);
}
