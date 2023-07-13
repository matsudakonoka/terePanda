package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ResearchManageServiceImpl implements ResearchManageService{
    @Value("${epai.domainhost}")
    private String ServerAddr;

    @Override
    public Object getUnitWorkTree(String nodeName, HttpServletRequest request) {
        String treeId = getTreeId(request);
        List<Map<String, Object>> fullTree = getFullTree(request, treeId);
        if (StringUtils.isNotBlank(nodeName)){
            List<String> nodes = findNode(request, treeId, nodeName);
            ArrayList<Map<String, Object>> maps = new ArrayList<>();
            for (String node : nodes) {
                maps.add(getTreeByNodeId(request, treeId, node));
            }
            return maps;
        }
        return fullTree;
    }

    private String getTreeId(HttpServletRequest request){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://" + ServerAddr + "/core/objdataset/navigate/searchtemplate?templateLevel=T0&isPageable=false", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);
        Map<String,Object> body = exchange.getBody();
        List<Map> result = (List<Map>) body.get("result");
        return  (String) result.get(0).get("treeId");
    }

    private List<Map<String, Object>> getFullTree(HttpServletRequest request,String treeId){
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/fulltree");
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization",request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
        Map<String,Object> body = exchange.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        return trees;
    }

    private List<String> findNode(HttpServletRequest request,String treeId,String nodeName){
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/findnode?name="+nodeName);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<JSONObject> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
        Map<String,Object> body = ex.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        ArrayList<String> list = new ArrayList<>();
        for (Map<String, Object> tree : trees) {
            list.add(tree.get("nodeId").toString());
        }
        return list;
    }

    private Map<String, Object> getTreeByNodeId(HttpServletRequest request, String treeId, String nodeId){
        RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://"+ServerAddr+"/core/objdataset/navigate/"+treeId+"/fulltree?nodeId="+nodeId);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization",request.getHeader("Authorization"));
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
        Map<String,Object> body = exchange.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        if (trees!=null){
            return trees.get(0);
        }
        return null;
    }

    public static void main(String[] args) {
        /*RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoiMTAwMDI2NzE4LnB0ciIsInVzZXJfaWQiOiJmbTd5dmE1eXZza2I4bW03dzdxZjd6MHQiLCJ1c2VyX25hbWUiOiIxMDAwMjY3MTgucHRyIiwic2NvcGUiOlsib3BlbmlkIl0sIm9yZ2FuaXphdGlvbiI6Ik9SR0FUTDEwMDAwMDI3NCIsImlzcyI6ImEzNmMzMDQ5YjM2MjQ5YTNjOWY4ODkxY2IxMjcyNDNjIiwiZXhwIjoxNjQwMjU1NzM1LCJkaXNwbGF5X25hbWUiOiLljY7lu7oiLCJyZWdpb24iOiJUTCIsImlhdCI6MTYzNzY2MzczNTExNCwianRpIjoiNGI0ZThlOWItNGU4Yy00NmE1LWIwNDktNTc0YmEwNTE3NGEyIiwiY2xpZW50X2lkIjoid2ViYXBwIn0.JrtkgulhXrT7mzGnq05lrIaMwPXs739NgJOvt4xuRw1HJ1w-qaGf9z42cQKRaY45WayjEFurhqE9HuvgaQlPZyaFYmTZcdSkMnqeEQWjMxzij971U-qsJPXiL8qjcuxPhMkKFSstG0MhHaxdi00sDq8XKWTC-ILmnNlAaB64v6y-Lp2N7EwThRr2tLY4mjYdECnadFTRxvFeMPf8Ux2Kgo89JNv16CThVCF8JMfsgg_bj8w0dnpwkzFEHh4mJSRMfyQmOSf4aj2X-3FlrCxf8_FX2JDtC1mSNgYeIXXuveS6aHY3k5yGoDWKaXXNmAywxGZGBDfe3P8IvsgNs9N9SQ");
        ResponseEntity<JSONObject> exchange = restTemplate.exchange("http://www.dev.pcep.cloud/core/objdataset/navigate/searchtemplate?templateLevel=T0&isPageable=false", HttpMethod.GET, new HttpEntity<>(null, headers), JSONObject.class);
        Map<String,Object> body = exchange.getBody();
        List<Map> result = (List<Map>) body.get("result");
        String treeId = (String) result.get(0).get("treeId");*/

        /*RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://www.dev.pcep.cloud/core/objdataset/navigate/7e79be2b5683447cb57c0c3730239a6f/fulltree");
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization","Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoiMTAwMDI2NzE4LnB0ciIsInVzZXJfaWQiOiJmbTd5dmE1eXZza2I4bW03dzdxZjd6MHQiLCJ1c2VyX25hbWUiOiIxMDAwMjY3MTgucHRyIiwic2NvcGUiOlsib3BlbmlkIl0sIm9yZ2FuaXphdGlvbiI6Ik9SR0FUTDEwMDAwMDI3NCIsImlzcyI6ImEzNmMzMDQ5YjM2MjQ5YTNjOWY4ODkxY2IxMjcyNDNjIiwiZXhwIjoxNjQwMjU1NzM1LCJkaXNwbGF5X25hbWUiOiLljY7lu7oiLCJyZWdpb24iOiJUTCIsImlhdCI6MTYzNzY2MzczNTExNCwianRpIjoiNGI0ZThlOWItNGU4Yy00NmE1LWIwNDktNTc0YmEwNTE3NGEyIiwiY2xpZW50X2lkIjoid2ViYXBwIn0.JrtkgulhXrT7mzGnq05lrIaMwPXs739NgJOvt4xuRw1HJ1w-qaGf9z42cQKRaY45WayjEFurhqE9HuvgaQlPZyaFYmTZcdSkMnqeEQWjMxzij971U-qsJPXiL8qjcuxPhMkKFSstG0MhHaxdi00sDq8XKWTC-ILmnNlAaB64v6y-Lp2N7EwThRr2tLY4mjYdECnadFTRxvFeMPf8Ux2Kgo89JNv16CThVCF8JMfsgg_bj8w0dnpwkzFEHh4mJSRMfyQmOSf4aj2X-3FlrCxf8_FX2JDtC1mSNgYeIXXuveS6aHY3k5yGoDWKaXXNmAywxGZGBDfe3P8IvsgNs9N9SQ");
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
        Map<String,Object> body = exchange.getBody();
        List<Tree> trees = (List<Tree>) body.get("result");
        System.out.println(trees);*/

        /*RestTemplate restTemplate = new RestTemplate();
        StringBuffer serviceName = new StringBuffer();
        serviceName.append("http://www.dev.pcep.cloud/core/objdataset/navigate/7e79be2b5683447cb57c0c3730239a6f/findnode?name="+"资料及评价");
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl9uYW1lIjoiMTAwMDI2NzE4LnB0ciIsInVzZXJfaWQiOiJmbTd5dmE1eXZza2I4bW03dzdxZjd6MHQiLCJ1c2VyX25hbWUiOiIxMDAwMjY3MTgucHRyIiwic2NvcGUiOlsib3BlbmlkIl0sIm9yZ2FuaXphdGlvbiI6Ik9SR0FUTDEwMDAwMDI3NCIsImlzcyI6ImEzNmMzMDQ5YjM2MjQ5YTNjOWY4ODkxY2IxMjcyNDNjIiwiZXhwIjoxNjQwMjU1NzM1LCJkaXNwbGF5X25hbWUiOiLljY7lu7oiLCJyZWdpb24iOiJUTCIsImlhdCI6MTYzNzY2MzczNTExNCwianRpIjoiNGI0ZThlOWItNGU4Yy00NmE1LWIwNDktNTc0YmEwNTE3NGEyIiwiY2xpZW50X2lkIjoid2ViYXBwIn0.JrtkgulhXrT7mzGnq05lrIaMwPXs739NgJOvt4xuRw1HJ1w-qaGf9z42cQKRaY45WayjEFurhqE9HuvgaQlPZyaFYmTZcdSkMnqeEQWjMxzij971U-qsJPXiL8qjcuxPhMkKFSstG0MhHaxdi00sDq8XKWTC-ILmnNlAaB64v6y-Lp2N7EwThRr2tLY4mjYdECnadFTRxvFeMPf8Ux2Kgo89JNv16CThVCF8JMfsgg_bj8w0dnpwkzFEHh4mJSRMfyQmOSf4aj2X-3FlrCxf8_FX2JDtC1mSNgYeIXXuveS6aHY3k5yGoDWKaXXNmAywxGZGBDfe3P8IvsgNs9N9SQ");
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);
        ResponseEntity<JSONObject> ex = restTemplate.exchange(serviceName.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
        Map<String,Object> body = ex.getBody();
        List<Map<String, Object>> trees = (List<Map<String, Object>>) body.get("result");
        ArrayList<String> list = new ArrayList<>();
        for (Map<String, Object> tree : trees) {
            list.add(tree.get("nodeId").toString());
        }
        for (String s : list) {
            System.out.println(s);
        }*/
    }
}
