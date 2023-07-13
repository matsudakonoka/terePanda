package com.cnpc.epai.core.workscene.commom;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.http.HttpHeaders.ACCEPT;

public class HttpUtils {

    public static HttpHeaders header() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + TokenUtil.getToken());
        headers.set(ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
