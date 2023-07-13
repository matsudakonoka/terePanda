package com.cnpc.epai.core.workscene.mapper;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cnpc.epai.core.workscene.entity.SceneKeywordRelation;
import com.cnpc.epai.core.workscene.pojo.vo.SceneKeywordRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 *  @Name: KeywordRelationMapper
 *  @Description:
 *  @Version: V1.0.0
 *  @Author: 陈淑造
 *  @create 2021/11/19 18:14
 */
@Mapper
public interface KeywordRelationMapper extends BaseMapper<SceneKeywordRelation> {

    @Transactional
    @Select("SELECT r.*,c.type_name FROM sr_scene_keyword_relation r,sr_scene_common_type c, sr_scene_keyword_re k where r.type = #{type} and r.bsflag = '1' and r.application_id = #{application_id} and r.keyword_id = k.keyword_id and k.type_id = c.type_id ORDER BY r.sort_num,r.keyword_name")
    List<SceneKeywordRelationVo> selectRelationSortedVo(@Param("type") String type, @Param("application_id")  String applicationId);

    @Select("SELECT * FROM sr_scene_keyword_relation where type = #{type} and bsflag = '1' and application_id = #{application_id} ORDER BY sort_num,keyword_name")
    List<SceneKeywordRelation> selectRelationSorted(@Param("type") String type,@Param("application_id")  String applicationId);

    @Update("update sr_scene_keyword_relation\n" +
            "        set sort_num = sort_num + 1\n" +
            "        where sort_num >= #{sortNum} and bsflag = '1'")
    void addSortNums(int sortNum);

    @Update("update sr_scene_keyword_relation\n"+
            "        set type = #{type},keyword_id = #{keywordId},keyword_name = #{keywordName},sort_num = #{sortNum},application_id = #{applicationId},remarks = #{remarks}\n"+
            "        where relation_id = #{relationId} and bsflag = '1'")

    int updateByRelationId(@Param("relationId")String relationId, @Param("type")String type, @Param("keywordId")String keywordId, @Param("keywordName")String keywordName, @Param("sortNum")String sortNum, @Param("applicationId")String applicationId, @Param("remarks")String remarks);


   @Update("<script>"+
           "update sr_scene_keyword_relation\n" +
           "set bsflag = '0' \n" +
           "where\n" +
           "application_id = #{applicationId,jdbcType=VARCHAR}\n" +
           "and keyword_id in\n" +
           "<foreach collection='keywordIdList' open='(' item='item' separator=',' close=')'> #{item,jdbcType=VARCHAR}</foreach>"+
           "</script>")
   int batchDeleteRelation(@Param("applicationId")String applicationId,@Param("keywordIdList") Collection<String> keywordIdList);

}
