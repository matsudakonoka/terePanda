package com.cnpc.epai.core.workscene.commom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> {

    private String status;

    private String message;

    private T body;

    public Result successResult(T body){
        Result r = new Result();
        r.setMessage("操作成功");
        r.setStatus("200");
        r.setBody(body);
        return r;
    }

    public Result failureResult(T body){
        Result r = new Result();
        r.setMessage("操作失败");
        r.setStatus("400");
        r.setBody(body);
        return r;
    }

    public Result successResult(){
        Result r = new Result();
        r.setMessage("操作成功");
        r.setStatus("200");
        return r;
    }

    public Result failureResult(){
        Result r = new Result();
        r.setMessage("操作失败");
        r.setStatus("400");
        return r;
    }
}
