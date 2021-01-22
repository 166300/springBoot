package com.atbm.locks.lockstest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

/*
*
* 以后jedis配置
*   在application.properties配置所有redis配置相关信息
*
* */

@SpringBootApplication
public class LocksTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocksTestApplication.class, args);
    }

}
