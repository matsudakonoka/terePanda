package org.example.nogizaka46.termFifth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.nogizaka46.termFifth.entity.TerePandaVideos;
import org.example.nogizaka46.termFifth.mapper.TerePandaVideosMapper;
import org.example.nogizaka46.termFifth.service.TeresaVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeresaVideoServiceImpl implements TeresaVideoService {

    @Autowired
    private TerePandaVideosMapper terePandaVideosMapper;

    @Override
    public int deleteVideos(List<String> ids) {
        int num;
        num = terePandaVideosMapper.deleteBatchIds(ids);
        return num;
    }

    @Override
    public List<TerePandaVideos> queryVideos(int pageNum, int pageSize) {
        QueryWrapper<TerePandaVideos> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("time");
        Page<TerePandaVideos> page = new Page<>(pageNum,pageSize);
        IPage<TerePandaVideos> iPage = terePandaVideosMapper.selectPage(page,queryWrapper);
        return iPage.getRecords();
    }

    @Override
    public int addVideos(List<TerePandaVideos> terePandaVideos) {
        int num = 0;
        for (TerePandaVideos terePandaVideo : terePandaVideos) {
            terePandaVideo.setId(String.valueOf(UUID.randomUUID()));
            terePandaVideosMapper.insert(terePandaVideo);
            num++;
        }

        return num;
    }
}
