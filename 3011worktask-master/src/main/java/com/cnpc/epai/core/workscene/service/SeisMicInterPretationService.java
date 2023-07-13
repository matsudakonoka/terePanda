package com.cnpc.epai.core.workscene.service;

import org.springframework.stereotype.Service;

/**
 * @ClassName: SeisMicInterPretationService
 * @Description:
 * @Author
 * @Date 2022/10/25
 * @Version 1.0
 */
@Service
public interface SeisMicInterPretationService {

    String parseStratumFileToFS(String fileID,String token);

    byte[] getFileDataFromHttp(String fileId,String extName, boolean isAdded,String  token);
}
