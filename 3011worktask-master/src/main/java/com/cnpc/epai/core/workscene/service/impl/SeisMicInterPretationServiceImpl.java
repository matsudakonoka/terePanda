package com.cnpc.epai.core.workscene.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.core.workscene.pojo.rfBaseFaultPolygonConvertor;
import com.cnpc.epai.core.workscene.pojo.rfBaseStratumConvertor;
import com.cnpc.epai.core.workscene.pojo.rfFileInfo;
import com.cnpc.epai.core.workscene.pojo.rfStratumConvertorManager;
import com.cnpc.epai.core.workscene.service.SeisMicInterPretationService;
import com.cnpc.epai.core.worktask.util.HeaderTool;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: SeisMicInterPretationServiceImpl
 * @Description:
 * @Author
 * @Date 2022/10/25
 * @Version 1.0
 */
@Slf4j
@Service
public class SeisMicInterPretationServiceImpl implements SeisMicInterPretationService {

    @Autowired
    private RestTemplate restClient;
    @Value("${epai.domainhost}")
    private String serverAddr;

    private rfBaseStratumConvertor _stratumConvertor = null; //层位转换适配器
    private rfBaseFaultPolygonConvertor _faultPolygonConvertor = null;
    private String _parentDircode = ""; //父目录ID
    private String _workRoomID = ""; //工作室ID
    private rfFileInfo _fileInfo = new rfFileInfo();   //缓存文件信息

    @Override
    public String parseStratumFileToFS(String orginFileID,String token) {
        HttpHeaders header = null;
        if(StringUtils.isNotEmpty(token)){
            header = HeaderTool.getSimpleHeader(token);
        }
        boolean ret = false;
        JSONArray fileIDArray = new JSONArray();
        String fileID = "-1";
        //根据文件ID获取文件名称
        ret = getFileInfoFromID(orginFileID,header);
        if (!ret) {
            log.info("2009seismicinterpretation:parseStratumFileToFS:获取文件信息失败");
            return fileID;
        }
        //标准文件不用解析
        if (_fileInfo.extension.toLowerCase().equals("rf3h")) {
            log.info("2009seismicinterpretation:parseStratumFileToFS:已经是标准文件");
            return fileID;
        }
        //根据文件ID下载文件到本地缓存
        String extName = "." + _fileInfo.extension;
        String folder = FileUtils.getTempDirectory().getAbsolutePath() + "/";
        String fileOriginName = folder + orginFileID + extName;
        extName = ".rf3h";
        String fileDesName = folder + orginFileID + extName;
        try {
            File file = new File(fileOriginName);
            getFileFromHttp(orginFileID, "rf3h", false, fileOriginName,header,token);
            //file = new File(fileDesName);
            ret = parseStratumFile(fileOriginName, fileDesName);
            if (ret == true) {
                //根据需求设置下面属性，后面补充完善。
                fileID = upLoadBigFileToFS(orginFileID, fileDesName,extName,header);
                String stratumName = _stratumConvertor.stratumFile.Name;
                String verticalDomain = _stratumConvertor.stratumFile.verticalDomain;
                String verticalDomainUnit = _stratumConvertor.stratumFile.verticalDomainUnit;
                String lineColor = String.valueOf(_stratumConvertor.stratumFile.lineColor);
                String lineType = String.valueOf(_stratumConvertor.stratumFile.lineType);
                String fileName = _fileInfo.fileTitle + extName;
                String jsonContent = String.format("[{\"file_id\":\"%s\",\"StratumName\":\"%s\",\"VerticalDomain\":\"%s\",\"VerticalDomainUnit\":\"%s\",\"LineColor\":\"%s\",\"lineType\":\"%s\",\"file_name\":\"%s\"}]", fileID, stratumName,
                        verticalDomain, verticalDomainUnit, lineColor, lineType, fileName);
                fileIDArray = JSONArray.parseArray(jsonContent);
            }
        } catch (Exception e) {
            log.info("2009seismicinterpretation:parseStratumFileToFS:" + e.getMessage());
        } finally {
            deleteFile(fileOriginName);
            deleteFile(fileDesName);
            return fileID;
        }
    }

    @Override
    public byte[] getFileDataFromHttp(String fileId, String extName, boolean isAdded,String token) {
        HttpHeaders header = null;
        if(StringUtils.isNotEmpty(token)){
            header = HeaderTool.getSimpleHeader(token);
        }
        String szHttpFile = "http://1016fss/sys/file/attachment/" + fileId + "/attachment." + extName.toLowerCase();
        if (isAdded == false) {
            szHttpFile = "http://1016fss/sys/file/" + fileId;
        }
        log.error(szHttpFile);
        byte[] btData = null;
        log.error("2009seismicinterpretation:GetFileDataFromHttp:Begin download");
        try {
            HttpHeaders headers = header==null?new HttpHeaders():header;
            HttpEntity htpEntity = new HttpEntity(headers);
            if(StringUtils.isEmpty(token)){
                btData = restClient.exchange(szHttpFile, HttpMethod.GET, htpEntity, byte[].class).getBody();
            }else {
                RestTemplate restTemplate = new RestTemplate();
                String url = String.format("http://%s/sys/file/%s", serverAddr,fileId);
                btData = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers),byte[].class).getBody();
            }
            log.error("2009seismicinterpretation:GetFileDataFromHttp:End download");
        } catch (Exception e) {
            log.error("2009seismicinterpretation:GetFileDataFromHttp:" + fileId + e.getMessage());
            btData = null;
        }
        return btData;
    }

    /**
     * 获取文件信息
     *
     * @param fileID 文件ID
     * @return 操作是否成功，true--成功，false失败，具体信息在_fileInfo中
     */
    private boolean getFileInfoFromID(String fileID,HttpHeaders headers) {
        boolean ret = false;
        String szHttpFile = String.format("http://1016fss/sys/file/%s/detail.json", fileID);
        String content = "";
        try {
            if(headers == null){
                content = restClient.getForEntity(szHttpFile, String.class).getBody();
            }else {
                RestTemplate restTemplate = new RestTemplate();
                String url = String.format("http://%s/sys/file/%s/detail.json", serverAddr,fileID);
                content = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers),String.class).getBody();
            }

            JSONObject son = JSON.parseObject(content);
            _fileInfo.fileName = JSON.parseObject(son.getString("basic")).getString("fileName");
            _fileInfo.fileSize = JSON.parseObject(son.getString("basic")).getIntValue("fileSize");
            _fileInfo.extension = JSON.parseObject(son.getString("basic")).getString("extension");
            _fileInfo.dataRegion = son.getJSONObject("basic").getString("dataRegion");
            _fileInfo.checksum = son.getJSONArray("versions").getJSONObject(0).getString("checksum");
            JSONArray sonArray = JSON.parseArray(son.getString("parentDirs"));
            if (sonArray.size() > 0)
                _parentDircode = JSON.parseObject(sonArray.get(0).toString()).getString("dirCode");
            int nPos = _fileInfo.fileName.lastIndexOf(".");
            if (nPos != -1) {
                _fileInfo.fileTitle = _fileInfo.fileName.substring(0, nPos);
            } else {
                _fileInfo.fileTitle = _fileInfo.fileName;
            }
            sonArray = JSON.parseArray(son.getString("extended"));
            if (sonArray.size() > 0) {
                for (int i = 0; i < sonArray.size(); i++) {
                    // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                    JSONObject job = sonArray.getJSONObject(i);
                    if (job.get("attrName").toString().equals("DATA_GROUP")) {
                        _workRoomID = job.get("attrValue").toString();
                        break;
                    }
                }
            }
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            _fileInfo.fileTitle = "";
            log.info("SeisMicInterPretationService:getFileInfoFromID:" + e.getMessage());
        }
        return ret;
    }

    /**
     * 解析层位文件为标准文件
     *
     * @param orginFileName 原始文件
     * @param rfFileName    标准文件
     * @return Boolean  返回结果
     * @out 解析是否成功
     */
    public Boolean parseStratumFile(String orginFileName, String rfFileName) {
        Boolean ret = false;
        try {
            ret = true;
            _stratumConvertor = rfStratumConvertorManager.GetStratumConvertor(orginFileName);
            if (_stratumConvertor == null) {
                log.info("层位文件不可识别");
                ret = false;
                return ret;
            }
            ret = _stratumConvertor.ReadFile(orginFileName);
            if (ret)
                _stratumConvertor.WriteFile(rfFileName);
            //ret = true;
        } catch (Exception e) {
            log.info("2009seismicinterpretation:parseStratumFile:" + e.getMessage());
        } finally {
            return ret;
        }
    }

    /**
     * 层位标准文件入库并和主文件关联
     *
     * @param orginFileID 原始文件ID
     * @param fileDesName 目标文件名称
     * @return 返回标准层位文件ID
     */
    private String upLoadBigFileToFS(String orginFileID, String fileDesName,String extName,HttpHeaders header) {
        String fsID = "";
        try {
            String serviceHost = "http://1016fss";
            String urlService = String.format("%s/sys/file/_upload", serviceHost);
            //得到输入流
            File file = new File(fileDesName);
            if (file.exists()) {
                HttpHeaders headers = header == null?new HttpHeaders():header;
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();
                String fileName = _fileInfo.fileTitle + extName;
                String dataRegion = StringUtils.isEmpty(System.getenv("DATA_REGION"))?"TL":System.getenv("DATA_REGION");
                JSONObject jsonPresignedInfo = GetPresignedInfo(fileName, dataRegion,header);
                // 上传文件
                //URL url = new URL(jsonPresignedInfo.getString("presignedUrl"));
                //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //connection.setDoOutput(true);
                //connection.setRequestMethod("PUT");
                //try(FileInputStream fileInputStream =  new FileInputStream(fileDesName);
                //    OutputStream out = connection.getOutputStream()) {
                //    int copyBytes = IOUtils.copy(fileInputStream, out);
                //}
                //connection.disconnect();
                //HttpHeaders headers = new HttpHeaders();
                // 上传文件,修改为okhttp处理模式
                 URL url = new URL(jsonPresignedInfo.getString("presignedUrl"));
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(600, TimeUnit.SECONDS)//设置连接超时时间
                        .readTimeout(600, TimeUnit.SECONDS)//设置读取超时时间
                        .build();


                RequestBody body = RequestBody.create(file, okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
                Request request = new Request.Builder().url(url).method("PUT", body).build();
                try (Response response = client.newCall(request).execute()) {
                    log.info("isSuccessful: {}", response.isSuccessful());
                }
                String storeageName = jsonPresignedInfo.getString("storageName");
                String filePath = dataRegion + "/" + fileName;
                param.add("fileName", fileName);
                param.add("parentDirCode", _parentDircode);
                //String md5 = getMD5(file);
                String checksum = getMD5(file);//connection.getHeaderField("ETag").replace("\"", "");
                param.add("checksum", checksum);
                param.add("filePath", jsonPresignedInfo.getString("objectKey"));
                param.add("dataRegion", dataRegion);
                param.add("storageName", storeageName);
                param.add("fileSize", String.valueOf(file.length()));
                String psFlag = "层位解析";
                param.add("attrNameValueMap[DATA_GROUP]", _workRoomID);
                param.add("attrNameValueMap[PSFLAG]", psFlag);
                param.add("attrNameValueMap[APP_DOMAIN]", "CRP");
                HttpEntity htpEntity = new HttpEntity(param, headers);
                String retStr = "";
                if(header == null){
                    retStr = restClient.postForObject(urlService, htpEntity, String.class);
                }else {
                    RestTemplate restTemplate = new RestTemplate();
                    String upLoadUrl = String.format("http://%s/sys/file/_upload", serverAddr);
                    retStr = restTemplate.exchange(upLoadUrl, HttpMethod.POST, htpEntity,String.class).getBody();
                }
                JSONObject son = JSON.parseObject(retStr);
                fsID = JSON.parseObject(son.getString("basic")).getString("fileId");
                return fsID;
            } else {
                log.info("2009seismicinterpretation:upLoadBigFileToFS:文件获取失败");
            }
            return fsID;
        } catch (Exception ex) {
            log.info("2009seismicinterpretation:upLoadBigFileToFS:" + ex.getMessage());
            return fsID;
        } finally {
        }
    }

    /**
     * 根据文件Id下载文件
     *
     * @param fileId      --文件ID
     * @param extName     --文件扩展名
     * @param isAdded     --是否附件
     * @param tarFileName --目标文件名称
     * @return 获取文件是否成功
     * 备注：通过流方式下载，避免原来下载到内存太大，出现异常的问题。
     */
    public boolean getFileFromHttp(String fileId, String extName, boolean isAdded, String tarFileName,HttpHeaders headers,String token) {
        boolean isRet = getFileInfoFromID(fileId,headers);
        if (isRet == false) {
            log.info("SeisMicInterPretationService:getFileFromHttp:getFileInfoFromID失败");
            return false;
        }
        byte[] btData = getFileDataFromHttp(fileId, extName, isAdded,token);
        if (btData == null) {
            log.info("SeisMicInterPretationService:getFileFromHttp:获取文件数据体失败");
            return false;
        }
        writeToFile(btData, tarFileName);
        return true;
    }

    /**
     * 将二进制数据写入临时文件
     *
     * @param data     二进制数据
     * @param fileName 目标文件
     * @return Boolean  返回结果
     */
    public Boolean writeToFile(byte[] data, String fileName) {
        Boolean ret = false;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            //创建输出流
            fos = new FileOutputStream(fileName);
            dos = new DataOutputStream(fos);
            dos.write(data);
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                log.info("SeisMicInterPretationService:writeToFile:" + e.getMessage());
                e.printStackTrace();
            }
            return ret;
        }
    }

    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    private JSONObject GetPresignedInfo(String fileName, String dataRegion,HttpHeaders headers) {
        JSONObject info = null;
        String szHttpFile = String.format("http://1016fss/sys/file/s3/generatePresignedUploadUrl.json?dataRegion=%s&fileName=%s", dataRegion, fileName);
        try {
            String content = "";
            if(headers == null){
                content = restClient.getForEntity(szHttpFile, String.class).getBody();
            }else {
                RestTemplate restTemplate = new RestTemplate();
                String url = String.format("http://%s/sys/file/s3/generatePresignedUploadUrl.json?dataRegion=%s&fileName=%s",serverAddr, dataRegion, fileName);
                content = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(headers),String.class).getBody();
            }
            info = JSON.parseObject(content);
            //storeageName = son.getString("storageName");
        } catch (Exception e) {
            e.printStackTrace();
            _fileInfo.fileTitle = "";
            log.info("2009seismicinterpretation:getFileInfoFromID:" + e.getMessage());
        }
        return info;
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        boolean isRet = false;
        File file = new File(fileName);
        try {
            log.info("SeisMicInterPretationService:deleteFile:" + fileName + "开始!");
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile()) {
                isRet = file.delete();
                if (isRet) {
                    log.info("SeisMicInterPretationService:deleteFile:" + fileName + "成功!");
                    return isRet;
                } else {
                    log.info("SeisMicInterPretationService:deleteFile:" + fileName + "失败!");
                    return isRet;
                }
            } else {
                log.info("SeisMicInterPretationService:deleteFile:" + fileName + "不存在!");
                return isRet;
            }
        } catch (Exception e) {
            log.info("SeisMicInterPretationService:deleteFile Exception:" + fileName + e.getMessage());
            return isRet;
        }
    }

    /**
     * 上传附件
     *
     * @param fileID   原始文件ID
     * @param fileName 附件文件名称
     * @param title    标题
     * @param extName  扩展名
     * @return 上传是否成功 1--成功 0--不成功
     */
    private int upAccessoryFileToFSEx(String fileID, String fileName, String title, final String extName) {
        try {
            ResponseEntity<JSONObject> configEntity = GetAttachmentPresignedInfo(fileID, "attachment" + extName);
            //得到输入流
            File file = new File(fileName);
            if (file.exists() == false) {
                log.info("SeisMicInterPretationService:upAccessoryFileToFSEx:" + fileID + "对应的文件不存在！");
                return 0;
            }
            //HttpHeaders headers = new HttpHeaders();
            // 上传文件
            URL url = new URL(configEntity.getBody().getString("presignedUrl"));
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(600, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(600, TimeUnit.SECONDS)//设置读取超时时间
                    .build();


            RequestBody body = RequestBody.create(file, okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            Request request = new Request.Builder().url(url).method("PUT", body).build();
            try (Response response = client.newCall(request).execute()) {
                log.info("isSuccessful: {}", response.isSuccessful());
            }
            return 1;
        } catch (Exception ex) {
            log.info("SeisMicInterPretationService:upAccessoryFileToFSEx:" + fileID + ex.getMessage());
            throw new RuntimeException("ERROR:上传附件文件到FS系统失败");
        } finally {
        }
    }

    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    private ResponseEntity<JSONObject> GetAttachmentPresignedInfo(String fileID, String fileName) {
        // 获取上传配置
        String url = String.format("http://1016fss/sys/file/attachment/%s/generatePresignedUploadUrl", fileID);
        URI uri = UriComponentsBuilder.fromUriString(url).queryParam("attachmentName", fileName)
                .build(false).toUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpHeaders headers = new HttpHeaders();
        // 在单元测试中，可以打开此行注释，因为服务请求是需要授权信息的。但是在正式代码中不需要，因为框架底层将授权信息自动注入到请求头了。
//        httpHeaders.set("Authorization", "Bearer " + daemonTokenGenerator.generate());
        ResponseEntity<JSONObject> configEntity = restClient.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(null, httpHeaders),
                JSONObject.class);
        return configEntity;
    }

    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    private static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
