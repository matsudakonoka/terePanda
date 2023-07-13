package com.cnpc.epai.core.workscene.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName: SourceSatellite
 * @Description: 源卫星端
 * @Author
 * @Date 2022/8/20
 * @Version 1.0
 */
@Data
public class SourceSatellite    {
    /*
     * 卫星端是否可用
     */
    private boolean enable;

    /**
     * 客户端ID
     */
    private String id;
    /**
     * 客户端名称
     */
    private String satelliteName;
    /**
     * 客户端服务发布地址
     */
    private String serviceUrl;
    /*
     * 主机地址
     */
    private String hostIP;
    /*
     * 数据来源（软件、系统名称）
     */
    private String source;
    /*
     * 服务端口
     */
    private int port;
    /*
     * 最后活跃时间
     */
    private Date lastActive;
    /*
     * 油田标识
     */
    private String dataRegion;

}

