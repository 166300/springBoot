package com.atbm.gmall.cart.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/*
* 购物车
* */
@Setter
@ToString
public class Cart implements Serializable {
    //所有的购物项
    @Getter
    List<CartItem> cartItems;
    //商品总数
    private Integer count;
    //已选中的总价格
    private BigDecimal totalPrice;

    //购物车数据返回前端


    public Integer getCount() {
        //原子级别
        if(cartItems!=null){
            AtomicInteger integer=new AtomicInteger(0);
            cartItems.forEach((cartItems)->{
                //累加
                integer.getAndAdd(cartItems.getCount());
            });
            return integer.get();
        }else{
            return 0;
        }
    }

    public BigDecimal getTotalPrice() {
        if(cartItems!=null){
            AtomicReference<BigDecimal> allTotal = new AtomicReference<>(new BigDecimal("0"));
            cartItems.forEach((cartItems)->{
                BigDecimal add = allTotal.get().add(cartItems.getTotalPrice());
                allTotal.set(add);
            });
            return allTotal.get();
        }else{
            return new BigDecimal("0");
        }
    }

}
