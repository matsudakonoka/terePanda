package com.cnpc.epai.core.workscene.pojo.rfProfessionGraph;

import java.util.ArrayList;

/**
 * @Title rfFaultPolygonFile
 * @author
 * @version 1.0.0
 * @Description: 瑞飞断层多边形文件格式
 */
public class rfFaultPolyonFile{
    public String version = "v1.0"; //四个字节版本信息
    //基本属性
    public int lengFlag = 8 ;       //长度类型标志
    //扩展属性
    //数据特点
    public ArrayList<rfCurve> curveArray = new ArrayList<rfCurve>();

}