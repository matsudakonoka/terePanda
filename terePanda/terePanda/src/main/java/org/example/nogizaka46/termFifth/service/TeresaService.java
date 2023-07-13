package org.example.nogizaka46.termFifth.service;

import org.example.nogizaka46.termFifth.entity.TerePandaBlogs;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface TeresaService {

    String getBlogData(String id) throws IOException;

    List<TerePandaBlogs> queryBlog(int pageNum,int pageSize);

    int uploadFilesList(List<MultipartFile> mFile) throws IOException;

    int deleteBlog(List<String> ids);
}
