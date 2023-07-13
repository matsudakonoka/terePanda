package org.example.nogizaka46.termFifth.Controller;

import org.example.nogizaka46.termFifth.entity.ResultApi;
import org.example.nogizaka46.termFifth.entity.TerePandaVideos;
import org.example.nogizaka46.termFifth.service.TeresaVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nogizaka46/termfifth/ikedateresa")
@CrossOrigin(origins = "*")
public class TeresaVideoApi {

    @Autowired
    private TeresaVideoService teresaVideoService;


    @RequestMapping(value = "/queryVideos", method = RequestMethod.GET,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi queryBlog(int pageNum,int pageSize) {
        try{
            List<TerePandaVideos> videos = this.teresaVideoService.queryVideos(pageNum,pageSize);
            return ResultApi.ofSuccessResult(videos);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }


    @RequestMapping(value = "/deleteVideos", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi deleteVideos(@RequestBody List<String> ids) {
        try{
            int num = this.teresaVideoService.deleteVideos(ids);
            return ResultApi.ofSuccessResult(num);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

    @RequestMapping(value = "/addVideos", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi addVideos(@RequestBody List<TerePandaVideos> terePandaVideos) {
        try{
            int num = this.teresaVideoService.addVideos(terePandaVideos);
            return ResultApi.ofSuccessResult(num);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

}
