package org.example.nogizaka46.termFifth.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.example.nogizaka46.termFifth.config.MybatisLimtConfig;
import org.example.nogizaka46.termFifth.entity.TerePandaBlogs;
import org.example.nogizaka46.termFifth.mapper.TerePandaBlogsMapper;
import org.example.nogizaka46.termFifth.service.TeresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.System.in;
import static java.lang.System.out;

@Service
public class TeresaServiceImpl implements TeresaService {

    @Autowired
    private TerePandaBlogsMapper terePandaBlogsMapper;

    @Override
    public String getBlogData(String id) throws IOException {
        TerePandaBlogs terePandaBlogs = terePandaBlogsMapper.selectById(id);
        String fileName = queryFile(terePandaBlogs.getFilename());
        return fileName;
    }

    List<File> searchFiles(File folder, final String keyword) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
               if (file.getName().toLowerCase().contains(keyword)) {
                    return true;
                }
                return false;
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                } else {
                    // 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, keyword));
                }
            }
        }
        return result;
    }

    String queryFile(String fileName) throws IOException {
        long startTime=System.currentTimeMillis();
        List<File> files = searchFiles(new File("/poka/teresaBlog"), fileName);
        out.println("共找到:" + files.size() + "个文件");
        String line = "";
        String txt = "";
        for (File file : files) {
            out.println(file.getAbsolutePath());
            String filePath = file.getAbsolutePath();
            File fileblog = new File(filePath);
            FileReader fr = new FileReader(fileblog);
            BufferedReader br = new BufferedReader(fr);
            while((line = br.readLine()) != null){
                //process the line
                out.println(line);
                txt = txt+line;
            }
        }
        long endTime=System.currentTimeMillis();
        float excTime=(float)(endTime-startTime)/1000;
        out.println("执行时间："+excTime+"s");
        return txt;
    }

    //查列表
    @Override
    public List<TerePandaBlogs> queryBlog(int pageNum,int pageSize) {
        QueryWrapper<TerePandaBlogs> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("date");
        Page<TerePandaBlogs> page = new Page<>(pageNum,pageSize);
        IPage<TerePandaBlogs> iPage = terePandaBlogsMapper.selectPage(page,queryWrapper);
        return iPage.getRecords();
    }

    //上传文件
    @Override
    public int uploadFilesList(List<MultipartFile> mFile) throws IOException {
        int z = 0;
        for (int i = 0; i < mFile.size(); i++) {
            uploadFiles(mFile.get(i));
            z++;
        }
        return z;
    }



    int uploadFiles(MultipartFile mFile) throws IOException {
        try {
            TerePandaBlogs terePandaBlogs = new TerePandaBlogs();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            InputStream in = mFile.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            File file = new File("/poka/teresaBlog");
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            OutputStream out = new FileOutputStream("/poka/teresaBlog/"+mFile.getOriginalFilename());
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            //获取title
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/poka/teresaBlog/"+mFile.getOriginalFilename()));
            String line = bufferedReader.readLine();;
            List<Integer> before = getIndex(line,"<p>");
            List<Integer> after = getIndex(line,"</p>");

            for (int i = 0; i < before.size(); i++) {
                String bak = StringUtils.substring(line,before.get(i)+3,after.get(i));
                if(!(null == bak||"".equals(bak.replaceAll("\\s*", "")))){
                    line = bak;
                    break;
                }
            }

            terePandaBlogs.setId(String.valueOf(UUID.randomUUID()));
            //设置日期
            String day = mFile.getOriginalFilename();
            day = StringUtils.substringBefore(day,".");

            System.out.println("day:"+day);
            terePandaBlogs.setDate(new SimpleDateFormat("yyyyMMdd").parse(day));
            terePandaBlogs.setTitle(line);
            terePandaBlogs.setFilename(mFile.getOriginalFilename());
            QueryWrapper<TerePandaBlogs> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("filename",mFile.getOriginalFilename());
            terePandaBlogsMapper.delete(queryWrapper);
            terePandaBlogsMapper.insert(terePandaBlogs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
            in.close();
        }
        return 0;
    }

    List<Integer> getIndex(String strings, String str){
        List<Integer> list=new ArrayList<>();
        int flag=0;
        while (strings.indexOf(str)!=-1){
            //截取包含自身在内的前边部分
            String aa= strings.substring(0,strings.indexOf(str)+str.length());
            flag=flag+aa.length();
            list.add(flag-str.length());
            strings=strings.substring(strings.indexOf(str)+str.length());
        }
        return list;
    }

    @Override
    public int deleteBlog(List<String> ids) {
        int num;
        num = terePandaBlogsMapper.deleteBatchIds(ids);
        return num;
    }

}
