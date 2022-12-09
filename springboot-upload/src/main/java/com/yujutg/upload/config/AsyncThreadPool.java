package com.yujutg.upload.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableAsync
public class AsyncThreadPool {

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor(){
        log.info("start asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    boolean f = executor.getQueue().offer(r, 60, TimeUnit.SECONDS);
                    log.warn("add task: {}, task:{}, comp:{}, action:{}", f, executor.getTaskCount(), executor.getCompletedTaskCount(), executor.getActiveCount());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.warn("time out task");
                }
            }
        });
        executor.initialize();
        return executor;
    }

}
