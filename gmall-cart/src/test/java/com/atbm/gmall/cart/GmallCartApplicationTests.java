package com.atbm.gmall.cart;

import com.alibaba.fastjson.JSON;
import com.atbm.gmall.cart.vo.Cart;
import com.atbm.gmall.cart.vo.CartItem;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;

@SpringBootTest
class GmallCartApplicationTests {

    @Autowired
    RedissonClient redissonClient;
    @Test
    void userRedissonMap() {
        RMap<String, String> cart = redissonClient.getMap("cart");
        CartItem item = new CartItem();
        item.setPrice(new BigDecimal("12.98"));
        item.setSkuId(1L);
        item.setCount(1);
        String s = JSON.toJSONString(item);
        cart.put("2",s);
    }

    @Test
    void getMap() {
        RMap<String, String> cart = redissonClient.getMap("cart");
        cart.remove("2");
        String s = cart.get("2");
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        System.out.println(cartItem.getSkuId());
    }




    //测试商品返回计算数据
    @Test
    void contextLoads() {
        CartItem cartItem = new CartItem();
        cartItem.setCount(2);cartItem.setPrice(new BigDecimal("10.98"));
        CartItem cartItem2 = new CartItem();
        cartItem2.setCount(1);cartItem2.setPrice(new BigDecimal("11.3"));
        Cart cart = new Cart();
        cart.setCartItems(Arrays.asList(cartItem,cartItem2));
        System.out.println(cart);
        System.out.println(cart.getCount());
        System.out.println(cart.getTotalPrice());
    }

}
