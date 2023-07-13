package com.cnpc.epai.core.workscene.commom;

public class StatusCode {
    //正常情况
    public static final String NORMAL = "200";
    //异常情况 : 增删改查异常
    public static final String ADD_FAILED = "A001";
    public static final String DELETE_FAILED = "A002";
    public static final String QUERY_FAILED  = "A003";
    public static final String UPDATE_FAILED = "A004";
    //异常情况 ： 添加重复关键字
    public static final String ADD_REPEATED = "B001";
}
