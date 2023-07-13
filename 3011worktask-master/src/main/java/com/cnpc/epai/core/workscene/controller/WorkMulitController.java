package com.cnpc.epai.core.workscene.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.workscene.entity.Work;
import com.cnpc.epai.core.workscene.pojo.vo.*;
import com.cnpc.epai.core.workscene.service.*;
import com.cnpc.epai.core.workscene.service.impl.WorkMulitServiceImpl;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "工作创建分组")
@RestController
@RequestMapping("/core/worktask/Mulit")
public class WorkMulitController {

    @Autowired
    private WorkMulitService workMulitService;

    @Autowired
    private GeoMulitService geoMulitService;

    @GetMapping("/getGeoTypeCountEx")
    public Object getGeoTypeCountEx(String workId) {
        return geoMulitService.getGeoTypeCountEx(workId);
    }

    @ApiOperation(value = "创建工作, workId 为空创建工作，不为空修改工作")
    @PostMapping("/createWorkByReport")
    public ApiResult createWorkByReport(@RequestBody WorkMulitVo workMulitVo) {
        ApiResult apiResult = ApiResult.newInstance();
        apiResult.setFlag(false);
        if (workMulitVo != null) {
            String objectId = workMulitVo.getObjectId();
            String objectName = workMulitVo.getObjectName();
            String objectType = workMulitVo.getObjectType();
            String templateId = workMulitVo.getTemplateId();
            List<JSONObject> chargeUsers = workMulitVo.getChargeUsers();
            Date startTime = workMulitVo.getStartTime();
            Date endTime = workMulitVo.getEndTime();
            boolean notBlankchargeUser = false;
            if (chargeUsers != null) {
                for (JSONObject chargeUser : chargeUsers) {
                    if (StringUtils.isEmpty(chargeUser.getString("chargeUserId"))) {
                        notBlankchargeUser = true;
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(objectId) || StringUtils.isEmpty(objectName) || StringUtils.isEmpty(objectType)) {
                apiResult.setMsg("参数错误：研究对象属性不能为空！");
            } else if (StringUtils.isEmpty(templateId)) {
                apiResult.setMsg("参数错误：模板id不能为空！");
            } else if (chargeUsers == null || chargeUsers.size() == 0 || notBlankchargeUser) {
                apiResult.setMsg("参数错误：负责人不能为空！");
            } else if (StringUtils.isEmpty(startTime.toString())) {
                apiResult.setMsg("参数错误：开始时间不能为空！");
            } else if (StringUtils.isEmpty(endTime.toString())) {
                apiResult.setMsg("参数错误：结束时间不能为空！");
            } else {
                try {
                    apiResult= workMulitService.createWorkByReport(workMulitVo);
                } catch (Exception e) {
                    apiResult.setMsg("工作创建失败，" + e.getMessage());
                }
            }
        } else {
            apiResult.setMsg("参数为空。");
        }
        return apiResult;
    }

    @Autowired
    WorkMulitServiceImpl workMulitServiceImpl;

    @ResponseBody
    @RequestMapping(value = "/saveUpdateByOtherObject", method = RequestMethod.POST)
    @ApiOperation(value = "保存数据至其他对象", notes = "", code =200,produces="application/json")
    public ApiResult saveUpdateByOtherObject(
            @ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
            @ApiParam(name = "dataTargetTypeZt", value = "区分研究资料或成果列表", required = true) @RequestParam(name = "dataTargetTypeZt", defaultValue="") String dataTargetTypeZt,
            @ApiParam(value = "上传数据",required = true)@RequestBody JSONArray data,
            SrTaskTreeData srTaskTreeData, HttpServletRequest httpServletRequest) {
        ApiResult apiResult = ApiResult.newInstance();
        try {
            if(StringUtils.isEmpty(dataTargetTypeZt) || StringUtils.isEmpty(srTaskTreeData.getDataType())){
                apiResult.setMsg("参数：dataTargetTypeZt不能为空");
                apiResult.setResult(null);
            }else {
                apiResult = workMulitServiceImpl.saveUpdateByOtherObject(projectId,data,srTaskTreeData,dataTargetTypeZt,httpServletRequest);
            }
        } catch (Exception e) {
            apiResult.setMsg(e.getMessage());
            apiResult.setResult(e);
            e.printStackTrace();
        }
        return apiResult;
    }
}
