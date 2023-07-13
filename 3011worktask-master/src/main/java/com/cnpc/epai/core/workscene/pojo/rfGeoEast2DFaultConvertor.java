package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rf2DFaultPoint;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFault;
import com.cnpc.epai.core.worktask.pojo.ByteOrderDataOutput;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @Title: rfGeoEastFaultConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:GeoEastFault2D断层转换器
 * @author 张伟强
 * @date 2018.11.21
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfGeoEast2DFaultConvertor extends rfBaseFaultConvertor {
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
     * 读取GeoFault的2D断层文件
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
            String oldName="";
            String Name ="";
            int connectFlag = -1;
            String color ="";
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
                rf2DFaultPoint point = new rf2DFaultPoint();
                point.NO = nIndex;
                nIndex++;
                point.inLineName = splits[1];
                point.trace = Integer.parseInt(splits[2]);
                point.x = Double.parseDouble(splits[3]);
                point.y = Double.parseDouble(splits[4]);
                point.z = Float.parseFloat(splits[5]);
                Name = splits[0];
                int  newFlag = Integer.parseInt(splits[6]);
                int nFlag =2;
                if(newFlag != connectFlag)
                {
                    nFlag =1;
                    connectFlag = newFlag;
                }
                //String flag = Name.substring(0,1);
                //int nFlag = Integer.parseInt(flag);
                switch (nFlag ) {
                    case 1:
                        fault = new rfFault();
                        fault.Name = Name;
                        color = splits[8].substring(1);
                        fault.color = hexToDecimal(color);
                        fault.pointArray2D.add(point);
                        file.curveArray.add(fault);
                        break;
                    case 2:
                        fault.pointArray2D.add(point);
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
    /**
     * 写断层标准文件
     *
     * @param fileName 目标文件名称
     * @return float
     */
    public boolean WriteFile(String fileName) {
        //断层类对象写标准文件
        File f = new File(fileName);
        OutputStream baos = null;
        DataOutputStream dos = null;
        try {
            baos = new FileOutputStream(f);
            dos = new DataOutputStream(baos);
            ByteOrderDataOutput put = new ByteOrderDataOutput(dos, ByteOrder.LITTLE_ENDIAN);
            put.write(file.version.getBytes());
            put.writeInt(file.lengFlag);
            int curveCount = file.curveArray.size();
            put.writeLong(curveCount);
            byte[] byBuffer = new byte[64];
            byte[] inlineName =new byte[32];
            byte[] temp = null;
            for (int i = 0; i < curveCount; i++) {
                rfFault fault = file.curveArray.get(i);
                put.writeLong(fault.NO);
                temp = fault.Name.getBytes();
                Arrays.fill(byBuffer, (byte) 0);
                System.arraycopy(temp,0,byBuffer,0,temp.length);
                put.write(byBuffer);
                put.writeInt(fault.color);
                put.writeLong(fault.pointArray2D.size());
                int pointsize = fault.pointArray2D.size();
                for (int j = 0; j < pointsize; j++) {
                    rf2DFaultPoint point = fault.pointArray2D.get(j);

                    put.writeLong(point.NO);
                    temp = point.inLineName.getBytes();
                    Arrays.fill(inlineName, (byte) 0);
                    System.arraycopy(temp,0,inlineName,0,temp.length);
                    put.write(inlineName);
                    put.writeLong(point.trace);
                    put.writeDouble(point.x);
                    put.writeDouble(point.y);
                    put.writeFloat(point.z);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
