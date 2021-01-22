package com.atbm.locks.lockstest;

import com.atbm.locks.lockstest.service.RedisIncrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootTest
class LocksTestApplicationTests {
    //@Autowired
    JedisPool jedisPool;

    @Autowired
    RedisIncrService redisIncrService;

    @Test
    void contextLoads() {

/*        System.out.println(jedisPool);
        Jedis jedis = jedisPool.getResource();
        jedis.set("hello","hello");
        System.out.println(jedis.get("hello"));*/
        redisIncrService.useRedissonForLock();
    }

}
