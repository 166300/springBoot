package com.atbm.gmall.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/*
* 数组类型数据提交需要同名属性多几个值
*   如:http://localhost:8080/search?catelog3=19&catelog3=20&catelog3=30
* */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GmallShopPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallShopPortalApplication.class, args);
    }

}
