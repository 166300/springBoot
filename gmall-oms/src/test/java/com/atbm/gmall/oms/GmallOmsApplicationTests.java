package com.atbm.gmall.oms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

//@SpringBootTest
class GmallOmsApplicationTests {

    @Test
    void contextLoads() {
//        int i = new BigDecimal("10.00").compareTo(new BigDecimal("11.00"));
//        System.out.println(i);
        String a = "aaa";
        String[] split = a.split("-");
        System.out.println(split);
    }

}
