package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFault;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFaultPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfOpenWorksConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:OpenWorks断层格式转换器
 * @author 张伟强
 * @date 2018.8.01
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfOpenWorksFaultConvertor extends rfBaseFaultConvertor{
    /**
     * 读取OpenWorks的断层文件
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
            rfFault fault = null;
            int nIndex =1;
            while((strTextLine = br.readLine())!=null){
                strTextLine = strTextLine.trim();

                String[] splits = strTextLine.split("\\s+");
                if(splits.length<14) {
                    continue;
                }
                rfFaultPoint point = new rfFaultPoint();
                point.NO = nIndex;
                nIndex++;
                point.x = Double.parseDouble(splits[0]);
                point.y = Double.parseDouble(splits[1]);
                point.z = Float.parseFloat(splits[2]);
                String Name = splits[7];
                String flag = Name.substring(0,1);
                int nFlag = Integer.parseInt(flag);
                switch (nFlag ) {
                    case 1:
                        fault = new rfFault();
                        fault.Name = Name.substring(1);
                        int b = Integer.parseInt(splits[3]);
                        int g = Integer.parseInt(splits[4]);
                        int r = Integer.parseInt(splits[5]);
                        fault.color = r+g*256+b*256*256;
                        fault.pointArray.add(point);
                        break;
                    case 2:
                        fault.pointArray.add(point);
                        break;
                    case 3:
                        fault.pointArray.add(point);
                        file.curveArray.add(fault);
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
