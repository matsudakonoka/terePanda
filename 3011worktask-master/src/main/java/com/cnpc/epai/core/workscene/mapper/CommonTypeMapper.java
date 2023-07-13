package com.cnpc.epai.core.workscene.mapper;
/**
 * Copyright  2021
 * 昆仑数智有限责任公司
 * All  right  reserved.
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnpc.epai.core.workscene.entity.CommonType;
import com.cnpc.epai.core.workscene.pojo.vo.KeywordTypeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.mapstruct.Mapper;
import java.util.List;

/**
 *  @Name: CommonTypeMapper
 *  @Description:
 *  @Version: V1.2.1
 *  @Author: 陈淑造
 *  @create 2021/10/14 17:29
 */
@Mapper
public interface CommonTypeMapper extends BaseMapper<CommonType> {

    @Select("select count(*) from sr_scene_common_type  \n" +
            "where sr_scene_common_type.type_name= #{typeName} \n" +
            "and sr_scene_common_type.bsflag = '1' \n" +
            "and sr_scene_common_type.common_type = '1'")
    int isExistName(String typeName);

    //查询通用类型,根据排序号返回
    @Select("<script>  select *\n" +
            "        from sr_scene_common_type\n" +
            "        where common_type = #{common_type} and bsflag = '1'  " +
            "        <if  test= \"typeStatus != null \"> and type_status = #{typeStatus}</if>" +
            "        order by type_sort_num </script>")
    List<CommonType> getCommonType(@Param("common_type") String commonType,@Param("typeStatus") String typeStatus);


    //根据id更改bsflag与type_status
    @Update("update sr_scene_common_type\n" +
            "        set bsflag = '0' , type_status = '0' " +
            "        where type_id = #{typeId}")
    int logicDelete(@Param("typeId")String typeId);



    //更改排序
    @Update("update sr_scene_common_type\n" +
            "        set type_sort_num = type_sort_num + #{addNum}\n" +
            "        where type_sort_num > #{type_sort_num} and common_type = #{common_type}")
    int changeSortNums(@Param("common_type") String commonType,@Param("addNum") int addNum,@Param("type_sort_num") int type_sort_num);

    @Select("select type_id typeId,type_name typeName,type_sort_num typeSortNum from sr_scene_common_type where common_type='1' and bsflag = '1' and type_status = '1'")
    List<KeywordTypeVo> keywordTypeDownList();

    @Select("select distinct(t.type_id) typeId," +
            "t.type_name typeName," +
            "t.type_sort_num typeSortNum" +
            " from sr_scene_common_type t,sr_scene_keyword_re k" +
            " where " +
            " k.type_id = t.type_id " +
            " and k.bsflag = '1' " +
            " and t.bsflag = '1' ")
    List<KeywordTypeVo> keywordTypeQueryDownList();


    @Select("<script>  select *\n" +
            "        from sr_scene_common_type\n" +
            "        where common_type = #{common_type} and type_name like '%${type_name}%' and bsflag = '1'  " +
            "        <if  test= \"typeStatus != null \"> and type_status = #{typeStatus}</if>" +
            "        order by type_sort_num </script>")
    Page<CommonType> findPageTypes(Page page,@Param("common_type") String typeNum, @Param("typeStatus")String typeStatus,@Param("type_name")String typeName);
}
