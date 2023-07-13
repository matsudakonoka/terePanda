package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumFile;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumPoint;
import com.cnpc.epai.core.worktask.pojo.ByteOrderDataOutput;

import java.io.*;
import java.nio.ByteOrder;

/**
 * @author 张伟强
 * @Title: rfBaseStratumConvertor.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:基本解释层位格式转换器
 * @date 2018.8.07
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
public class rfBaseStratumConvertor {
    public rfStratumFile stratumFile = new rfStratumFile();

    public boolean ReadFile(String fileName) {
        return false;
    }

    ;

    public boolean WriteFile(String fileName) {
        //层位类对象写标准文件
        File f = new File(fileName);
        OutputStream baos = null;
        DataOutputStream dos = null;
        try {
            baos = new FileOutputStream(f);
            dos = new DataOutputStream(baos);
            ByteOrderDataOutput put = new ByteOrderDataOutput(dos, ByteOrder.LITTLE_ENDIAN);
            put.write(stratumFile.version.getBytes());
            put.writeInt(stratumFile.lengFlag);
            put.writeLong(stratumFile.minInLineNo);
            put.writeLong(stratumFile.maxInLineNo);
            put.writeLong(stratumFile.stepInLineNo);
            put.writeLong(stratumFile.minTraceID);
            put.writeLong(stratumFile.maxTraceID);
            put.writeLong(stratumFile.stepTraceID);
            int ncount = stratumFile.pointArray.size();
            for (int i = 0; i < ncount; i++) {
            rfStratumPoint pt = stratumFile.pointArray.get(i);
                put.writeLong(pt.NO);
                put.writeLong(pt.inLine);
                put.writeLong(pt.traceNO);
                put.writeDouble(pt.x);
                put.writeDouble(pt.y);
                put.writeFloat(pt.z);
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
