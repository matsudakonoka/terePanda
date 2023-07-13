package com.cnpc.epai.core.workscene.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.core.workscene.entity.KeywordEntity;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordMaintenanceVo;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author liuTao
 * @version 1.0
 * @name KeywordEntityMapper
 * @description
 * @date 2021/10/14 10:19
 */
@Mapper
public interface KeywordEntityMapper extends BaseMapper<KeywordEntity> {

    @Select("select count(*) from epai_crpadmin.sr_meta_dataset_relation where s_dataset_id = #{dataSetId} and bsflag = 'N'")
    int countDataRelation(String dataSetId);

    @Select("select count(*) from sr_scene_keyword_re where sr_scene_keyword_re.keyword_name = #{keywordName} and sr_scene_keyword_re.bsflag = '1'")
    int isExit(String keywordName);

    @Select("select count(*) from sr_scene_keyword_re where sr_scene_keyword_re.keyword_sort_num = #{keywordSortNum} and sr_scene_keyword_re.bsflag = '1'")
    int isExitSort(Integer keywordSortNum);

    @Select("SELECT \n" +
            "\tK.keyword_id keywordId,\n" +
            "\tK.keyword_name keywordName,\n" +
            "\tT.type_name keywordType,\n" +
            "\tK.usage_count usageCount,\n" +
            "\tK.bsflag,\n" +
            "\tK.remarks,\n" +
            "\tK.create_date createDate,\n" +
            "\tK.create_user createUser,\n" +
            "\tK.update_date updateDate,\n" +
            "\tK.update_user updateUser\n" +
            "FROM\n" +
            "\tsr_scene_keyword_re K,\n" +
            "\tsr_scene_common_type T \n" +
            "WHERE\n" +
            "\tK.type_id = T.type_id\n" +
            " AND \n" +
            " K.bsflag='1'\n" +
            " AND \n" +
            " T.bsflag='1'\n" +
            " \tORDER BY\n" +
            " \tT.type_sort_num,K.keyword_sort_num")
    List<KeywordVo> findAll();

    @Select("SELECT \n" +
            "\tK.keyword_id keywordId,\n" +
            "\tK.keyword_name keywordName,\n" +
            "\tT.type_name keywordType,\n" +
            "\tK.usage_count usageCount,\n" +
            "\tK.bsflag,\n" +
            "\tK.remarks,\n" +
            "\tK.create_date createDate,\n" +
            "\tK.create_user createUser,\n" +
            "\tK.update_date updateDate,\n" +
            "\tK.update_user updateUser\n" +
            "FROM\n" +
            "\tsr_scene_keyword_re K,\n" +
            "\tsr_scene_common_type T \n" +
            "WHERE\n" +
            "\tK.type_id = T.type_id \n" +
            "AND\n" +
            "\tK.keyword_name like '%${keyword_name}%' \n" +
            " AND \n" +
            " K.bsflag='1'\n" +
            " AND \n" +
            " T.bsflag='1'\n" +
            " \tORDER BY\n" +
            " \tT.type_sort_num,K.keyword_sort_num")
    List<KeywordVo> findList(String keywordName);

    int updateUsaCount1(List<KeywordEntity> keywordList);

    @Select("SELECT \n" +
            "\tK.keyword_id keywordId,\n" +
            "\tK.keyword_name keywordName,\n" +
            "\tT.type_id typeId,\n" +
            "\tT.type_name typeName,\n" +
            "\tK.keyword_sort_num keywordSortNum,\n" +
            "\tK.keyword_status keywordStatus,\n" +
            "\tK.bsflag,\n" +
            "\tK.remarks,\n" +
            "\tK.create_date createDate,\n" +
            "\tK.create_user createUser,\n" +
            "\tK.update_date updateDate,\n" +
            "\tK.update_user updateUser\n" +
            "FROM\n" +
            "\tsr_scene_keyword_re K,\n" +
            "\tsr_scene_common_type T \n" +
            "WHERE\n" +
            "\tK.type_id = T.type_id \n" +
            " AND \n" +
            "  K.bsflag = '1'\n" +
            " AND \n" +
            "  T.bsflag = '1'\n" +
            "\tORDER BY \n" +
            "\tT.type_sort_num,K.keyword_sort_num ")
    IPage<KeywordMaintenanceVo> findPageListAll(Page page);


    @Select("SELECT \n" +
            "\tK.keyword_id keywordId,\n" +
            "\tK.keyword_name keywordName,\n" +
            "\tT.type_id typeId,\n" +
            "\tT.type_name typeName,\n" +
            "\tK.keyword_sort_num keywordSortNum,\n" +
            "\tK.keyword_status keywordStatus,\n" +
            "\tK.bsflag,\n" +
            "\tK.remarks,\n" +
            "\tK.create_date createDate,\n" +
            "\tK.create_user createUser,\n" +
            "\tK.update_date updateDate,\n" +
            "\tK.update_user updateUser\n" +
            "FROM\n" +
            "\tsr_scene_keyword_re K,\n" +
            "\tsr_scene_common_type T \n" +
            "WHERE\n" +
            "\tK.type_id = T.type_id \n" +
            " AND \n" +
            "  K.bsflag = '1'\n" +
            " AND \n" +
            "  T.bsflag = '1'\n" +
            " AND\n" +
            " T.type_id = #{typeId}" +
            "\tORDER BY \n" +
            "\tT.type_sort_num,K.keyword_sort_num ")
    IPage<KeywordMaintenanceVo> findPageList(Page page, @Param("typeId") String typeId);

    @Update("update sr_scene_keyword_re set bsflag = '0' where keyword_id = #{keywordId}")
    int updateBsflagByKeywordId(@Param("keywordId") String keywordId);

    @Update("update sr_scene_keyword_re" +
            " set keyword_sort_num = keyword_sort_num + 1 " +
            "where keyword_sort_num between #{keywordSortNum} and " +
            " (select keyword_sort_num from sr_scene_keyword_re where keyword_id = #{keywordId}) " +
            "and " +
            " type_id = (select type_id from sr_scene_keyword_re where keyword_id = #{keywordId})")
    void updateKeywordSortNum(@Param("keywordSortNum") Integer keywordSortNum,@Param("keywordId") String keywordId);


    @Update("update sr_scene_keyword_re set keyword_sort_num = keyword_sort_num -1  where keyword_sort_num> #{keywordSortNum}")
    void delAndUpdateKeywordSortNum(@Param("keywordSortNum") Integer keywordSortNum);

    @Update("update sr_scene_keyword_re" +
            " set keyword_sort_num = keyword_sort_num -1  " +
            "where keyword_sort_num> " +
            "(select keyword_sort_num from sr_scene_keyword_re where keyword_id = #{keywordId}) " +
            "and " +
            " type_id = (select type_id from sr_scene_keyword_re where keyword_id = #{keywordId})")
    void delAndUpdateSort(@Param("keywordId") String keywordId);

    @Update("update sr_scene_keyword_re" +
            " set keyword_sort_num = keyword_sort_num + 1 " +
            "where keyword_sort_num >= #{keywordSortNum} and " +
            " type_id = #{typeId}")
    void updateAddKeywordSortNum(@Param("keywordSortNum") Integer keywordSortNum,@Param("typeId") String typeId);
}
