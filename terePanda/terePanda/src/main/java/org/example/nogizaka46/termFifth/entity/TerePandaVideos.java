package org.example.nogizaka46.termFifth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * video
 * @author 
 */
@Data
@TableName(value = "video")
@NoArgsConstructor
public class TerePandaVideos implements Serializable {
    @TableId(value = "id",type = IdType.INPUT)
    private String id;

    private String name;

    private String url;

    private Date time;

    private String members;

    private int groups;

    private String headerimage;

    private static final long serialVersionUID = 1L;
}