package org.example.nogizaka46.termFifth.service;

import org.example.nogizaka46.termFifth.entity.TerePandaVideos;

import java.util.List;

public interface TeresaVideoService {

    int deleteVideos(List<String> ids);

    List<TerePandaVideos> queryVideos(int pageNum, int pageSize);

    int addVideos(List<TerePandaVideos> terePandaVideos);
}
