package com.cnpc.epai.core.workscene.pojo;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfFaultPolygonConvertorManager.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:断层多边形转换适配器管理
 * @author 张伟强
 * @date 2018.8.21
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfFaultPolygonConvertorManager {
    /**
     * 获取断层多边形转换适配器
     *
     * @param fileName 文件名称
     * @return   读取是否成功
     */
    static rfBaseFaultPolygonConvertor GetFaultPolygonConvertor(String fileName){
        rfBaseFaultPolygonConvertor convertor = null;
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String strTextLine="";

            strTextLine = br.readLine().trim();
            if(strTextLine.contains("#faultPolygonName"))
            {
                convertor = new rfGeoEastFaultPolygonConvertor();
            }
            else {
                String[] splits = strTextLine.split("\\s+");
                if (splits.length == 4) {
                    convertor = new rfOpenWorksFaultPolygonConvertor();
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
