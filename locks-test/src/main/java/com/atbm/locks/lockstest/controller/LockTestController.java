package com.atbm.locks.lockstest.controller;

import com.atbm.locks.lockstest.service.RedissonLockService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LockTestController {

    @Autowired
    RedissonLockService redissonLockService;

    @GetMapping("/go")
    public Boolean gogogo() throws InterruptedException {
        return redissonLockService.gogogo();
    }
    @GetMapping("/suomen")
    public String suomen() throws InterruptedException {
        Boolean suomen = redissonLockService.suomen();
        return suomen?"锁门":"没锁";
    }



    @GetMapping("/rpark")
    public Boolean rpark() throws InterruptedException {
        return redissonLockService.rpark();
    }
    @GetMapping("/park")
    public Boolean park() throws InterruptedException {
        return redissonLockService.park();
    }


    @GetMapping("/read")
    public String read(){
        return redissonLockService.read();
    }
    @GetMapping("/write")
    public String write(){
        return redissonLockService.write();
    }


    @GetMapping("/lock")
    public String lock() throws InterruptedException {

        redissonLockService.lock();
        return "ok";
    }
    @GetMapping("/unlock")
    public String unlock(){

        redissonLockService.unlock();
        return "ok";
    }
}
