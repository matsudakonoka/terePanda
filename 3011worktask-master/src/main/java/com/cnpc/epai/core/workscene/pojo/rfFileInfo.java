package com.cnpc.epai.core.workscene.pojo;
/**
 * @Title: rfFileInfo.java
 * @Package com.cnpc.epai.common.welllog.service;
 * @Description: 文件信息文件
 * @author 张伟强
 * @date 2018.8.31
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
/**
 * @Title rfFileInfo
 * @author 张伟强
 * @version 1.0.0
 * @Description: 文件信息类
 */
public class rfFileInfo {
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

