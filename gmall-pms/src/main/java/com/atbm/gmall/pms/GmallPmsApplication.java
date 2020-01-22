package com.atbm.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/*
* 1、配置dubbo
* 2、mybatis plus
*   logstash整合
*   1、依赖
*   2、日志配置文件
*   3、在kibana建立日志索引
* */
@EnableDubbo
@MapperScan(basePackages = "com.atbm.gmall.pms.mapper")
@SpringBootApplication
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
