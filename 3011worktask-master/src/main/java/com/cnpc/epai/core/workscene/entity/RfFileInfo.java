package com.cnpc.epai.core.workscene.entity;

import lombok.Data;

/**
 * @ClassName: RfFileInfo
 * @Description: 瑞飞文件格式
 * @Author
 * @Date 2022/10/25
 * @Version 1.0
 */
@Data
public class RfFileInfo {
    public String fileName = ""; //文件名称
    public String fileTitle = ""; //文件标题
    public String extension = ""; //扩展名
    public  int fileSize = 0;     //文件大小
    public String dirID="";       // 文件目录ID
    public Boolean isConverted = false; //是否已经转换
    public Boolean isNeedConvert = false; //是否需要转换 ---只有采集类需要转换
    public String checksum="";   //文件md5值
    public String dataRegion = ""; //文件区域
}
