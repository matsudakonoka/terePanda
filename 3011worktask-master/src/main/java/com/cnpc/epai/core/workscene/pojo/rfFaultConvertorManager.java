package com.cnpc.epai.core.workscene.pojo;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfFaultConvertorManager.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:断层适配器管理器
 * @author 张伟强
 * @date 2018.8.21
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfFaultConvertorManager {
    /**
     * 获取断层转换适配器
     *
     * @param fileName 文件名称
     * @return   读取是否成功
     */
    static rfBaseFaultConvertor GetFaultConvertor(String fileName){
        rfBaseFaultConvertor convertor = null;
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String strTextLine="";

            strTextLine = br.readLine().trim();
            if(strTextLine.equals("111")||strTextLine.equals("#faultName"))
            {
                convertor = new rfGeoEastFaultConvertor();
            }
            else {
                String[] splits = strTextLine.split("\\s+");
                //标准的断层14列
                if (splits.length == 14) {
                    convertor = new rfOpenWorksFaultConvertor();
                }
            }
            br.close();
        } catch (IOException e) {
            log.info(e.getMessage());
            e.printStackTrace();

        }
        finally {
        }
        return convertor;
    }
}

