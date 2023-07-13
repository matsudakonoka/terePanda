package com.cnpc.epai.core.worktask.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ExecutorService synchronousDataExecutorPool(){
        return Executors.newSingleThreadExecutor();
    }
}
