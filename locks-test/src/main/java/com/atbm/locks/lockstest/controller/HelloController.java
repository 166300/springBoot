package com.atbm.locks.lockstest.controller;

import com.atbm.locks.lockstest.service.RedisIncrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    RedisIncrService redisIncrService;

    @GetMapping("/incr")
    public String incr(){
        redisIncrService.incr();
        return "ok";
    }
    @GetMapping("/incr2")
    public String incr2(){
        redisIncrService.incrDistribute();
        return "ok";
    }
    @GetMapping("/incr3")
    public String incr3(){
        redisIncrService.useRedissonForLock();
        return "ok";
    }
}
