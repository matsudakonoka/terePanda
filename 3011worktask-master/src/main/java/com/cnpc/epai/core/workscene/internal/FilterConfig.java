package com.cnpc.epai.core.workscene.internal;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.ServiceLoader;

@Component
public class FilterConfig implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private ApplicationContext applicationContext;
    private Filter filterChain;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.filterChain = buildFilterChain();
    }

    public Filter buildFilterChain() {
        ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
        Iterator<Filter> iterator = serviceLoader.iterator();
        Filter last = null;
        while (iterator.hasNext()) {
            Filter filter = iterator.next();
            filter.setFilter(last);
            filter.setApplicationContext(applicationContext());
            last = filter; 
        }
        return last;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext applicationContext() {
        return applicationContext;
    }

    public Filter chain() {
        return filterChain;
    }
}
