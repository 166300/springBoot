package com.atbm.rabbit.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order implements Serializable {
    private String orderSn;//订单号
    private Long skuId;//购买商品Id
    private Integer num;//购买个数
    private Integer meberId;//购买者Id
}
