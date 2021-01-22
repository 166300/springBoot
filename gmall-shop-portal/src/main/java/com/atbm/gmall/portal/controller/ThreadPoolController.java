package com.atbm.gmall.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolController {
/*
* 线程池
* */
    @Qualifier("mainThreadPoolExecutor")
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/thread/staus")
    public Map threadPoolStatue(){
        Map<String,Object> map=new HashMap<>();
        map.put("ActiveCount",threadPoolExecutor.getActiveCount());
        map.put("CorePoolSize",threadPoolExecutor.getCorePoolSize());
        return map;
    }
}
