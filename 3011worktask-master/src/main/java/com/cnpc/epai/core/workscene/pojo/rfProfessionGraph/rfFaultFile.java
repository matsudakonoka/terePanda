package com.cnpc.epai.core.workscene.pojo.rfProfessionGraph;

import java.util.ArrayList;

/**

import java.util.ArrayList;* @Title rfFaultFile
        * @author
 * @version 1.0.0
        * @Description: 瑞飞断层文件格式
        */
public class rfFaultFile{
    public String version = "v1.0"; //四个字节版本信息
    public boolean is3D= true; //is3D为真，3D断层，否则，二维断层
    //基本属性
    public int lengFlag = 8 ;       //长度类型标志
    //扩展属性
    //数据特点
    public ArrayList<rfFault> curveArray = new ArrayList<rfFault>();

}