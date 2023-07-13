package com.cnpc.epai.core.workscene.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.common.util.User;
import com.cnpc.epai.core.workscene.service.SeisMicInterPretationService;
import com.cnpc.epai.core.worktask.domain.ResearchWorkDto;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.service.WorkObjectServiceImpl;
import com.cnpc.epai.core.worktask.util.HeaderTool;
import com.cnpc.epai.core.worktask.util.JwtUtil;
import com.cnpc.epai.core.worktask.util.fssTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: mqListener
 * @Description:
 * @Author
 * @Date 2022/8/29
 * @Version 1.0
 */
@Component
public class AsiListener {

    String treeId = "OJz8guY8EFDDj1zbfgqKpUNJ";//3c1v7esiwre25swt0ugstgrp

    @Value(("${epai.confighost.fss_host}"))
    private String fss_host;

    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Autowired
    SeisMicInterPretationService   seisMicInterPretationService;
    @Autowired
    private WorkObjectServiceImpl workObjectService;


    @RabbitListener(queues = "EPAIASI.AchievementFile")
    public void onMessage(Channel channel, Message message,@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        try {
            //MessageProperties messageProperties = message.getMessageProperties();
            //代表投递的标识符，唯一标识了当前信道上的投递
            //long deliveryTag = messageProperties.getDeliveryTag();
            //如果是重复投递的消息 true 用作消费幂等处理
            //Boolean redelivered = messageProperties.getRedelivered();
            //获取生产者发送的原始消息
            //String originalMessage = new String(message.getBody());

            //1、队列数据处理
            System.out.println("mq message = " + new String(message.getBody()));
            JSONObject data = JSONObject.parseObject(new String(message.getBody()));
            JSONObject seisObject = data.getJSONObject("SEISObject");
            String dataUrl,fileName,fullFileName,workroomID,token,fileId ="",objectId,objectName,dataSetId,
                    dataSetName,workId,nodeId,nodeNamesDataTargetType,taskId,eoCode= "";
            if(seisObject != null){
                eoCode = data.getString("eoCode");
                dataUrl = seisObject.getString("dataUrl");
                fileName = seisObject.getString("name");
                fullFileName = fileName+".dat";
                workroomID = data.getString("workroomID");
                token = data.getString("token");
                objectId = data.getString("objectId");
                objectName = data.getString("objectName");
                dataSetId = data.getString("datasetId");
                dataSetName = data.getString("dataSetName");
                workId = data.getString("workId");
                nodeId = data.getString("nodeId");
                nodeNamesDataTargetType = data.getString("nodeNames");
                taskId = data.getString("taskId");

            }else{
                return;
            }
            RestTemplate restTemplate = new RestTemplate();
            byte[] forObject = restTemplate.getForObject(dataUrl, byte[].class);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(forObject);

            //2、上传文件
            try{
                fssTool fssToll = new fssTool();
                HttpHeaders header = HeaderTool.getSimpleHeader(token);
                header.set("clientId","CRP");
                Map<String, Object> metaAttribute = new HashMap<String, Object>();
                metaAttribute.put("APP_DOMAIN", "CRP");
                metaAttribute.put("DATA_GROUP", workroomID);
                String dataRegion = StringUtils.isEmpty(System.getenv("DATA_REGION"))?"TL":System.getenv("DATA_REGION");
                String fileLogicModel = fssToll.uploadFileToFss(fullFileName, byteArrayInputStream, null,dataRegion , treeId, metaAttribute, header,"http://" + fss_host);
                ObjectMapper mapper = new ObjectMapper();
                fileId = mapper.readTree(fileLogicModel).get("basic").get("fileId").asText();
                System.out.println("fileId = " + fileId);
            }catch (Exception e){
                System.out.println("上传文件出错");
                e.printStackTrace();
            }


            //3、解析文件
            String parseFileId = "";
            HttpHeaders header = HeaderTool.getSimpleHeader(token);
            HttpEntity<String> httpEntity = new HttpEntity(null,header);
            try {
                boolean parse = false;
                boolean parseSEISHorizon = false;
                String parseUrl = String.format("http://%s/common/seismicinterpretation/%s/",ServerAddr,fileId);
                if(eoCode.equals("SEISPolygonSet")){//解释多边形数据体
                    parseUrl = parseUrl + "parseFaultPolygonFileToFS";
                    parse = true;
                }else if(eoCode.equals("SEISFault")){//解释断层数据体
                    parseUrl = parseUrl + "parseFaultFileToFS";
                    parse = true;
                }else if(eoCode.contains("SEISHorizon")){//"SEISHorizon3DDate,SEISHorizon2DDate"://解释层位数据体
                    //parseUrl = parseUrl +"parseStratumFileToFS";
                    parseFileId = seisMicInterPretationService.parseStratumFileToFS(fileId,token);
                    parseSEISHorizon = true;
                    //parse = true;
                }
                if(parse){
                    JSONArray body = restTemplate.exchange(parseUrl, HttpMethod.GET, httpEntity, JSONArray.class).getBody();
                    if(body != null && body.size()>0 && body.getJSONObject(0).keySet().size()>0){
                        parseFileId = body.getJSONObject(0).getString("file_id");
                        System.out.println(fileId + "解析完成");
                    }
                }else if(parseSEISHorizon) {
                    System.out.println("SEISHorizon"+fileId + "解析完成");
                }else {
                    System.out.println(fileId + "解析失败");
                }
            }catch (Exception e){
                System.out.println("文件解析出错");
                e.printStackTrace();
            }

            //4、保存
            User userFromToken = new JwtUtil().getUserFromToken(token);
            JSONObject parametersJo = seisObject.getJSONObject("parameters");
            if(null != parametersJo && parametersJo.keySet().size()>0 && "研究院".equals(parametersJo.getString("source"))){
                //全线上成果保存
                System.out.println("研究院成果保存"+parametersJo);
                ResearchWorkDto researchWorkDto = new ResearchWorkDto();
                JSONArray jsonArray = new JSONArray();
                seisObject.put("fileId",fileId);
                seisObject.put("fileName",fullFileName);
                jsonArray.add(seisObject);

                researchWorkDto.setBelong("研究院");
                researchWorkDto.setDatasetId(dataSetId);
                researchWorkDto.setDatasetName(dataSetName);
                researchWorkDto.setDataContent(jsonArray);
                researchWorkDto.setDataStatus("待提交");
                researchWorkDto.setDataTargetType(nodeNamesDataTargetType.split("\\|")[1]);
                if(nodeNamesDataTargetType.split("\\|")[1].equals("成果列表")){
                    researchWorkDto.setTreeDataId("file");
                }else {
                    researchWorkDto.setTreeDataId("data");
                }
                researchWorkDto.setFileId(fileId);
                researchWorkDto.setFileName(fileName);
                researchWorkDto.setFileUrl(fullFileName);
                //researchWorkDto.setFlowId();
                researchWorkDto.setNodeId(nodeId);
                researchWorkDto.setNodeName(nodeNamesDataTargetType.split("\\|")[0]);
                researchWorkDto.setObjectId(objectId);
                researchWorkDto.setObjectName(objectName);
                researchWorkDto.setSource("专业软件");
                researchWorkDto.setTaskId(taskId);
                //researchWorkDto.setTreeDataId();
                researchWorkDto.setTreeDataType("非结构化");
                researchWorkDto.setWorkId(workId);
                //researchWorkDto.setWorkTreeDataId();

                String researchWorkResultSaveUrl = String.format("http://%s/research/exploration/researchwork/saveOrUpdateSrTaskTreeData?projectId=%s",ServerAddr,workroomID);
                MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
                header.setContentType(type);
                header.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
                HttpEntity<String> httpEntity2 = new HttpEntity<String>(JSONObject.toJSONString(researchWorkDto), header);
                JSONObject body = restTemplate.exchange(researchWorkResultSaveUrl, HttpMethod.POST, httpEntity2, JSONObject.class).getBody();
                if(!body.getBoolean("flag")){
                    throw new BusinessException("400","解析前的全线上成果保存错误");
                }

                //保存解析后的数据
                if(StringUtils.isNotEmpty(parseFileId) && !"-1".equals("parseFileId")){
                    JSONArray parseJsonArray = new JSONArray();
                    seisObject.put("fileId",parseFileId);
                    String parseFileName = fullFileName.split("\\.")[0]+".rf3h";
                    seisObject.put("fileName",parseFileName);
                    parseJsonArray.add(seisObject);
                    researchWorkDto.setDataContent(parseJsonArray);
                    researchWorkDto.setFileId(parseFileId);
                    researchWorkDto.setFileName(parseFileName);
                    researchWorkDto.setFileUrl(parseFileName);
                    HttpEntity<String> parseHttpEntity = new HttpEntity<String>(JSONObject.toJSONString(researchWorkDto), header);
                    JSONObject parseBody = restTemplate.exchange(researchWorkResultSaveUrl, HttpMethod.POST, parseHttpEntity, JSONObject.class).getBody();
                    if(!parseBody.getBoolean("flag")){
                        throw new BusinessException("400","解析后的全线上成果保存错误");
                    }
                }
            }else {
                SrTaskTreeData srTaskTreeData = new SrTaskTreeData();
                srTaskTreeData.setObjectId(objectId);//WELLTL100017242
                srTaskTreeData.setObjectName(objectName);//东河1-10H
                srTaskTreeData.setDatasetId(dataSetId);//ztXcVJ1HfwfaFku6xWejxdOcvFjUfKrl
                srTaskTreeData.setDatasetName(dataSetName);//解释多边形数据体
                srTaskTreeData.setWorkId(workId);//1565701111511736321
                srTaskTreeData.setNodeId(nodeId);//l0aj3ctnmt2x5lr9wkdr9hpg_1736321
                srTaskTreeData.setNodeNames(nodeNamesDataTargetType.split("\\|")[0]);//专业软件工具
                srTaskTreeData.setTaskId(taskId);//1565701111553679362
                srTaskTreeData.setDatasetType("data");
                srTaskTreeData.setSource("专业软件");
                srTaskTreeData.setDataTargetType(nodeNamesDataTargetType.split("\\|")[1]);//成果列表 研究资料
                srTaskTreeData.setDataType("非结构化");
                srTaskTreeData.setFileId(fileId);
                srTaskTreeData.setFileName(fileName);
                srTaskTreeData.setFileAllName(fullFileName);
                srTaskTreeData.setCreateUser(userFromToken.getUserId());
                srTaskTreeData.setCreateUserName(userFromToken.getDisplayName());
                srTaskTreeData.setUpdateUser(userFromToken.getUserId());
                JSONArray dataArray = new JSONArray();
                JSONArray innerAy = new JSONArray();
                innerAy.add(seisObject);
                JSONObject innerJo = new JSONObject();
                innerJo.put("dataContent", innerAy);
                dataArray.add(innerJo);
                SrTaskTreeData ss = workObjectService.saveSrTreeData(workroomID, dataArray, srTaskTreeData, null, "Bearer  " + token);
                if (ss == null) {
                    throw new BusinessException("400", "引用归档失败！");
                } else {
                    System.out.println("解析前的专业软件数据保存成功:" + ss.toString());
                }

                //保存解析后的数据
                if(StringUtils.isNotEmpty(parseFileId) && !"-1".equals("parseFileId")){
                    srTaskTreeData.setFileId(parseFileId);
                    String parseFileName = fullFileName.split("\\.")[0]+".rf3h";
                    srTaskTreeData.setFileAllName(parseFileName);
                    SrTaskTreeData parseSs = workObjectService.saveSrTreeData(workroomID, dataArray, srTaskTreeData, null, "Bearer  " + token);
                    if (parseSs == null) {
                        throw new BusinessException("400", "引用归档失败！");
                    } else {
                        System.out.println("解析后的专业软件数据保存成功:" + ss.toString());
                    }
                }
            }

            channel.basicAck(tag,false);
        }catch (Exception e) {
            e.printStackTrace();
            //channel.basicNack(deliveryTag:消息的唯一标识,multiple:是否批量处理,requeue:是否重新放入队列);
            //消息出现异常时，若requeue=false，则该消息会被放入死信队列，若没有配置死信队列则该消息会丢失。
            channel.basicNack(tag,false, false);
        }
    }
}
