package com.cnpc.epai.core.workscene.pojo;

import com.cnpc.epai.core.workscene.pojo.rfProfessionGraph.rf2DStratumPoint;
import com.cnpc.epai.core.worktask.pojo.ByteOrderDataOutput;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

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
public class rfOpenWorks2DPureDataStratumConvertor extends rfBaseStratumConvertor{
    public boolean ReadFile(String fileName) {
        boolean ret = false;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strTextLine="";
            int i=0;
            strTextLine = br.readLine().trim();
            while(strTextLine.isEmpty() ==false){

                rf2DStratumPoint point = new rf2DStratumPoint();
                String[] splits = strTextLine.split("\\s+");
                point.NO = i+1;
                try {
                    point.inLineName = splits[0];
                    //point.traceNO = Integer.parseInt(splits[1]);
                    point.traceNO = (int)Math.floor(Float.parseFloat(splits[1])+0.5);
                    point.x = Double.parseDouble(splits[2]);
                    point.y = Double.parseDouble(splits[3]);
                    point.z = Float.parseFloat(splits[4]);
                }
                catch (Exception ex){
                    strTextLine = br.readLine().trim();
                    continue;
                }

                stratumFile.pointArray2D.add(point);
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
            byte[] inlineName =new byte[32];
            //put.writeLong(stratumFile.minInLineNo);
            //put.writeLong(stratumFile.maxInLineNo);
            //put.writeLong(stratumFile.stepInLineNo);
            //put.writeLong(stratumFile.minTraceID);
            //put.writeLong(stratumFile.maxTraceID);
            //put.writeLong(stratumFile.stepTraceID);
            int ncount = stratumFile.pointArray2D.size();
            byte[] temp = null;
            for (int i = 0; i < ncount; i++) {
                rf2DStratumPoint pt = stratumFile.pointArray2D.get(i);
                put.writeLong(pt.NO);
                temp = pt.inLineName.getBytes();
                Arrays.fill(inlineName, (byte) 0);
                System.arraycopy(temp,0,inlineName,0,temp.length);
                put.write(inlineName);
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
