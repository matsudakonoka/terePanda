package com.cnpc.epai.core.worktask.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AdjacentWellsReuseUtil {

    // 报告邻井复用:处理数据
    public static  JSONArray formatTableData(String authMasterDataType, Map workObjects, JSONArray jsonArray) {
        String objectId=workObjects.get("objectId")==null?"":workObjects.get("objectId").toString();
        String objectName=workObjects.get("objectName")==null?"":workObjects.get("objectName").toString();
        String objectType=workObjects.get("objectType")==null?"":workObjects.get("objectType").toString();
        String WELLBORE_ID=workObjects.get("WELLBORE_ID")==null?"":workObjects.get("WELLBORE_ID").toString();
        String WELLBORE_LABEL=workObjects.get("WELLBORE_LABEL")==null?"":workObjects.get("WELLBORE_LABEL").toString();
        String WELLBORE_COMMON_NAME=workObjects.get("WELLBORE_COMMON_NAME")==null?"":workObjects.get("WELLBORE_COMMON_NAME").toString();
        String WELLBORE_LEGAL_NAME=workObjects.get("WELLBORE_LEGAL_NAME")==null?"":workObjects.get("WELLBORE_LEGAL_NAME").toString();
        String WELL_LEGAL_NAME=workObjects.get("WELL_LEGAL_NAME")==null?"":workObjects.get("WELL_LEGAL_NAME").toString();

        for(Object obj : jsonArray) {
            JSONObject item = (JSONObject)obj;
            item.put("objectId",objectId);
            item.put("objectName",objectName);
            item.put("objectType",objectType);
            item.put("OBJECT_ID",objectId);
            item.put("OBJECT_NAME",objectName);
            switch (authMasterDataType) {//CD_WELLBORE、CD_WELL分支：是默认分支的重复
                case "CD_GEO_UNIT":
                    item.put("PROJECT_ID",objectId);
                    item.put("PROJECT_NAME",objectName);
                    break;
                case "CD_SITE":
                    item.put("SITE_ID",objectId);
                    item.put("SITE_NAME",objectName);
                    break;
                case "CD_COMPLETION":
                    item.put("COMPLETION_ID",objectId);
                    item.put("COMPLETION_NAME",objectName);
                    break;
                case "CD_MINING_CLAIM":
                    item.put("MINING_CLAIM_ID",objectId);
                    item.put("MINING_CLAIM_NAME",objectName);
                    break;
                default://CD_WELLBORE、CD_WELL分支：是默认分支的重复
                    if(StringUtils.isNotEmpty(WELLBORE_ID)){
                        item.put("WELLBORE_ID",WELLBORE_ID);
                        item.put("WELLBORE_LABEL",WELLBORE_LABEL);
                        item.put("WELLBORE_COMMON_NAME",WELLBORE_COMMON_NAME);
                        item.put("WELLBORE_LEGAL_NAME",WELLBORE_LEGAL_NAME);
                        item.put("WELL_LEGAL_NAME",WELL_LEGAL_NAME);
                        item.put("WELL_ID",objectId);
                        item.put("WELL_COMMON_NAME",objectName);
                        item.put("WELL_NAME",objectName);
                        item.put("WELL_ID_REL_NAME",objectName);
                    }
                    break;
            }
        }
        return jsonArray;
    }
    // 报告邻井复用:查询井筒信息
    public static Map getWellBore(String ServerAddr,JSONObject object,String authMasterDataType) throws Exception{
        String objectId = object.get("objectId")==null?"":object.get("objectId").toString();
        String objectName = object.get("objectName")==null?"":object.get("objectName").toString();
        String objectType = object.get("objectType")==null?"":object.get("objectType").toString();
        String token = HeaderTool.getToken();
        String ss = "{ \"subFilter\": [" +
                "{\"key\": \"WELL_ID\",\"logic\": \"AND\",\"realValue\": [\"" + objectId + "\"],\"symbol\": \"=\"}" +
                "] }";
        String url = "http://" + ServerAddr + "/sys/masterdata/masterdata/"+authMasterDataType+"/getpagedata?page=0&size=9999";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build();
        MediaType json = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json,ss);
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization",token)
                .url(url)
                .build();
        Date date=new Date();
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String id=df.format(date);
        System.out.println("----["+id+"]查询井筒信息开始请求接口---------");
        System.out.println("----["+id+"]url:"+url);
        System.out.println("----["+id+"]body:"+ss);
        System.out.println("----["+id+"]Authorization:"+token);
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        //System.out.println("----["+id+"]接口返回:"+s);
        Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(saveMap.get("content")));
        if (jsonArray!=null){
            for(Object obj : jsonArray){
                JSONObject map=(JSONObject) obj;
                if(StringUtils.equals((CharSequence) map.get("WELLBORE_NO"),"00") || jsonArray.size()==1){
                    String WELLBORE_ID=map.get("WELLBORE_ID")==null?"":map.get("WELLBORE_ID").toString();
                    String WELLBORE_LABEL=map.get("WELLBORE_LABEL")==null?"":map.get("WELLBORE_LABEL").toString();
                    String WELLBORE_COMMON_NAME=map.get("WELLBORE_COMMON_NAME")==null?"":map.get("WELLBORE_COMMON_NAME").toString();
                    String WELLBORE_LEGAL_NAME=map.get("WELLBORE_LEGAL_NAME")==null?"":map.get("WELLBORE_LEGAL_NAME").toString();
                    String WELL_LEGAL_NAME=map.get("WELL_LEGAL_NAME")==null?"":map.get("WELL_LEGAL_NAME").toString();
                    WELLBORE_LEGAL_NAME=StringUtils.equals(WELLBORE_LEGAL_NAME,"")?WELL_LEGAL_NAME:WELLBORE_LEGAL_NAME;
                    WELL_LEGAL_NAME=StringUtils.equals(WELL_LEGAL_NAME,"")?WELLBORE_LEGAL_NAME:WELL_LEGAL_NAME;
                    WELLBORE_LABEL=StringUtils.equals(WELLBORE_LABEL,"")?WELL_LEGAL_NAME:WELLBORE_LABEL;

                    object.put("WELL_LEGAL_NAME",WELL_LEGAL_NAME);
                    object.put("WELLBORE_ID",WELLBORE_ID);
                    object.put("WELLBORE_LABEL",WELLBORE_LABEL);
                    object.put("WELLBORE_COMMON_NAME",WELLBORE_COMMON_NAME);
                    object.put("WELLBORE_LEGAL_NAME",WELLBORE_LEGAL_NAME);
                    object.put("WELL_ID",objectId);
                    object.put("WELL_COMMON_NAME",objectName);
                    object.put("WELL_NAME",objectName);
                    object.put("WELL_ID_REL_NAME",objectName);
                }
            }
        }
        object.put("objectId",objectId);
        object.put("objectName",objectName);
        object.put("objectType",objectType);
        return object;
    }

    // 报告邻井复用:获取节点资源
    public static List<Map<String,Object>> nodeResources(String ServerAddr,String [] sourceNodeIds) throws Exception{
        String token = HeaderTool.getToken();
        Map<String,Object> map=new HashMap<>();
        //遍历id去查询节点资源匹配当前工具
        String ids = StringUtils.join(sourceNodeIds,",");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build();
        MediaType json = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = new FormBody.Builder().add("treeNodeIds", ids).build();
        Request request = new Request.Builder()
                .post(requestBody)
                .header("Authorization",token)
                .url("http://" + ServerAddr + "/core/objdataset/minBizUnit/getByTreeNodeIds")
                .build();
        Date date=new Date();
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String id=df.format(date);
        System.out.println("----["+id+"]获取节点资源开始请求接口---------");
        System.out.println("----["+id+"]url:"+request.url());
        System.out.println("----["+id+"]body:"+ids);
        System.out.println("----["+id+"]Authorization:"+token);
        Call callTask = client.newCall(request);
        Response response = callTask.execute();
        ResponseBody body = response.body();
        String s= body.string();
        //System.out.println("----["+id+"]接口返回:"+s);
        Map<String,Object> saveMap = (Map<String, Object>) com.alibaba.fastjson.JSON.parse(s);
        List<Map<String,Object>> toolList = (List<Map<String,Object>>)saveMap.get("result");
        return toolList;
    }
    // 报告邻井复用:T2树节点转化为T3树节点
    public static String t2IdToT3Id(String oldId,String workid) {
        // console.log("uuid().length=",this.uuid().length,"---oldId.length==",oldId.length,"workid.length==","1562031177985314817".length)
        // 处理逻辑为newid=oldId_workid[后7位]；其中oldId:24位；workid：19位；
        int leng=(oldId.length()+1)-32;
        String workid0=workid.substring(workid.length()+leng);
        return oldId+"_"+workid0;
    }
    // 判断数组在字符串内
    public static boolean arrayInString(String str,String[] arr) {
        boolean flag = true;
        for (String aa : arr) {
            if (!str.contains(aa)) {
                //System.out.println("字符串不包含：" + aa);
                flag = false;
                break;
            }
        }
//        if (flag) {
//            System.out.println("数组在字符串内。");
//        } else {
//            System.out.println("数组不在字符串内。");
//        }
        return flag;
    }

}
