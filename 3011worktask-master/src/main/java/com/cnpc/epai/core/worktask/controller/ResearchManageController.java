package com.cnpc.epai.core.worktask.controller;

import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.worktask.domain.SrFocusonLink;
import com.cnpc.epai.core.worktask.service.ResearchManageService;
import com.cnpc.epai.core.worktask.service.SrFocusonLinkService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: controller
 * @author 王博
 * @version 1.0.0
 * @date  2021/11/23
 */
@RestController
@RequestMapping("/core/worktask")
public class ResearchManageController {

    @Autowired
    ResearchManageService researchManageService;
    @Autowired
    SrFocusonLinkService srFocusonLinkService;

    @RequestMapping(value = "/getUnitWorkTree", method = RequestMethod.GET)
    @ApiOperation(value = "工作单元树&节点模糊搜索", tags={"研究管理"}, notes = "", code =200,produces="application/json")
    public Object getUnitWorkTree(@ApiParam(name = "treeNodeName", value = "业务名称", required = false) @RequestParam(name = "treeNodeName", defaultValue="") String treeNodeName,
                                    HttpServletRequest request) {

        return researchManageService.getUnitWorkTree(treeNodeName,request);
    }

    @RequestMapping(value = "/saveOrUpdateFocusLink", method = RequestMethod.POST)
    @ApiOperation(value = "添加或修改研究类型的url", tags={"研究管理"}, notes = "", code =200,produces="application/json")
    public ApiResult saveOrUpdateFocusLink(@ApiParam(value = "研究类型URL配置对象",required = true)@RequestBody SrFocusonLink srFocusonLink) {
        try {
            SrFocusonLink result = srFocusonLinkService.saveOrUpdateFocusLink(srFocusonLink);
            ApiResult apiResult = ApiResult.ofSuccessResult(result);
            apiResult.setCode("200");
            return apiResult;
        }catch (Exception e){
            e.printStackTrace();
            ApiResult apiResult = ApiResult.ofFailureResult(e);
            apiResult.setCode("400");
            return apiResult;
        }
    }

    @RequestMapping(value = "/findLinkByResearchType", method = RequestMethod.GET)
    @ApiOperation(value = "根据研究类型查询链接", tags={"研究管理"}, notes = "", code =200,produces="application/json")
    public ApiResult findLinkByResearchType( @ApiParam(value = "研究类型", type="String",required = true) @RequestParam(required = true) String researchType,
                                             @ApiParam(value = "是否启用，1在用0不在用", type="String",required = false) @RequestParam(required = false) Integer isValid) {
        try {
            SrFocusonLink result = srFocusonLinkService.findLinkByResearchType(researchType,isValid);
            ApiResult apiResult = ApiResult.ofSuccessResult(result);
            apiResult.setCode("200");
            return apiResult;
        }catch (Exception e){
            e.printStackTrace();
            ApiResult apiResult = ApiResult.ofFailureResult(e);
            apiResult.setCode("400");
            return apiResult;
        }
    }

}
