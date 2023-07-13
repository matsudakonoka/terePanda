package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFault;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFaultPoint;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfGeoEastFaultConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:GeoEastFault断层转换器
 * @author 张伟强
 * @date 2018.8.20
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfGeoEastFaultConvertor  extends rfBaseFaultConvertor {
    // 将一个十六进制的字符串转换为十进制数
    private  int hexToDecimal(String hexStr) {
        int decimalValue = 0;
        for (int i = 0; i < hexStr.length(); i++) {
            // 将字符串转换为一组字符
            char hexChar = hexStr.charAt(i);
            // 计算
            decimalValue = decimalValue * 16 + hexCharToDecimal(hexChar);
        }
        return decimalValue;
    }

    // 将十六进制字符转换为十进制数
    private  int hexCharToDecimal(char ch) {
        if (ch >= 'A' && ch <= 'F') {
            return 10 + ch - 'A';
        } else {
            return ch - '0';
        }

    }
    /**
     * 读取GeoFault的断层文件
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
            String Name ="";
            int nFlag = 2;
            String color ="";
            int beforeConnectionFlag = -1;
            while((strTextLine = br.readLine())!=null){
                strTextLine = strTextLine.trim();
                if(strTextLine.equals("111"))
                    continue;
                if(strTextLine.contains("#faultName"))
                    continue;
                String[] splits = strTextLine.split("\\s+");
                if(splits.length<9) {
                    continue;
                }
                rfFaultPoint point = new rfFaultPoint();
                point.NO = nIndex;
                nIndex++;
                point.inLine = Math.round(Double.parseDouble(splits[1]));
                point.CmpNo = Math.round(Double.parseDouble(splits[2]));
                point.x = Double.parseDouble(splits[3]);
                point.y = Double.parseDouble(splits[4]);
                point.z = Float.parseFloat(splits[5]);
                int connectionFlag = Integer.parseInt(splits[6]);
                Name = splits[0];
                nFlag = 2;
                if(connectionFlag != beforeConnectionFlag)
                {
                    beforeConnectionFlag = connectionFlag;
                    nFlag =1;
                }
                switch (nFlag ) {
                    case 1:
                        fault = new rfFault();
                        fault.Name = Name;
                        color = splits[8].substring(1);
                        fault.color = hexToDecimal(color);
                        fault.pointArray.add(point);
                        file.curveArray.add(fault);
                        break;
                    case 2:
                        fault.pointArray.add(point);
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
