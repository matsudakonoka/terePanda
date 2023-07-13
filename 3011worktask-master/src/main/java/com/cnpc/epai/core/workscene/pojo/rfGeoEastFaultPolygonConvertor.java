package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfCurve;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfGeoEastFaultPolygonConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:GeoEast断层多边形格式转换器
 * @author 张伟强
 * @date 2018.8.01
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfGeoEastFaultPolygonConvertor extends rfBaseFaultPolygonConvertor {
    /**
     * 读取GeoEast的断层多边形文件
     *
     * @param fileName 文件名称
     * @return   读取是否成功
     */
    public boolean ReadFile(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strTextLine="";
            int i=0;
            rfCurve curve = null;
            int nIndex =1;
            int ncurveIndex =1;
            String Name="";
            int nFlag =2;
            String oldName="";
            while((strTextLine = br.readLine())!=null){
                strTextLine = strTextLine.trim();
                if(strTextLine.contains("#faultPolygonName"))
                    continue;
                String[] splits = strTextLine.split("\\s+");
                if(splits.length<7) {
                    continue;
                }
                rfPoint point = new rfPoint();
                point.x = Double.parseDouble(splits[1]);
                point.y = Double.parseDouble(splits[2]);
                point.z = 0;
                Name = splits[0];
                nFlag = 2;
                if(oldName.equals(Name) == false)
                {
                    nFlag =1;
                    oldName = Name;
                }
                switch (nFlag ) {
                    case 1:
                        curve = new rfCurve();
                        curve.curveName = Name;
                        curve.curveNO  =ncurveIndex;
                        ncurveIndex++;
                        curve.curvePointArray.add(point);
                        file.curveArray.add(curve);
                        break;
                    case 2:
                        curve.curvePointArray.add(point);
                        break;
                    default:
                        break;
                }
            }
             br.close();
        } catch (IOException e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
