package com.cnpc.epai.core.workscene.pojo;


import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfCurve;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author 张伟强
 * @Title: rfBaseStratumConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:OpenWorks断层多边形格式转换器
 * @date 2018.8.09
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfOpenWorksFaultPolygonConvertor extends rfBaseFaultPolygonConvertor {
    /**
     * 读取OpenWorks的断层多边形文件
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
            while((strTextLine = br.readLine())!=null){
                strTextLine = strTextLine.trim();

                String[] splits = strTextLine.split("\\s+");
                if(splits.length<4) {
                    continue;
                }
                rfPoint point = new rfPoint();
                point.x = Double.parseDouble(splits[0]);
                point.y = Double.parseDouble(splits[1]);
                point.z = 0;
                int nFlag = Integer.parseInt(splits[2]);
                switch (nFlag ) {
                    case 6:
                        curve = new rfCurve();
                        curve.curveNO = ncurveIndex;
                        ncurveIndex++;
                        curve.curveName = splits[3];
                        curve.curvePointArray.add(point);
                        break;
                    case 7:
                        curve.curvePointArray.add(point);
                        break;
                    case 8:
                        curve.curvePointArray.add(point);
                        file.curveArray.add(curve);
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
