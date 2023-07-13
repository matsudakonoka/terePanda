package com.cnpc.epai.core.workscene.pojo;

//import com.sun.tools.javac.util.Convert;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfOpenWorksConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:OpenWorks解释层位格式转换器
 * @author 张伟强
 * @date 2018.8.01
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfOpenWorksStratumConvertor extends rfBaseStratumConvertor{
    public boolean ReadFile(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strTextLine="";
            //while ((strTextLine = br.readLine()) != null) {
               ReadFileHeader(br);
               int i=0;
            strTextLine = br.readLine().trim();
            while (strTextLine.equals("EOD") == false) {

                rfStratumPoint point = new rfStratumPoint();
                String[] splits = strTextLine.split("\\s+");
                point.NO = i + 1;
                try {
                    point.inLine = Math.round(Double.parseDouble(splits[0]));
                    point.traceNO = Math.round(Double.parseDouble(splits[1]));
                    point.x = Double.parseDouble(splits[2]);
                    point.y = Double.parseDouble(splits[3]);
                    point.z = Float.parseFloat(splits[4]);
                } catch (Exception ex) {
                    strTextLine = br.readLine().trim();
                    continue;
                }

                stratumFile.pointArray.add(point);
                i++;
                strTextLine = br.readLine().trim();
            }
            br.close();
        } catch (IOException e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    private  Boolean ReadFileHeader(BufferedReader br)
    {
        boolean ret = true;
        String strTextLine = null;
        try {
            strTextLine = br.readLine();
            while(strTextLine.equals( "#End_of_Horizon_ASCII_Header_") ==false){
                if(strTextLine.contains("#Horizon_name"))
                {
                    String[] splits = strTextLine.split(" ");
                    stratumFile.Name = splits[1];
                }
                if(strTextLine.contains("Horizon_color_is"))
                {
                    String[] splits = strTextLine.split(" ");
                    if(splits.length>4){
                        int b = Integer.parseInt(splits[1]);
                        int g = Integer.parseInt(splits[2]);
                        int r = Integer.parseInt(splits[3]);
                        int color = r+g*256+b*256*256;
                        stratumFile.lineColor = String.valueOf(color);
                    }
                }
                strTextLine = br.readLine().trim();;
            };
        }catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
            ret = false;
        }
        return ret;

    }
}
