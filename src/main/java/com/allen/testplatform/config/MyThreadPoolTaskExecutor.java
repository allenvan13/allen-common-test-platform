package com.allen.testplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Fan QingChuan
 * @since 2022/4/21 15:09
 */
@RefreshScope
@Configuration
@EnableAsync
@Slf4j
public class MyThreadPoolTaskExecutor {

    /**
     * 核心线程数
     */
    @Value("${threadpool.corepool.size}")
    private Integer corePoolSize = 10;
    /**
     * 最大线程数
     */
    @Value("${threadpool.maxpool.size}")
    private Integer maxPoolSize = 50;
    /**
     * 队列数
     */
    @Value("${threadpool.queuecapacity}")
    private Integer queueCapacity = 10;

    @Bean("callerRunsThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor callerRunsThreadPoolTaskExecutor() {
        log.info("创建线程池");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        //设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //设置队列数
        executor.setQueueCapacity(queueCapacity);
        //设置线程名称前缀
        executor.setThreadNamePrefix("MyThreadPoolExecutor----------------------->");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // 设置拒绝策略. AbortPolicy()-当工作队列已满,线程数为最大线程数的时候,接收新任务抛出RejectedExecutionException异常
        //CallerRunsPolicy()-线程不够用时由调用的线程处理该任务(不在新线程中执行任务，而是有调用者所在的线程来执行)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 执行初始化
        executor.initialize();
        return executor;

    }

}