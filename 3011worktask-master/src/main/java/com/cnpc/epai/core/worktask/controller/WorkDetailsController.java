package com.cnpc.epai.core.worktask.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ApiResult;
import com.cnpc.epai.core.worktask.domain.SrWorkTask;
import com.cnpc.epai.core.worktask.repository.SrWrokTaskRepository;
import com.cnpc.epai.core.worktask.service.SrWorkTaskService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * @Description: controller
 * @author 王博
 * @version 1.0.0
 * @date  2021/9/7
 */
@RestController
@RequestMapping("/core/worktask")
public class WorkDetailsController {

    @Autowired
    SrWorkTaskService workInfoService;

    @Autowired
    SrWrokTaskRepository workInfoRepository;

    @RequestMapping(value = "/getScenesTemplate", method = RequestMethod.GET)
    @ApiOperation(value = "首页-业务场景筛选列表", notes = "", code =200,produces="application/json")
    public JSONArray getScenesTemplate(HttpServletRequest request) {
        return workInfoService.getScenesTemplate(request);
    }

    @RequestMapping(value = "/getWorkList", method = RequestMethod.POST)
    @ApiOperation(value = "首页-工作列表筛选、搜索按钮",notes = "参数：{\"templateId\":\"逗号分隔\",\"taskName\":\"\"}",code=200,produces="application/json")
    public Page<SrWorkTask> getWorkList(Pageable page, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getWorkList(page,data);
    }

    @RequestMapping(value = "/getResultsTree", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-工作成果导航&&工作成果导航关键词搜索",notes = "参数：{\"taskId\":\"\",\"ResultKeyWords\":\"\"}",code=200,produces="application/json")
    public Object getResultsTree(HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultsTree(request,data);
    }

    @RequestMapping(value = "/getResultsTreeStatus", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-工作成果导航-数据集状态显示",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public Object getResultsTreeStatus(HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultsTreeStatus(request,data);
    }

    @RequestMapping(value = "/getWorkInfoList", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-工作详情",notes = "参数：{\"taskId\":\"\",\"workId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public JSONObject getWorkInfoList(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getWorkInfoList(request,data,page);
    }

    @RequestMapping(value = "/getResultList", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-任务分析",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public Map getResultList(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultList(request,data,page);
    }

    @RequestMapping(value = "/getResultListBy", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-成果浏览条件筛选/分页",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\",\"projectUser\":\"\",\"status\":\"\",\"startTime\":\"\",\"endTime\":\"\",\"dataSetId\":\"\"}",code=200,produces="application/json")
    public Map getResultListBy(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultListBy(request,data,page);
    }

    @RequestMapping(value = "/getResultListUser", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-项目成员",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public Map getResultListUser(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultListUser(request,data,page);
    }

    @RequestMapping(value = "/getResultPage", method = RequestMethod.POST)
    @ApiOperation(value = "查看成果",notes = "参数：{\"taskId\":\"\",\"dataSetId\":\"\"}",code=200,produces="application/json")
    public List<JSONObject> getResultPage(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultPage(request,data,page);
    }

    @RequestMapping(value = "/getFrontPageResult", method = RequestMethod.GET)
    @ApiOperation(value = "首页-我的成果/查看全部/分页",notes = "",code=200,produces="application/json")
    public Map getFrontPageResult(Pageable page, HttpServletRequest request) {
        return workInfoService.getFrontPageResult(request,page);
    }

    /*@RequestMapping(value = "/getWorkInfoList", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-工作详情",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public JSONObject getWorkInfoList(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getWorkInfoList1(request,data,page);
    }

    @RequestMapping(value = "/getResultList", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-任务分析",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public Map getResultList(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultList1(request,data,page);
    }

    @RequestMapping(value = "/getResultListBy", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-成果浏览条件筛选/分页",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\",\"projectUser\":\"\",\"status\":\"\",\"startTime\":\"\",\"endTime\":\"\",\"dataSetId\":\"\"}status审核状态 1.待审核 2.已通过 3.未通过 4.已提交 5.审核中",code=200,produces="application/json")
    public Map getResultListBy(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultListBy1(request,data,page);
    }

    @RequestMapping(value = "/getResultListUser", method = RequestMethod.POST)
    @ApiOperation(value = "工作查看-项目成员",notes = "参数：{\"taskId\":\"\",\"treeNodeId\":\"\"}",code=200,produces="application/json")
    public Map getResultListUser(Pageable page, HttpServletRequest request, @ApiParam(value = "传参json", required = true) @RequestBody JSONObject data) {
        return workInfoService.getResultListUser1(request,data,page);
    }*/

    @RequestMapping(value = "/searchData", method = RequestMethod.POST)
    @ApiOperation(value = "数据检索", tags={"研究管理-井数据查询"}, code = 200, produces = "application/json")
    public ApiResult searchData(@ApiParam(name = "projectId", value = "项目ID", required = true) @RequestParam(name = "projectId", defaultValue="") String projectId,
                                @ApiParam(name = "datasetIds", value = "数据集ID(逗号分隔)", required = true) @RequestParam(name = "datasetIds", defaultValue="") String datasetIds,
                                @ApiParam(name = "wellNames", value = "井名列表(逗号分隔)", required = true) @RequestParam(name = "wellNames", defaultValue="") String wellNames,
                                HttpServletRequest request) {
        try {
            List<JSONObject> data = workInfoService.searchData(projectId,datasetIds,wellNames,request);
            return ApiResult.ofSuccessResultMsg(data, "查询成功!");
        } catch (Exception e) {
            return ApiResult.ofFailure();
        }
    }

    public static void main(String[] args) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();

        String projectId = "ACTIJD100002260";
        String datasetIds = "jpvHKk93xh1E7LwcZfPxZmH6jh3DDut9,vw4x24D8V4FUQJivrxa8Y8Yq1LBz9io1,HN7t6gosVFx2JTUuw8TYPYkJ3VcEp9YL,5d7z5RE6pI7CTpXdAUF6GwFqk5Aehr2B,ibP3FeD03uiVaDnLhETlCnjCYhRZtKNA,rl6zerymnFpmNiJ78j38MM3XYaL4HQF1,V0l3QzCvPPf23vfEc4etWFqt2qc37Aaw,rl6zerymnFpmNiJ78j38MM3XYaL4HQFt,XfpYFM5LuiBA9GCi8ol3PIxSu69gyQE1,eh6g8feMVwveGlvghuq691V1FvBZ9aL1,4vPJ1Oaj2ClCqTx5LU5J2t53xpcen11l,JyUoNGuH7kymPc2loZ8Khi0Oas2ZAIaH,VLKi3SY3dGrXE0Hi10ht8X2ntFWrOgaU,vrioki1vWTAaMh0xeOtjG5QGAL4p91Ac,y6wbx1A345BSIoF6wCd3A1Sr3qxNKUMQ,w41h8SeXATO3AhS38miUUBOuEZ8kwfCu,7axd2IfaXnJxybeDSD3PgkhkS1qPBGd1,DdL8gzKnB7KUbeVVqYpeRMSDXYTA82NS,nvKYWowVewlGtm4YCxkrfUxVINUKHMmX,lRztc4SqHN3zQLVVBvPdIk5plFbb8isP,Klv9VWWlJRXjld4QX5Q6sKmpSTELvec0,0hDHQRNhALISWNDckc0AuTOV1BmMqdJt,Jo23O8xa23ckQfiElONk2EejM4I1DMAB,wpwtGIEyvXHTrIG3a4ITFoD8ld2yClv1,7LcLQIqmVe5jxmyUkSkEO3Z9RlvlMlKJ,aIHdQ9QZJSLnw4JNSyBySPATBQYAROuK,RAw4Kw7VqrCWClISPnjWEP9iaZZ0f8AL,7lQXE3sHQRwXgzZj9XPOiSZ5AI4LNxi9,cx2bq3AKnsV9XoVW669KVMegHEIoM2mc";
        String wellNames = "大10-1,堡古2";

        serviceName.append("http://www.pcep.cloud/core/dataset/batch/udb/searchdata?projectId="+projectId+"&datasetIds="+datasetIds+"&wellNames="+wellNames+"");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization","Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoid2FuZ2RvbmduYSIsInVzZXJfaWQiOiJldnRYRUl2RWxqT0tyeTlwelVxZkFyangiLCJ1c2VyX25hbWUiOiJ3YW5nZG9uZ25hIiwic2NvcGUiOlsib3BlbmlkIl0sIm9yZ2FuaXphdGlvbiI6Ik9SR0FKRDEwMDAwMDExNCIsImlzcyI6ImEzNmMzMDQ5YjM2MjQ5YTNjOWY4ODkxY2IxMjcyNDNjIiwiZXhwIjoxNjUwNTA3Nzg3LCJkaXNwbGF5X25hbWUiOiLnjovlhqzlqJwiLCJyZWdpb24iOiJKRCIsImlhdCI6MTY0NzkxNTc4NzQxNywianRpIjoiYTEzODY0OGEtNzVmNi00YTZhLTgyN2ItZjM2MmIwYzk5MWUzIiwiY2xpZW50X2lkIjoid2ViYXBwIn0.l0JsMhKA2wzEP4yvzrV2xkGpAZd6W4H2iEi3TiyWmtP6fENK3uZCWIl14kqlqq7SxwaqE0gXkUyNGVE9Zutx_dYs9BgqugIrRTTiK7QVorQ0VOFS1c2dzqpR_CzTnpks2Hq9DngQtJCWwLZyXrbl8fWcumNcXC8J5G0fTCbT6L467O_lThXwZWLIo8fgSBHgMJCTHneHPIeqr2Ic4I9b9RwoxAX3AXQxALjofU9PZfKaigfnhopaKbLyNa7BLAc2R5kaPR-dGfiF07GrzF0FLp6CptdO1LWAyUAv--bIFW42DFQGv49zYn1QN3RMITKyOqg1vAoUYJ9a9MvHvo1T4g");
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        String s = restTemplate.postForObject(serviceName.toString(), httpEntity, String.class);
        System.out.println(s);
    }
}
