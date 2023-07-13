package com.cnpc.epai.core.workscene.service.impl;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.commom.StatusCode;
import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.mapper.CommonTypeMapper;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordTypeVo;
import com.cnpc.epai.core.workscene.service.CommonTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 *  @Name: CommonTypeServiceImpl
 *  @Description:
 *  @Version: V1.2.0
 *  @Author: 陈淑造
 *  @create 2021/10/14 20:25
 */
@Service
public class CommonTypeServiceImpl implements CommonTypeService {

    public static final String LOGIC_INUSE = "1";
    public static final String DELETE = "0";

    @Autowired
    private CommonTypeMapper commonTypeMapper;

    @Override
    public List<CommonType> queryAllSorted(String type,String typeStatus) {
        typeStatus = StringUtils.isEmpty(typeStatus)?null:typeStatus;
        type = StringUtils.isEmpty(type)? "1":type;
        return commonTypeMapper.getCommonType(type,typeStatus);
    }

    /**
     * 关键字类型下拉列表（用于新增）
     * @return
     */
    @Override
    public List<KeywordTypeVo> keywordTypeDownList() {
        List<KeywordTypeVo> list = commonTypeMapper.keywordTypeDownList();
        return list;
    }

    /**
     * 关键字类型下拉列表（用于关键词查询）
     * @return
     */
    @Override
    public List<KeywordTypeVo> keywordTypeQueryDownList() {
        List<KeywordTypeVo> list = commonTypeMapper.keywordTypeQueryDownList();
        return list;
    }

    @Override
    public Page<CommonType> pageQueryAllSorted(int pageNo, int pageSize, String typeNum, String typeStatus,String typeName) {
        Page<CommonType> page = new Page<>(pageNo+1,pageSize);
        return commonTypeMapper.findPageTypes(page,typeNum,typeStatus,typeName);
    }


    /**
    * @Name: insertOrUpdate
    * @Description: 根据id判断更新或添加，有则更新，空则添加
    * @Param: [typeId, typeName, typeSortNum, typeStatus, remarks]
    * @return: com.cnpc.epai.core.workscene.commom.Result
    */
    @Override
    public Result insertOrUpdate(String cType,String typeId,String typeName,int typeSortNum,String typeStatus,String remarks) {
        cType = StringUtils.isEmpty(cType)? "1":cType;
        Result result = new Result();
        CommonType commonType = new CommonType();
        commonType.setTypeName(typeName).setTypeSortNum(typeSortNum).setTypeStatus(typeStatus).setRemarks(remarks);

        //插入
        if (typeId==null){
            //设置为关键词类型
            commonType.setCommonType(cType);
            //>=当前位置则排序号+1
            updateSortNum(cType,1,typeSortNum-1);
            return commonTypeMapper.insert(commonType)==1?result.setStatus(StatusCode.NORMAL).setMessage("保存成功！")
                    :result.setStatus(StatusCode.ADD_FAILED).setMessage("保存失败！");
        }

        //更新
        //>之前位置则排序号-1（删除之前的排序号
        updateSortNum(cType,-1,selectLocationById(typeId));
        //>=当前位置则排序号+1
        updateSortNum(cType,1,typeSortNum-1);
        commonType.setTypeId(typeId);
        return commonTypeMapper.updateById(commonType)==1?result.setStatus(StatusCode.NORMAL).setMessage("更新成功！")
                :result.setStatus(StatusCode.UPDATE_FAILED).setMessage("更新失败！");
    }



    /**
    * @Name: logicDeleteType
    * @Description: 逻辑删除
    * @Param: [typeID]
    * @return: boolean
    */
    @Override
    public ApiResult logicDeleteType(String cType, String typeId) {
        try{
            updateSortNum(cType,-1,selectLocationById(typeId));
            boolean flag = commonTypeMapper.logicDelete(typeId)>0;
            return ApiResult.ofSuccessResult(flag);
            //return true;
        }catch (Exception e){
            return ApiResult.ofFailureResult(e.toString());
        }
    }

    @Override
    public boolean isExistTypeName(String typeName) {
        return commonTypeMapper.isExistName(typeName)>0;
    }

    /**
     * @Name: selectLocationById
     * @Description: 根据id查询排序号
     * @Param: [typeID]
     * @return: java.lang.Integer
     */
    private Integer selectLocationById(String typeId){
        CommonType commonType = commonTypeMapper.selectById(typeId);
        return commonType.getTypeSortNum();
    }


    /**
    * @Name: updateSortNum
    * @Description: 批量更新排序号
    * @Param: [changeNum, location]
    * @return: void
    */
    public void updateSortNum(String commonType,int changeNum,int location) {
        if(changeNum == 0){
            return;
        }
        commonTypeMapper.changeSortNums(commonType,changeNum,location);
    }

}
