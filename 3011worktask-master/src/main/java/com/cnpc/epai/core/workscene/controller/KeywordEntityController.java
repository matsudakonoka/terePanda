package com.cnpc.epai.core.workscene.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.entity.KeywordEntity;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordTypeVo;
import com.cnpc.epai.core.workscene.pojo.vo.SceneKeywordRelationVo;
import com.cnpc.epai.core.workscene.service.CommonTypeService;
import com.cnpc.epai.core.workscene.service.KeywordEntityService;
import com.cnpc.epai.core.workscene.service.KeywordRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author liuTao
 * @version 1.2
 * @name KeywordEntityController
 * @description
 * @date 2021/10/14 10:41
 */
@Api(tags = {"关键词维护"})
@RestController
@RequestMapping("/core/worktask/keyword")
public class KeywordEntityController {


    @Autowired
    KeywordEntityService keywordService;
    @Autowired
    private CommonTypeService commonTypeService;
    @Autowired
    private KeywordRelationService sceneKeywordRelationService;

    @RequestMapping(value = "/insertKeywordRelation", method = RequestMethod.POST)
    @ApiOperation(value = "增加或修改关键词关联信息", notes = "",  produces = "application/json")
    public Result insertRelation(
                                 @ApiParam(value = "关联id", type = "String", required = false)
                                 @RequestParam(required = false) String relationId,
                                 @ApiParam(value = "类型", type = "String", required = true)
                                 @RequestParam String type,
                                 @ApiParam(value = "关键词id", type = "String", required = true)
                                 @RequestParam String keywordId,
                                 @ApiParam(value = "关键词名称", type = "String", required = true)
                                 @RequestParam String keywordName,
                                 @ApiParam(value = "排序号", type = "String", required = true)
                                 @RequestParam String sortNum,
                                 @ApiParam(value = "应用id", type = "String", required = true)
                                 @RequestParam String applicationId,
                                 @ApiParam(value = "备注", type = "String")
                                 @RequestParam(required = false) String remarks) {
        return sceneKeywordRelationService.insertOrUpdate(relationId,type, keywordId,keywordName, sortNum, applicationId, remarks);

    }

    @RequestMapping(value = "/findRelation", method = RequestMethod.GET)
    @ApiOperation(value = "查询关键词关联信息", notes = "", code = 200)
    public Result queryRelationList(@ApiParam(value = "类型", type = "String",required = true)
                                        @RequestParam String type,
                                    @ApiParam(value = "应用id", type = "String",required = true)
                                    @RequestParam String applicationId
                                   ) {
        try {
            List<SceneKeywordRelationVo> ls = sceneKeywordRelationService.queryRelationSortedVo(type,applicationId);
            return new Result().successResult(ls);
        } catch (Exception e) {
            return new Result().failureResult(e.toString());
        }
    }

    @RequestMapping(value = "/findRelationKeyword", method = RequestMethod.GET)
    @ApiOperation(value = "查询关联信息中的关键词列表", notes = "", code = 200)
    public Result queryRelationKeyword(@ApiParam(value = "类型", type = "String",required = true)
                                           @RequestParam String type,
                                       @ApiParam(value = "应用id", type = "String",required = true)
                                           @RequestParam String applicationId
                                     ) {
        try {
            List<String> ls = sceneKeywordRelationService.queryRelationKeywordSorted(type,applicationId);
            return new Result().successResult(ls);
        } catch (Exception e) {
            return new Result().failureResult(e.toString());
        }
    }

    @RequestMapping(value = "/deleteRelation",method = RequestMethod.POST)
    @ApiOperation(value = "删除关联信息中的关键词",notes = "", code = 200)
    public Result deleteRelation(@ApiParam(value = "应用id",type = "String",required = true)
                                 @RequestParam(required = true)String applicationId,
                                 @ApiParam(value = "关键字id",type = "String",required = true)
                                 @RequestParam(required = true)String keywordId
    ){
        try {
            if(sceneKeywordRelationService.logicDeleteRelation(applicationId,keywordId)>0) {
                return new Result().successResult("删除成功");
            }else{
                return new Result().failureResult("删除失败");
            }
        } catch (Exception e) {

            return new Result().setMessage(e.toString()).failureResult("删除失败");
        }

    }


//    ----------------以上为关键词关联信息表-----------------------
//    ---------------------------------------------------------
//    ----------------以下是通用类型表----------------------------


    @RequestMapping(value = "/insertOrUpdateKeywordType", method = RequestMethod.POST)
    @ApiOperation(value = "增加或更新-Keyword类型", notes = "通用类型号{1:关键词类型（默认），2:方案设计类型，3:储量报告类型}；\n启用状态{0:禁用，1:启用}",  produces = "application/json")
    public Result insertOrUpdate(@ApiParam(value = "关键词类型id", type = "String")
                                 @RequestParam(required = false) String typeId,
                                 @ApiParam(value = "通用类型号，不填默认为1", type = "String")
                                 @RequestParam(required = false) String typeNum,
                                 @ApiParam(value = "类型名称", type = "String", required = true)
                                 @RequestParam String typeName,
                                 @ApiParam(value = "排序号", type = "String", required = true)
                                 @RequestParam int typeSortNum,
                                 @ApiParam(value = "启用状态", type = "String", required = true)
                                 @RequestParam String typeStatus,
                                 @ApiParam(value = "备注", type = "String")
                                 @RequestParam(required = false) String remarks) {
        return commonTypeService.insertOrUpdate(typeNum, typeId, typeName, typeSortNum, typeStatus, remarks);

    }

    @RequestMapping(value = "/deleteKeywordType", method = RequestMethod.POST)
    @ApiOperation(value = "逻辑删除-Keyword类型", notes = "通用类型号{1:关键词类型（默认），2:方案设计类型，3:储量报告类型}", code = 200)
    public Result delete(@ApiParam(value = "关键词类型", type = "String", required = true)
                         @RequestParam String typeId,
                         @ApiParam(value = "通用类型号", type = "String")
                         @RequestParam(required = false) String typeNum
//                            @ApiParam(value = "原排序号", type="String",required = true)
//                            @RequestParam int oldTypeSortNum
    ) {
        try {
            commonTypeService.logicDeleteType(typeNum, typeId);
            return new Result().successResult(null);
        } catch (Exception e) {
            return new Result().failureResult(null);
        }
    }

    @RequestMapping(value = "/findAllKeywordTypes", method = RequestMethod.GET)
    @ApiOperation(value = "查询所有-Keyword类型", notes = "通用类型号{1:关键词类型（默认），2:方案设计类型，3:储量报告类型}；\n启用状态{0:禁用，1:启用}", code = 200)
    public Result queryAll(@ApiParam(value = "通用类型号", type = "String")
                           @RequestParam(required = false) String typeNum,
                           @ApiParam(value = "启用状态", type = "String")
                           @RequestParam(required = false) String typeStatus) {
        try {
            List<CommonType> ls = commonTypeService.queryAllSorted(typeNum, typeStatus);
            return new Result().successResult(ls);
        } catch (Exception e) {
            return new Result().failureResult(e.toString());
        }
    }

    @RequestMapping(value = "/pageFindTypes", method = RequestMethod.GET)
    @ApiOperation(value = "分页查询-Keyword类型", notes = "通用类型号{1:关键词类型，2:方案设计类型，3:储量报告类型}；\n启用状态{0:禁用，1:启用}", code = 200)
    public Result pageQueryAll(@ApiParam(value = "通用类型号", type = "String", required = true)
                                    @RequestParam String typeNum,
                           @ApiParam(value = "类型名称",type = "String",required = false)
                                    @RequestParam(value = "typeName",required = false)String typeName,
                           @ApiParam(value = "启用状态", type = "String")
                                    @RequestParam(required = false) String typeStatus,
                           @ApiParam(name = "pageNo", value = "页码", required = true)
                                   @RequestParam(value = "pageNo", required = true) int pageNo,
                           @ApiParam(name = "pageSize", value = "页大小", required = true)
                                   @RequestParam(value = "pageSize", required = true) int pageSize)

                           {
        try {
            Page<CommonType> ls = commonTypeService.pageQueryAllSorted(pageNo,pageSize,typeNum, typeStatus,typeName);
            return new Result().successResult(ls);
        } catch (Exception e) {
            return new Result().failureResult(e.toString());
        }
    }

    @RequestMapping(value = "/getKeywordTypeDownList", method = RequestMethod.GET)
    @ApiOperation(value = "关键词类型下拉列表", notes = "", code = 200)
    public Result getKeywordTypeDownList() {
        try {
            List<KeywordTypeVo> list = commonTypeService.keywordTypeDownList();
            return new Result().successResult(list);
        } catch (Exception e) {
            return new Result().failureResult(null);
        }
    }

    @RequestMapping(value = "/getKeywordTypeQueryDownList", method = RequestMethod.GET)
    @ApiOperation(value = "关键词类型下拉列表-(查询修改)", notes = "", code = 200)
    public Result getKeywordTypeQueryDownList() {
        try {
            List<KeywordTypeVo> list = commonTypeService.keywordTypeQueryDownList();
            return new Result().successResult(list);
        } catch (Exception e) {
            return new Result().failureResult(null);
        }
    }

    @RequestMapping(value = "/uniquenessKeywordTypeName", method = RequestMethod.GET)
    @ApiOperation(value = "关键词类型名称唯一性查询", notes = "", code = 200, produces = "application/json")
    public Result isExistName(@ApiParam(value = "关键字类型名称", required = true) @RequestParam String typeName) {
        /*if (commonTypeService.isExistTypeName(typeName)) {
            return new Result().successResult(true);
        } else {
            return new Result().failureResult(false);
        }*/
        Result result = new Result();
        result.setStatus("200");
        if (!commonTypeService.isExistTypeName(typeName)) {
            result.setMessage("关键词未重复");
            return result;
        }
        result.setStatus("400");
        result.setMessage("关键词重复");
        return result;
    }

//    ----------------------以上关键词类型-------------------------
//    分割线-----------------------------------------------------
//    -----------------------以下关键词---------------------------


    @RequestMapping(value = "/addOrUpdateKeyWord", method = RequestMethod.POST)
    @ApiOperation(value = "添加关键词", notes = "", code = 200, produces = "application/json")
    public Result saveKeyword(
            @ApiParam(value = "关键字id") @RequestParam(required = false) String keywordId,
            @ApiParam(value = "关键字名称", required = true) @RequestParam String keywordName,
            @ApiParam(value = "关键字类型id", required = true) @RequestParam String typeId,
            @ApiParam(value = "关键字状态,1表示启用，0表示停用", required = true) @RequestParam String keywordStatus,
            @ApiParam(value = "关键字排序号", required = true) @RequestParam Integer keywordSortNum,
            @ApiParam(value = "备注") @RequestParam(required = false) String remarks) {
        Result result = new Result();
        if (StringUtils.isEmpty(keywordName)) {
            result.setStatus("400");
            result.setMessage("关键词不能是空格");
            return result;
        }
        if (StringUtils.isEmpty(keywordSortNum)) {
            result.setStatus("400");
            result.setMessage("关键词排序号不能是空格");
            return result;
        }
        return keywordService.addOrUpdateKeyWordNew(keywordId, keywordName, typeId, keywordStatus, keywordSortNum, remarks);
    }

    @RequestMapping(value = "/deleteKeyword", method = RequestMethod.GET)
    @ApiOperation(value = "删除关键词", notes = "", code = 200, produces = "application/json")
    public Result deleteKeyword(@ApiParam(value = "关键字id", required = true) @RequestParam String keywordId
            /*@ApiParam(value = "关键字排序号",required = true)@RequestParam Integer keywordSortNum*/) {
        //return keywordService.deleteById(keywordId,keywordSortNum);
        Result result = new Result();
        if (!keywordService.deleteById(keywordId)) {
            return result.failureResult();
        }
        return result.successResult();
    }

    @RequestMapping(value = "/uniquenessKeywordName", method = RequestMethod.GET)
    @ApiOperation(value = "关键词唯一性查询", notes = "", code = 200, produces = "application/json")
    public Result isExit(@ApiParam(value = "关键字名称", required = true) @RequestParam String keywordName) {
        Result result = new Result();
        if (!keywordService.isExit(keywordName)) {
            result.setStatus("200");
            result.setMessage("关键词未重复");
            return result;
        }
        result.setStatus("400");
        result.setMessage("关键词重复");
        return result;
    }

    @RequestMapping(value = "/uniquenessKeywordSortNum", method = RequestMethod.GET)
    @ApiOperation(value = "关键词排序号唯一性查询", notes = "", code = 200, produces = "application/json")
    public Result isExitSort(@ApiParam(value = "关键字排序号", required = true) @RequestParam Integer keywordSortNum) {
        Result result = new Result();
        result.setStatus("200");
        if (!keywordService.isExitSort(keywordSortNum)) {
            result.setMessage("关键词序号未重复");
            return result;
        }
        result.setStatus("400");
        result.setMessage("关键词序号重复");
        return result;
    }

    @RequestMapping(value = "/keywordList", method = RequestMethod.GET)
    @ApiOperation(value = "获取关键词列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public Result findAll(@ApiParam(name = "keywordName", value = "关键词名称", required = false)
                          @RequestParam(value = "keywordName", required = false) String keywordName) {
        if (keywordName == null) {
            return new Result().successResult(keywordService.findAll());
        } else
            return new Result().successResult(keywordService.findList(keywordName));
    }

    @RequestMapping(value = "/updateUsacount", method = RequestMethod.POST)
    @ApiOperation(value = "更新使用次数", notes = "", code = 200, produces = "application/json")
    public Result updateUsaCount(@ApiParam(value = "关键词列表", type = "body")
                                 @RequestBody List<KeywordEntity> keywordList) {
        System.out.println("list:" + keywordList);
        return keywordService.updateUsaCount1(keywordList);
    }

    @RequestMapping(value = "/findKeywordPageListAll", method = RequestMethod.GET)
    @ApiOperation(value = "获取关键词分页列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public Result findPageListAll(@ApiParam(name = "typeId", value = "关键词类型id", required = false)
                                  @RequestParam(value = "typeId", required = false) String typeId,
                                  @ApiParam(name = "pageNo", value = "页码", required = true) @RequestParam(value = "pageNo", required = true) int pageNo,
                                  @ApiParam(name = "pageSize", value = "页大小", required = true) @RequestParam(value = "pageSize", required = true) int pageSize) {

        if (typeId == null) {
            return new Result().successResult(keywordService.findPageListAll(pageNo, pageSize).getBody());
        } else
            return new Result().successResult(keywordService.findPageList(pageNo, pageSize, typeId).getBody());
    }
}
