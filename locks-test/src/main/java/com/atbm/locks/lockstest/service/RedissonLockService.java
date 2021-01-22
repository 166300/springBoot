package com.atbm.locks.lockstest.service;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedissonLockService {

    @Autowired
    RedissonClient redissonClient;

    private String hello="hello";

    public void lock() throws InterruptedException{
        RLock lock = redissonClient.getLock("lock");
        //lock.lock();阻塞的 存在锁就不能运行 会等待 线程内任意期间要获取这把锁 都是直接使用的
        //lock.tryLock();非阻塞的 判断是否有锁 有返回fales则不再尝试
        //boolean b = lock.tryLock(100, 10, TimeUnit.SECONDS);等待100s内能获取到锁 这个锁10s就失效

        lock.lock();
    }

    public void unlock() {
        RLock lock = redissonClient.getLock("lock");
        lock.unlock();
    }
//=======================读写锁============================
/*
* 写锁:独占锁
* 读锁:共享锁,有写锁读锁不能运行
* */

/*
*
* 1,两个服务及以上服务操作相同数据,如果涉及读写
* 读加读锁,写加写锁
*
*
* */
    public String read() {
        RReadWriteLock helloValue = redissonClient.getReadWriteLock("helloValue");
        RLock readLock = helloValue.readLock();
        readLock.lock();
        //读数据
        String a=hello;
        readLock.unlock();
        return a;
    }
    public String write() {
        RReadWriteLock helloValue = redissonClient.getReadWriteLock("helloValue");
        RLock writeLock = helloValue.writeLock();
        writeLock.lock();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        //写数据
        hello = UUID.randomUUID().toString();
        writeLock.unlock();
        return hello;
    }
//=======================信号量============================
    //车位满就锁
    public Boolean park() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("car");
        semaphore.acquire();
        return true;
    }
    //释放一个车位
    public Boolean rpark() {
        RSemaphore semaphore = redissonClient.getSemaphore("car");
        semaphore.release();
        return true;
    }
//=============================锁门==============================
    //走一个人
    public Boolean gogogo() {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("go");
        downLatch.countDown();
        System.out.println("溜啦...");
        return true;
    }

    public Boolean suomen() throws InterruptedException {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("go");
        downLatch.trySetCount(10);//默认10个人

        downLatch.await();//等人都走完
        System.out.println("要锁门...");
        return true;
    }
}
