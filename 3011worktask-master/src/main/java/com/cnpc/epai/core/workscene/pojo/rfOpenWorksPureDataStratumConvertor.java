package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfOpenWorksPureDataStratumConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:openworks纯解释层位格式转换器
 * @author 张伟强
 * @date 2018.8.01
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfOpenWorksPureDataStratumConvertor  extends rfBaseStratumConvertor {
    public boolean ReadFile(String fileName) {
        boolean ret = false;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strTextLine="";
            int i=0;
            strTextLine = br.readLine().trim();
            while(strTextLine.isEmpty() ==false){

                rfStratumPoint point = new rfStratumPoint();
                String[] splits = strTextLine.split("\\s+");
                point.NO = i+1;
                try {
                    point.inLine = Math.round(Double.parseDouble(splits[0]));
                    point.traceNO = Math.round(Double.parseDouble(splits[1]));
                    point.x = Double.parseDouble(splits[2]);
                    point.y = Double.parseDouble(splits[3]);
                    point.z = Float.parseFloat(splits[4]);
                }
                catch (Exception ex){
                    strTextLine = br.readLine().trim();
                    continue;
                }

                stratumFile.pointArray.add(point);
                i++;
                strTextLine = br.readLine().trim();
                ret = true;

            };
            br.close();
            return ret;
        } catch (IOException e) {
            log.info(e.getMessage());
            e.printStackTrace();
            ret =  false;

        }
        finally {
            return ret;
        }

    }
}
