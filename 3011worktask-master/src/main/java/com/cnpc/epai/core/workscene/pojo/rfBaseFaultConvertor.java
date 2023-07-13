package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFault;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFaultFile;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFaultPoint;
import com.cnpc.epai.core.worktask.pojo.ByteOrderDataOutput;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author 张伟强
 * @Title: rfBaseFaultConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:基本断层格式转换器
 * @date 2018.8.09
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
public class rfBaseFaultConvertor {
    public rfFaultFile file = new rfFaultFile();

    public boolean ReadFile(String fileName) {
        return false;
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
            for (int i = 0; i < curveCount; i++) {
                rfFault fault = file.curveArray.get(i);
                put.writeLong(fault.NO);
                byte[] byBuffer = new byte[64];

                byte[] temp = fault.Name.getBytes();
                Arrays.fill(byBuffer, (byte) 0);
                System.arraycopy(temp,0,byBuffer,0,temp.length);
                put.write(byBuffer);
                put.writeInt(fault.color);
                put.writeLong(fault.pointArray.size());
                int pointsize = fault.pointArray.size();
                for (int j = 0; j < pointsize; j++) {
                    rfFaultPoint point = fault.pointArray.get(j);

                    put.writeLong(point.NO);
                    put.writeLong(point.inLine);
                    put.writeLong(point.CmpNo);
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
