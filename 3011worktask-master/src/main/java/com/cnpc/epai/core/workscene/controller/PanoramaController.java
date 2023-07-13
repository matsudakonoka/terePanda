package com.cnpc.epai.core.workscene.controller;

import com.cnpc.epai.core.workscene.commom.FileReader;
import com.cnpc.epai.core.workscene.service.DataService;
import com.cnpc.epai.core.workscene.service.PanoramaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "业务全景图分组")
@RestController
@RequestMapping("/core/worktask")
public class PanoramaController {

    @Autowired
    PanoramaService panoramaService;

    @Autowired
    DataService dataService;


    @ApiOperation(value = "业务全景图")
    @GetMapping("/panorama")
    public Object getPanorama(String workId, String nodeId) {
        return panoramaService.panorama(workId, nodeId);
    }

    @ApiOperation(value = "统计图数据")
    @GetMapping("/getAchieve")
    public Object getAchieve(@RequestParam String workId, @RequestParam String nodeId) {
        return panoramaService.getAchieve(workId, nodeId);
    }

}
