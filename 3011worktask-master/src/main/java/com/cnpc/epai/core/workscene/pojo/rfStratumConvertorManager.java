package com.cnpc.epai.core.workscene.pojo;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Title: rfStratumConvertorManager.java
 * @Package com.cnpc.epai.common.seismicinterpretation.service;
 * @Description:获取层位转换适配器
 * @author 张伟强
 * @date 2018.8.01
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */
@Slf4j
public class rfStratumConvertorManager {
    /**
     * 获取层位转换适配器
     *
     * @param fileName 文件名称
     * @return   读取是否成功
     */
    public static rfBaseStratumConvertor GetStratumConvertor(String fileName){
        rfBaseStratumConvertor convertor = null;
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String strTextLine="";

            strTextLine = br.readLine().trim();
            if(strTextLine.equals("@File_Version: 4"))
            {
                convertor = new rfOpenWorksStratumConvertor();
            }
            else {
                String[] splits = strTextLine.split("\\s+");
                if (splits.length == 5) {
                    convertor = new rfOpenWorksPureDataStratumConvertor();
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
