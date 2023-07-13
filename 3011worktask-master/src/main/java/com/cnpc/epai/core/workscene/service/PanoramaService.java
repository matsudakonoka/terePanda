package com.cnpc.epai.core.workscene.service;

import com.cnpc.epai.core.workscene.internal.Node;

public interface PanoramaService {
    Node panorama(String workId, String nodeId);

    Object getAchieve(String workId, String nodeId);
}
