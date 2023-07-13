package generate;

import generate.TerePandaBlogs;

public interface TerePandaBlogsMapper {
    int deleteByPrimaryKey(String id);

    int insert(TerePandaBlogs record);

    int insertSelective(TerePandaBlogs record);

    TerePandaBlogs selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TerePandaBlogs record);

    int updateByPrimaryKey(TerePandaBlogs record);
}