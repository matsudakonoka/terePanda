package com.cnpc.epai.core.workscene.controller;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.service.CommonTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  @Name: SchemeDesignController
 *  @Description:
 *  @Version: V1.0.0
 *  @Author: 陈淑造
 *  @create 2021/10/19 17:57
 */

@Api(tags = {"方案设计类型"})
@RestController
@RequestMapping("/core/worktask/schemeDesign")
public class SchemeDesignController {
    public static final String SCHEME_DESIGN_TYPE = "2";

    @Autowired
    private CommonTypeService commonTypeService;

    @RequestMapping(value = "/insertOrUpdateSchemeDesign", method = RequestMethod.POST)
    @ApiOperation(value = "增加或更新-SchemeDesign类型", notes = "", code = 200, produces = "application/json")
    public Result insertOrUpdate(@ApiParam(value = "SchemeDesign类型id", type="String")
                                 @RequestParam(required = false) String typeId,
                                 @ApiParam(value = "SchemeDesign类型名称", type="String",required = true)
                                 @RequestParam String typeName,
                                 @ApiParam(value = "排序号", type="String",required = true)
                                 @RequestParam int typeSortNum,
//                                 @ApiParam(value = "原排序号", type="String" )
//                                 @RequestParam(required = false) Integer oldTypeSortNum,
                                 @ApiParam(value = "启用状态", type="String",required = true)
                                 @RequestParam String typeStatus,
                                 @ApiParam(value = "备注", type="String")
                                 @RequestParam(required = false) String remarks){
        return commonTypeService.insertOrUpdate(SCHEME_DESIGN_TYPE,typeId,typeName,typeSortNum,typeStatus,remarks);

    }

    @RequestMapping(value = "/deleteSchemeDesign", method = RequestMethod.POST)
    @ApiOperation(value = "逻辑删除-SchemeDesign类型", notes = "", code = 200)
    public ApiResult delete(@ApiParam(value = "SchemeDesign类型", type="String",required = true)
                            @RequestParam String typeId
//                            @ApiParam(value = "原排序号", type="String",required = true)
//                            @RequestParam int oldTypeSortNum
    ){
        try{
            commonTypeService.logicDeleteType(SCHEME_DESIGN_TYPE,typeId);
            return ApiResult.ofSuccess();
        }catch (Exception e){
            return ApiResult.ofFailure();
        }
    }

    @RequestMapping(value = "/findAllSchemeDesign", method = RequestMethod.GET)
    @ApiOperation(value = "查询所有SchemeDesign类型-SchemeDesign类型", notes = "", code = 200)
    public ApiResult queryAll(@ApiParam(value = "是否指定typeStatus = 1", type="String")
                              @RequestParam(required = false) String typeStatus){
        try{
            List<CommonType> ls = commonTypeService.queryAllSorted(SCHEME_DESIGN_TYPE,typeStatus);
            return ApiResult.ofSuccessResult(ls);
        }catch (Exception e){
            return ApiResult.ofFailureResult(e.toString());
        }
    }
}
