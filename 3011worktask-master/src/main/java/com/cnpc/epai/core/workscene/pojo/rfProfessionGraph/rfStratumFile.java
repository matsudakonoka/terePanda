package com.cnpc.epai.core.workscene.pojo.rfProfessionGraph;

import java.util.ArrayList;

/**
 * @Title rfStratumFile
 * @author
 * @version 1.0.0
 * @Description: 瑞飞层位文件格式
 */
public class rfStratumFile{
    public String version = "v1.0"; //四个字节版本信息
    public boolean is3D= true; //is3D为真，3D断层，否则，二维断层
    //基本属性
    public int lengFlag = 8 ;       //长度类型标志
    public long  minInLineNo=0;    //最小线号
    public long  maxInLineNo = 0;  //最大线号
    public long  stepInLineNo = 1; //线号间隔
    public long  minTraceID = 0;   //最小道号
    public long  maxTraceID = 0;   //最大道号
    public long  stepTraceID =1;   //道号间隔
    //扩展属性
    public double left = 0;
    public double top = 0;
    public double right = 0;
    public double bottom = 0;
    public String Name = "";
    public String verticalDomain="";
    public String verticalDomainUnit="";
    public String lineColor="";
    public String lineType="";
    //数据特点
    public ArrayList<rfStratumPoint> pointArray = new ArrayList<rfStratumPoint>();
    //数据特点
    public ArrayList<rf2DStratumPoint> pointArray2D = new ArrayList<rf2DStratumPoint>();
}
