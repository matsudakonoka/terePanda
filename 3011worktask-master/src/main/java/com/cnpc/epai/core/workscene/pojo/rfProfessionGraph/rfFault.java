package com.cnpc.epai.core.workscene.pojo.rfProfessionGraph;

import java.util.ArrayList;

/**
 * @Title rfFault
 * @author
 * @version 1.0.0
 * @Description: 瑞飞断层格式
 */
public class rfFault {
    public long NO=0;       //顺序编号
    public String Name="";  //断层名称
    public int color =0;    //颜色
    public ArrayList<rfFaultPoint> pointArray = new ArrayList<rfFaultPoint>();
    public ArrayList<rf2DFaultPoint> pointArray2D = new ArrayList<rf2DFaultPoint>();
};