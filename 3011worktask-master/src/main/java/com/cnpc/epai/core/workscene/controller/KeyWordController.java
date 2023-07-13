package com.cnpc.epai.core.workscene.controller;

import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.commom.Result;
import com.cnpc.epai.core.workscene.commom.StatusCode;

import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.entity.Keyword;
import com.cnpc.epai.core.workscene.service.CommonTypeService;
import com.cnpc.epai.core.workscene.service.KeywordService;
import com.cnpc.epai.core.workscene.service.impl.CommonTypeServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"关键词分组"})
@RestController
@RequestMapping("/core/worktask/keywordMaintain")
public class KeyWordController {
    @Autowired
    private KeywordService keywordService;
    //todo
    @RequestMapping(value = "/updateusacount", method = RequestMethod.POST)
    @ApiOperation(value = "更新使用次数", notes = "", code = 200, produces = "application/json")
    public Result updateUsaCount(@ApiParam(value = "关键词列表", type="body")
                                             @RequestBody List<Keyword> keywordList){
        System.out.println("list:"+keywordList);
        return keywordService.updateUsaCount1(keywordList);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ApiOperation(value = "获取关键词列表", notes = "无需添加参数", code = 200, produces = "application/json")
    public ApiResult findAll(@ApiParam(name = "keywordName", value = "关键词名称", required = false)
                             @RequestParam(value="keywordName", required = false) String keywordName) {

        if(keywordName == null){
            return ApiResult.ofSuccessResult(keywordService.findAll());
        }else {
            return ApiResult.ofSuccessResult(keywordService.findList(keywordName));
        }
    }


    //todo
    @RequestMapping(value = "/addAndUpdateKeyWord", method = RequestMethod.POST)
    @ApiOperation(value = "添加关键词", notes = "", code = 200, produces = "application/json")
    public Result saveKeyword(
            @ApiParam(value = "关键字id") @RequestParam(required = false) String keywordId,
            @ApiParam(value = "关键字名称")@RequestParam String keywordName,
            @ApiParam(value = "关键字类型") @RequestParam String keywordType,
            @ApiParam(value = "备注") @RequestParam String remarks) {
        return keywordService.addAndUpdateKeyWord(keywordId,keywordName,keywordType,remarks);
    }

    //todo
    @RequestMapping(value = "/deletekeyword", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除关键词",notes = "" ,code = 200, produces = "application/json")
    public boolean deleteKeyword(String keywordId) {
        return keywordService.removeById(keywordId);
    }
}

