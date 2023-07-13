package com.cnpc.epai.core.workscene.controller;

import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.workscene.service.SeisMicInterPretationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @ClassName: SeisMicInterPretationController
 * @Description: 地震数据微服务
 * @Author
 * @Date 2022/10/25
 * @Version 1.0
 */

@Api(tags = "地震处理分组")
@RestController
@RequestMapping("/common/seismicinterpretation")
@Slf4j
public class SeisMicInterPretationController {

    @Autowired
    SeisMicInterPretationService seisMicInterPretationService;

    /**
     * 解析层位文件并上传到fs上
     *
     * @param fileID
     * @return String  新文件ID
     */
    @RequestMapping(value = "/{fileID}/parseStratumFileToFS", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "解析层位文件", notes = "解析层位文件", code = 200, produces = "application/text")
    public String parseStratumFileToFS(@ApiParam(name = "fileID", value = "文件ID", required = true) @PathVariable String fileID, HttpServletRequest request) throws BusinessException {
        String standFileID= seisMicInterPretationService.parseStratumFileToFS(fileID,null);
        return standFileID;
    }

    @RequestMapping(value = "/{fileID}/getStratumData", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取层位信息数据", notes = "获取层位信息数据", code = 200, produces = "application/octet-stream")
    public void getStratumData(@ApiParam(name = "fileID", value = "文件ID", required = true) @PathVariable String fileID, HttpServletResponse response) throws BusinessException {
        try {
            byte[] data = seisMicInterPretationService.getFileDataFromHttp(fileID,"rf3h",false,null);
            response.reset();
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            @Cleanup
            OutputStream out = response.getOutputStream();
            out.write(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
