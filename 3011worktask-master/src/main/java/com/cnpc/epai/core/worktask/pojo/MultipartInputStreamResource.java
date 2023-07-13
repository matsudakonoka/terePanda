package com.cnpc.epai.core.worktask.pojo;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import lombok.Getter;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author liuxiong
 * @date 2018-4-13 14:15:03
 */
public class MultipartInputStreamResource extends InputStreamResource {

    @Getter
    private String filename;

    public MultipartInputStreamResource(String filename, InputStream inputStream) {
        super(inputStream);
        this.filename = filename;
    }
    @Override
    public long contentLength() throws IOException {
        return -1;
    }
}