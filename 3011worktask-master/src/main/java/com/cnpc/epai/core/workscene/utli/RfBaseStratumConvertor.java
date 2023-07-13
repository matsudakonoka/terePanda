package com.cnpc.epai.core.workscene.utli;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumFile;
import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rfStratumPoint;

import java.io.*;
import java.nio.ByteOrder;

/**
 * @ClassName: RfBaseStratumConvertor
 * @Description:
 * @Author
 * @Date 2022/10/25
 * @Version 1.0
 */
public class RfBaseStratumConvertor {
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
