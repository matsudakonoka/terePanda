package com.cnpc.epai.core.worktask.controller;

import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.worktask.domain.ExpertContent;
import com.cnpc.epai.core.worktask.domain.Profession;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.service.ExpertService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.cnpc.epai.core.worktask.util.EnumUtil;
import com.cnpc.epai.core.worktask.util.EnumUtil.UnitTypes;

import org.springframework.data.domain.Pageable;

import java.util.*;

@RestController
@RequestMapping("/core/worktask/expert")
public class ExpertController {

    @Autowired
    private ExpertService expertService;

    @RequestMapping(value = "/saveExpertContent", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存专家信息", notes = "", code =200,produces="application/json")
    public boolean saveExpertContent(ExpertContent expertContent) {
        boolean x = false;
        try {
            return expertService.saveExpertContent(expertContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/getExpertContent", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询专家信息列表", notes = "", code =200,produces="application/json")
    public Page<ExpertContent> getExpertContent(
            @ApiParam(name = "keyWord", value = "模糊查询条件", required = false) @RequestParam(name = "keyWord", defaultValue="") String keyWord,
            Pageable pageable) {
        boolean x = false;


        try {
            return expertService.getExpertContent(keyWord,pageable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/delExpertContent", method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "删除专家信息", notes = "", code =200,produces="application/json")
    public boolean delExpertContent(
            @ApiParam(name = "id", value = "专家信息id", required = false)@RequestParam(name = "id", defaultValue="") String id) {
        boolean x = false;
        try {
            expertService.delExpertContent(id);
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/saveProfession", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "保存职务信息", notes = "", code =200,produces="application/json")
    public ApiResult saveProfession(Profession profession) {
        Map<String,Object> map = new HashMap<>();
        try {
            Profession profession1 = new Profession();
            profession1 =  expertService.saveProfession(profession);
            if (profession1==null){
                map.put("result",false);
                map.put("content",null);
                return ApiResult.ofFailureResultMsg(map,"有重复职务信息");
            }else {
                map.put("result",true);
                map.put("content",profession1);
                return ApiResult.ofSuccessResultMsg(map,"查询成果");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ApiResult.ofFailureResult("保存职务信息失败");
    }

    @RequestMapping(value = "/getProfession", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询职务信息列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> getProfession(
            @ApiParam(name = "unit", value = "单位", required = false) @RequestParam(name = "unit", defaultValue="") String unit,
            @ApiParam(name = "keyWord", value = "模糊查询条件", required = false) @RequestParam(name = "keyWord", defaultValue="") String keyWord,
            @ApiParam(name = "rule", value = "排序条件", required = false) @RequestParam(name = "rule", defaultValue="") String rule,
            Pageable pageable) {
        boolean x = false;
        try {
            return expertService.getProfession(unit,keyWord,rule,pageable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/delProfession", method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "删除职务信息", notes = "", code =200,produces="application/json")
    public boolean delProfession(
            @ApiParam(name = "id", value = "职务信息id", required = false)@RequestParam(name = "id", defaultValue="") String id) {
        boolean x = false;
        try {
            expertService.delProfession(id);
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "/getUnitList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询单位列表", notes = "", code =200,produces="application/json")
    public Map<String,Object> getUnitList(
            ) {
        Map<String,Object> rtnMap = new HashMap<>();
        try {
            UnitTypes[] yourEnums =UnitTypes.values();
            Arrays.asList(yourEnums).stream().forEach(e->rtnMap.put(e.getValue(), e.getDisplay()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnMap;
    }

    @RequestMapping(value = "/getExpertProfessionList", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "查询专家职务列表", notes = "", code =200,produces="application/json")
    public List<Map<String,Object>> getExpertProfessionList(
    ) {
        List<Map<String,Object>> rtnMap = new ArrayList<>();
        try {
            rtnMap = expertService.getExpertProfessionList();
            return rtnMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnMap;
    }
}
