package com.cnpc.epai.core.worktask.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: fssTool
 * @Description:
 * @Author
 * @Date 2021/10/20
 * @Version 1.0
 */
@Component
public class fssTool {

    @Value("${epai.confighost.fss_host}")
    private String fssHosts;

    public static void main(String[] args) throws IOException {
        String ASIFSTreeId = "qvvjwJZXxKKztq0AA1dDbir1";//软件接口数据文件存储节点//qvvjwJZXxKKztq0AA1dDbir1     //CRP_ASI_FILE_CLASS_00002
        File file = new File("D:\\KLDA\\靶点模板.xlsx");
        String fileName = "靶点模板.xlsx";
        Map<String, Object> metaAttribute = new HashMap<String, Object>();
        metaAttribute.put("APP_DOMAIN", "CRP");
        metaAttribute.put("DATA_GROUP", "ACTIJD100001988");
        //String token = "Bearer  eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtZW5nbGluZ2ppYW4yMDA4IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrZ_ku6Tnrq0iLCJhdXRob3JpdGllcyI6WyJST0xFX0xFQURFUiJdLCJjbGllbnRfaWQiOiJ3ZWJhcHAiLCJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOCIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTiIsInNjb3BlIjpbIm9wZW5pZCJdLCJvcmdhbml6YXRpb24iOiJPUkdBSkQxMDAwMDAwMjMiLCJleHAiOjE2Mzc1NTQwMDcsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjM0OTYyMDA3NzUzLCJqdGkiOiI4N2E1MzRmMi0xMTYwLTRlZjUtYmRmNy0xYzMzMTc4MzVmMjQifQ.J3sdacTDrZ1HWflu3LbFjvIg7poZ8DQHKMuHq35Gs-kQiyrmT6us9FbkgK3B9KAdZx-xVMKQvar2AGbuQmebbaPIWyHvprmHHKeG3dYshE-oO9q4QAPmTA5nQUcGwA4APP-aDIRsAbrsRThqC66QMbsqku_iXCU8CeM3NLzwKr04uh5S0pULOhgJLpWW5AwwB-gja-0eOtmBr4lzAxrQ4DXTYpx9IXMJqnpVNcDIv8x0qU--v1s1u25bPUd42YMDxZjVEVsB-5IL-wL6bTb05HHInHK_-c8JHHqsXANibX0MqoHqSsRDFPeZsYD7blS0Lb48AGVRM2Gj_sKBIjGeLA";
        String token = "Bearer  eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtZW5nbGluZ2ppYW4yMDA4IiwiaXNzIjoiYTM2YzMwNDliMzYyNDlhM2M5Zjg4OTFjYjEyNzI0M2MiLCJkaXNwbGF5X25hbWUiOiLlrZ_ku6Tnrq0iLCJhdXRob3JpdGllcyI6WyJST0xFX0xFQURFUiJdLCJjbGllbnRfaWQiOiJ3ZWJhcHAiLCJsb2dpbl9uYW1lIjoibWVuZ2xpbmdqaWFuMjAwOCIsInVzZXJfaWQiOiJuOE9OM0FDQU5jVXBLc1ZCZUQxdWtuRGhTTjZqbmVWTiIsInNjb3BlIjpbIm9wZW5pZCJdLCJvcmdhbml6YXRpb24iOiJPUkdBSkQxMDAwMDAwMjMiLCJleHAiOjE2MzgwOTA4MDIsInJlZ2lvbiI6IkpEIiwiaWF0IjoxNjM1NDk4ODAyMTYwLCJqdGkiOiJlOTM0NjNhNC05MjZiLTQxNDEtYWMwOS1jYWQ5YmE0NTZiZmMifQ.jqmsOBOx-ZvNZRES8FBpgnaJ0QdbqwJoCVfGcQ0dtsKI8HhLm8CxR1u29qlvNPiouiC-XILfvCyqkwqO3HzmhcZFNkXu3mgPtJSSseCsH5EEM65J0atjr5tFDs383CiopAQIDkZ-8RIhzbsGP57KcKJFspw5hZTXzuvOlKltm2cg0uM2Xn45OEVUQkSPSWLqZClWsx3WIenEO_iIUKtQi3wBN60QNKgXf8D3TngLwnKa6vdYOEyR1KidvykhQL0oI1cerdvFFqzW5hPokr9mtbIF4IYCU0OZqMt8bEw3qkiEJVfng4Gj-cMj533bgH1jd-bHkhpWQlBbFg4d-PGNjQ";
        //String fileLogicModel = uploadFileToFss(fileName, file,null,"TL",ASIFSTreeId,metaAttribute,token);
        ObjectMapper mapper = new ObjectMapper();
        //String fileId = mapper.readTree(fileLogicModel).get("basic").get("fileId").asText();
       // System.out.println(fileId);
    }


    /**
     * 获取要上传文件的预签名url
     *
     * @param fssHost    FSS域名（例如: http://www.dev.pcep.cloud）
     * @param dataRegion 油田标识(例如: JD)
     * @param fileName   上传文件的名称(例如: abc.txt)
     * @param header      header
     * @return 文件的预签名url
     */
    private  JSONObject uploadStepOne(String fssHost, String dataRegion, String fileName, HttpHeaders header) {
        Assert.notNull(fssHost, "fssHost地址为null");
        Assert.notNull(dataRegion, "dataRegion为null");
        Assert.notNull(fileName, "上传文件名为null");
        Assert.notNull(header, "token为null");

        String url = String.format("%s/sys/file/s3/generatePresignedUploadUrl?dataRegion=%s&fileName=%s", fssHost, dataRegion, fileName);

        HttpEntity<HttpHeaders> requestHttpEntity = new HttpEntity<>(header);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject config = restTemplate.exchange(url, HttpMethod.GET, requestHttpEntity, JSONObject.class).getBody();

        System.out.println("预签名url: "+config);

        return config;

    }


    /**
     * 上传文件到服务器存储
     * <p>
     * 处理本地文件，运行代码和文件在同一个机器上
     *
     * @param preUrl      预签名url
     * @param httpFileUrl 上传的文件的url
     * @return 上传文件的状态，包括etag、uploadStatusCode
     * <p>
     * the uploadStatusCode code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     */
    private  Map<String, String> uploadStepTwo(String preUrl, String httpFileUrl) {
        Assert.notNull(preUrl, "预签名url为null");
        Assert.notNull(httpFileUrl, "上传文件为null");

        InputStream inputStream = null;
        try {
            URL fileNetUrl = new URL(httpFileUrl);//处理http开头的网络文件
            inputStream = fileNetUrl.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uploadStepTwo(preUrl, inputStream);
    }

    /**
     * 上传文件到服务器存储
     * <p>
     * 处理本地文件，运行代码和文件在同一个机器上
     *
     * @param preUrl 预签名url
     * @param file   上传的文件
     * @return 上传文件的状态，包括etag、uploadStatusCode
     * <p>
     * the uploadStatusCode code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     */
    private  Map<String, String> uploadStepTwo(String preUrl, File file) {
        Assert.notNull(preUrl, "预签名url为null");
        Assert.notNull(file, "上传文件为null");
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return uploadStepTwo(preUrl, fileInputStream);
    }

    /**
     * 上传文件到服务器存储
     *
     * @param preUrl          预签名url
     * @param fileInputStream 上传文件的IO流
     * @return 上传成功后文件属性，包括etag/fileBytes
     * <p>
     * the uploadStatusCode code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     */
    private  Map<String, String> uploadStepTwo(String preUrl, InputStream fileInputStream) {
        Assert.notNull(preUrl, "预签名url为null");
        Assert.notNull(fileInputStream, "上传文件为null");

        Map<String, String> resultMap = new HashMap<>();

        try {

            // 上传文件
            URL url = new URL(preUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            OutputStream out = connection.getOutputStream();

            int copyBytes = IOUtils.copy(fileInputStream, out);

            String checksum = connection.getHeaderField("ETag").replace("\"", "");
            resultMap.put("etag", checksum);
            resultMap.put("fileBytes", copyBytes + "");

            connection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return resultMap;

    }

    /**
     * 创建逻辑模型
     *
     * @param fssHost       FSS 域名 http://fss.dev.pcep.cloud
     * @param fileName      文件名称，默认是文件的名称
     * @param httpFileUrl   网络文件，以http开头的文件，与本地文件互斥
     * @param localFile     本地文件，与运行代码同在一个服务器，与网络文件互斥
     * @param description   文件描述
     * @param dataRegion    油田标识  （例如: JD）
     * @param parentDirCode 文件结构目录代码，默认是根目录，如果是自己的业务文件，请尽量避免上传到默认目录
     * @param extendMap     扩展属性
     * @param header         header
     * @return 文件逻辑模型
     */
    private String uploadStepThree(String fssHost,
                                   String fileName, String httpFileUrl, InputStream localFile,
                                   String description,
                                   String dataRegion, String parentDirCode,
                                   Map<String, Object> extendMap, HttpHeaders header) {

        Assert.notNull(fssHost, "FSS 地址为null");
        Assert.notNull(fileName, "上传文件名称为null");
        Assert.notNull(parentDirCode, "文件树形目录为null");
        Assert.notNull(dataRegion, "dataRegion为null");

        //1、获取预签名url的值
        JSONObject config = uploadStepOne(fssHost, dataRegion, fileName, header);
        Assert.notNull(config, "FSS上传第一步，生成的预签名URL为null");

        //2、上传文件到服务器存储
        Map<String, String> stepTwoResultMap = null;
        stepTwoResultMap = uploadStepTwo(config.getString("presignedUrl"), localFile);

        Assert.notNull(stepTwoResultMap, "FSS上传第二步，通过IO流上传文件出错");

//        String url = "http://fss.dev.pcep.cloud/sys/file/_upload?fileName=abc42.txt&parentDirCode=0vpm1rdqpt4u9rau478w1z0b&description=%E5%87%8C%E7%A9%BA%E5%85%AB%E6%AE%B5&filePath=HQ%2Fabc41.txt&checksum=1e20a84875855238bda1407b39909fd3&dataRegion=HQ&storageName=HQ_minio&fileSize=13";
//                      http://www.dev.pcep.cloud/sys/file/_upload?fileName=%E9%9D%B6%E7%82%B9%E6%A8%A1%E6%9D%BF.xlsx&parentDirCode=CRP_ASI_FILE_CLASS_00002&filePath=TL%2F%E9%9D%B6%E7%82%B9%E6%A8%A1%E6%9D%BF.xlsx.202110211149014.xlsx&checksum=80ef5de7995001f78e53f27e27666446&dataRegion=TL&storageName=TL_hw&fileSize=9044
        //文件属性
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        String url = String.format("%s/sys/file/_upload", fssHost);
        param.add("fileName", fileName);
        param.add("parentDirCode", parentDirCode);
        param.add("filePath", config.getString("objectKey"));
        param.add("checksum", stepTwoResultMap.get("etag"));
        param.add("dataRegion", dataRegion);
        param.add("storageName", config.getString("storageName"));
        param.add("fileSize", stepTwoResultMap.get("fileBytes"));
        if (StringUtils.isNotBlank(description)) {
            param.add("description", description.trim());
        }
        //扩展属性
        if (null != extendMap && extendMap.size() > 0) {
//            extendMap.forEach(
//                    (k, v) -> {
//                        System.out.println("扩展属性:extend key=" + k);
//                        System.out.println("扩展属性:extend value=" + v);
//                        param.add("attrNameValueMap[" + k + "]", v);
//                    }
//            );
            for(Map.Entry entry : extendMap.entrySet()){
                System.out.println("扩展属性:extend key=" + entry.getKey());
                System.out.println("扩展属性:extend value=" + entry.getValue());
                param.add("attrNameValueMap[" + entry.getKey() + "]", entry.getValue());
            }

        }
        System.out.println("逻辑模型所有的输入属性为 ： " + param);

        //HttpHeaders headers = HeaderTool.getHeadersForUpload(token);
        header.setContentType(MediaType.MULTIPART_FORM_DATA);//注意：upload方法只支持表单信息
        //requestHttpEntity
        HttpEntity<MultiValueMap<String, Object>> requestHttpEntity = new HttpEntity<>(param, header);

        //发起请求
        RestTemplate restTemplate = new RestTemplate();
        String body = restTemplate.exchange(url, HttpMethod.POST, requestHttpEntity, String.class).getBody();
        //System.out.println("step three result : " + body);
        return body;

    }


    /**
     * 上传本地文件到FSS系统
     *
     * @param localFile     本地文件
     * @param fileName      文件名称，默认是文件的名称
     * @param description   文件描述
     * @param dataRegion    油田标识  （例如: TL）
     * @param parentDirCode 文件结构目录代码，默认是根目录，如果是自己的业务文件，请尽量避免上传到默认目录
     * @param extendMap     扩展属性
     */
    public  String uploadFileToFss(String fileName, InputStream localFile,
                                         String description, String dataRegion, String parentDirCode,
                                         Map<String, Object> extendMap, HttpHeaders header,String fssHost) {

        //String fssHost = ServiceParamsManager.getFssHost();
        //String fssHost = fssHosts;
        System.out.println("fssHost=" + fssHost);

        String fileLogicModel = uploadStepThree(
                fssHost, fileName, null, localFile,
                description, dataRegion,
                parentDirCode,
                extendMap, header);
/*
*  private static String uploadStepThree(String fssHost,
                                   String fileName, String httpFileUrl, File localFile,
                                   String description,
                                   String dataRegion, String parentDirCode,
                                   Map<String, Object> extendMap, String token) */
        return fileLogicModel;

    }
}
