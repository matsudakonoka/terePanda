package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfCurve;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfFaultPolyonFile;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfPoint;
import com.cnpc.epai.core.worktask.pojo.ByteOrderDataOutput;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author 张伟强
 * @Title: rfBaseStratumConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:基本断层多边形格式转换器
 * @date 2018.8.09
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
public class rfBaseFaultPolygonConvertor {
    public rfFaultPolyonFile file = new rfFaultPolyonFile();

    public boolean ReadFile(String fileName) {
        return false;
    }
    /**
     * 写断层多边形标准文件
     *
     * @param fileName 目标文件名称
     * @return float
     */
    public boolean WriteFile(String fileName) {
        //断层多边形类对象写标准文件
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
            put.writeInt(curveCount);
            for (int i = 0; i < curveCount; i++) {
                rfCurve curve = file.curveArray.get(i);
                put.writeLong(curve.curveNO);
                byte[] byBuffer = new byte[64];

                byte[] temp = curve.curveName.getBytes();
                Arrays.fill(byBuffer, (byte) 0);
                System.arraycopy(temp,0,byBuffer,0,temp.length);
                put.write(byBuffer);
                put.writeInt(curve.curvePointArray.size());
                int pointsize = curve.curvePointArray.size();
                for (int j = 0; j < pointsize; j++) {
                    rfPoint point = curve.curvePointArray.get(j);
                    put.writeLong(j+1);
                    put.writeDouble(point.x);
                    put.writeDouble(point.y);
                    put.writeFloat((float)point.z);
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
