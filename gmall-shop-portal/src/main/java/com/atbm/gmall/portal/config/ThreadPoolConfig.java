package com.atbm.gmall.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.nio.ch.ThreadPool;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
* 配置当前系统的线程池
* */
@Configuration
public class ThreadPoolConfig {


    /**
     *     public ThreadPoolExecutor(int corePoolSize,线程池的核心大小
     *                               int maximumPoolSize,最大线程数
     *                               long keepAliveTime,线程空闲时间
     *                               TimeUnit unit,空闲时间单位
     *                               BlockingQueue<Runnable> workQueue,工作队列
     *                               ThreadFactory threadFactory,创建线程工厂
     *                               RejectedExecutionHandler handler)线程池的拒绝策略
     */
    //核心业务线程池
    @Bean("mainThreadPoolExecutor")
    public ThreadPoolExecutor mainThreadPoolExecutor(PoolProperties poolProperties){
        LinkedBlockingDeque<Runnable> deque = new LinkedBlockingDeque<>(poolProperties.getCoreSize());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(), 10,
                TimeUnit.MINUTES, deque);
        return threadPoolExecutor;
    }
    //核心业务线程池
    @Bean("otherThreadPoolExecutor")
    public ThreadPoolExecutor otherThreadPoolExecutor(PoolProperties poolProperties){
        LinkedBlockingDeque<Runnable> deque = new LinkedBlockingDeque<>(poolProperties.getCoreSize());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(), 10,
                TimeUnit.MINUTES, deque);
        return threadPoolExecutor;
    }

}
