package org.example.nogizaka46.termFifth.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultApi implements Serializable {
    private String code;
    private String msg;
    private boolean flag = true;
    private Object result;

    public static ResultApi newInstance() {
        return new ResultApi();
    }
    public static ResultApi ofSuccessResult(Object result) {
        return ofSuccessResultMsg(result,"操作成功！");
    }
    public static ResultApi ofSuccessResultMsg(Object result , String msg, Object... args) {
        ResultApi apiResult = newInstance();
        apiResult.setFlag(true);
        apiResult.setResult(result);
        apiResult.setMsg(String.format(msg, args));
        return apiResult;
    }

    public static ResultApi ofFailureResultMsg(Object result, String msg, Object... args) {
        ResultApi apiResult = newInstance();
        apiResult.setFlag(false);
        apiResult.setResult(result);
        apiResult.setMsg(String.format(msg, args));
        return apiResult;
    }

    public static ResultApi ofFailureResult(Object result) {
        return ofFailureResultMsg(result, "操作失败！");
    }
}
