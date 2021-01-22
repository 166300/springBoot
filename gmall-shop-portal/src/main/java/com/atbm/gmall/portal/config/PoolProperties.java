package com.atbm.gmall.portal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
//绑定配置文件中gmall.pool前缀的属性配置
@ConfigurationProperties(prefix = "gmall.pool")
public class PoolProperties {

    private Integer coreSize;
    private Integer maximumPoolSize;
    private Integer queueSize;


}
