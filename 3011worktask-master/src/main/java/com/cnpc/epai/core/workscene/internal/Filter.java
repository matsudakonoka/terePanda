package com.cnpc.epai.core.workscene.internal;

import org.springframework.context.ApplicationContextAware;

public interface Filter extends ApplicationContextAware {
    Node invoke(Node result);
    void setFilter(Filter filter);
}
