package org.example.nogizaka46.termFifth.Controller;

import org.example.nogizaka46.termFifth.entity.ResultApi;
import org.example.nogizaka46.termFifth.entity.TerePandaBlogs;
import org.example.nogizaka46.termFifth.service.TeresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/nogizaka46/termfifth/ikedateresa")
@CrossOrigin(origins = "*")
public class TeresaApi {

    @Autowired
    private TeresaService teresaService;

    //获取blog
    @RequestMapping(value = "/getBlogData", method = RequestMethod.GET,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi getBlogData(@RequestParam(required = false ,value = "id") String id) {
        try{
            String blogData = this.teresaService.getBlogData(id);
            return ResultApi.ofSuccessResult(blogData);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

    @RequestMapping(value = "/queryBlog", method = RequestMethod.GET,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi queryBlog(int pageNum,int pageSize) {
        try{
            List<TerePandaBlogs> blogs = this.teresaService.queryBlog(pageNum,pageSize);
            return ResultApi.ofSuccessResult(blogs);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

    @RequestMapping(value = "/uploadFiles", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi uploadFilesList(List<MultipartFile> mFile) {
        try{
            int num = this.teresaService.uploadFilesList(mFile);
            return ResultApi.ofSuccessResult(num);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

    @RequestMapping(value = "/deleteBlog", method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultApi deleteBlog(@RequestBody List<String> ids) {
        try{
            int num = this.teresaService.deleteBlog(ids);
            return ResultApi.ofSuccessResult(num);
        }catch (Exception e){
            e.printStackTrace();
            return ResultApi.ofFailureResult("接口调用异常");
        }
    }

}
