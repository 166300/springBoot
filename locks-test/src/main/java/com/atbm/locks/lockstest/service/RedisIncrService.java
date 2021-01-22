package com.atbm.locks.lockstest.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 分布式情况下
 * */
@Service
public class RedisIncrService {

    private Object obj = new Object();

    @Autowired
    JedisPool jedisPool;

    //对象在这有一个
    ReentrantLock lock = new ReentrantLock();

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate stringRedisTemplate;



    public void useRedissonForLock(){
        //获取一把锁.只要各个代码使用的锁名一样
        RLock lock = redisson.getLock("lock");
        //加锁 3秒后自动解锁
        lock.lock(3,TimeUnit.SECONDS);
            //业务代码
            Jedis jedis = jedisPool.getResource();
            String num = jedis.get("num");
            Integer i = Integer.parseInt(num);
            i=i+1;
            jedis.set("num",i.toString());
            jedis.close();
        //解锁
        lock.unlock();

    }












    /*
    * 用jedis clients
    * */
    public void incrDistribute(){

        //1.占着位置(原子性不可分割)
        //先判断没有,在赋值
        /*
        * //判断如果没有就给 redis 保存值
        * setnx lock hello 操作 只有一个通过
        *
        * 阶段一:
        * //存在原子问题
        * public void hello(){
        *   String lock = getFormRedis("lock");
        *   if(lock == null){
        *       setRedisKey("lock","1");
        *       //执行业务
        *       delRedisKey("lock");
        *   }else{
        *       //等待重试
        *       hello();//自旋
        *   }
        * }
        * //问题:枷锁原子性
        *
        *
        * 阶段二:
        * public void hello(){
        *   Integer lock = setnx("lock","111");//0代表没有保存数据,说明已经有人占,1代表展位成功
        *   if(lock != 0){
        *       //执行业务
        *       delRedisKey("lock");
        *   }else{
        *       //等待重试
        *       hello();//自旋
        *   }
        * }
        * //问题:由于各种问题导致锁没释放 其他人永远获取不到锁
        * //解决 加入过期时间  expire lock 10 十秒过期
        *
        * 阶段三:
         * public void hello(){
         *   Integer lock = setnx("lock","111");//0代表没有保存数据,说明已经有人占,1代表展位成功
         *   if(lock != 0){
         *       expire("lock",10s);
         *       //执行业务
         *       delRedisKey("lock");
         *   }else{
         *       //等待重试
         *       hello();//自旋
         *   }
         * }
        * //问题:由于各种问题导致没来得及设置锁的超时时间
        * //解决 加锁和加超时必须原子性
        * 阶段四:
         * public void hello(){
         *   String result = setnxex("lock","111",10s);//添加锁同时设置超时时间
         *   if(result == "ok"){
         *       expire("lock",10s);
         *       //执行业务
         *       delRedisKey("lock");
         *   }else{
         *       //等待重试
         *       hello();//自旋
         *   }
         * }
        *
        *
        * 阶段五:
         * public void hello(){
         *   String token =UUID;
         *   String result = setnxex("lock",token,10);//添加锁同时设置超时时间
         *   if(result == "ok"){
         *       expire("lock",10s);
         *       //执行业务
         *       //删除,保证删除的是自己锁
         *       if(get("lock")==token){
         *          del("lock");
         *       }
         *       delRedisKey("lock");
         *   }else{
         *       //等待重试
         *       hello();//自旋
         *   }
         * }
        * //问题:准备删除锁之前得到锁时,redis时间过期删除了锁.但是拿到了值对比成功
        *       (下一个线程添加了他自己的锁),还是删除了锁.至少有两个线程进了同一个代码
        * //原因删除锁要判断不是原子性的操作
        * 解决:
        *  String script =
        *       "if redis.call('get', KEYS[1]) == ARGV[1] then
        *               return redis.call('del', KEYS[1])
        *       else
        *               return 0
        *       end";
        *  jedis.eval(script, Collections.singletonList(key), Collections.singletonList(token));
        *
        *   传两个值<key>,<token>
        *   call调用get命令 得到<key>的值是否等于<token> 等于就删除<key>否则就什么都不做
        *
        *
        * lua脚本删除
        *
        * 分布式锁的核心
        *   1).加锁,一定要是原子性的
        *   2).设置超时原子性
        *   3).解锁也要原子
        *
        * 最终分布式锁代码
        * public void hello(){
        *   String token = uuid;
        *   String lock =redis.setnxex("lock",token,10s);
        *   if(lock == "ok"){
        *       //执行业务逻辑
        *       //脚本删锁
        *   }else{
        *       hello();
        *   }
        *
        *
        * }
        * */

        //加锁--->失败
//        String token = UUID.randomUUID().toString();
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
//        if (lock){
//            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
//            String num = stringStringValueOperations.get("num");
//            if (num!=null){
//                Integer i = Integer.parseInt(num);
//                i=i+1;
//                stringStringValueOperations.set("num",i.toString());
//            }
//            //删除锁
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            DefaultRedisScript<String> script1 = new DefaultRedisScript<>();
//            stringRedisTemplate.execute(script1, Arrays.asList("lock"),token);
//
//
//            System.out.println("删除锁OK");
//        }else {
//            incrDistribute();
//        }


        /*
        * 锁得跟多考虑
        *   自旋:
        *       自旋次数
        *       自旋超时
        *   锁设置:
        *       锁粒度:细;记录级别
        *           各自服务各自锁
        *           分析锁得粒度,不要锁住无关数据,一种数据一种锁,一条数据一种锁.
        *   锁类型:
        *       读写锁
        *
        * */


        Jedis jedis = jedisPool.getResource();
        try {
            String token = UUID.randomUUID().toString();
            SetParams nx = SetParams.setParams().ex(3).nx();
            String lock = jedis.set("lock", token, nx);
            if (lock!=null&&lock.equalsIgnoreCase("ok")){
                //ok
                //业务
                String num = jedis.get("num");
                Integer i = Integer.parseInt(num);
                i=i+1;
                jedis.set("num",i.toString());
                //删锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));
                System.out.println("删锁OK...");
            }else{
                try {
                    Thread.sleep(1000);
                    incrDistribute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            jedis.close();
        }


    }



    public void incr(){

        //对象在这有多个
        //ReentrantLock lock = new ReentrantLock();

        lock.lock();

        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String num = stringStringValueOperations.get("num");
        if (num!=null){
            Integer i = Integer.parseInt(num);
            i=i+1;
            stringStringValueOperations.set("num",i.toString());
        }
        lock.unlock();


        //所有人是否使用同一个锁@Service Spring注入组件因为单例
        // synchronized (this) 就一个 所以锁就相同
        //synchronized (stringRedisTemplate) 就一个 所以锁就相同
        //synchronized (new Object()) 不能 创建新的对象不一样
        //synchronized (obj) RedisIncrService对象创建的时候对属性赋值
            // 1).RedisIncrService对象创建一个 只要不对 obj 赋值 obj 就一个(固定的内存地址)
        //synchronized (RedisIncrService.class) RedisIncrService类型只有一个锁得住
//        synchronized (this){
//            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
//            String num = stringStringValueOperations.get("num");
//            if (num!=null){
//                Integer i = Integer.parseInt(num);
//                i=i+1;
//                stringStringValueOperations.set("num",i.toString());
//            }
//        }
    }
/*    //synchronized (obj()) 值没有改变 锁得住
    public Object obj(){
        return obj;
    }*/
    //synchronized (obj())锁不住 Object o 内存不一样
    public Object obj(){
        Object o=new Object();
        BeanUtils.copyProperties(obj,o);
        return o;
    }
}
