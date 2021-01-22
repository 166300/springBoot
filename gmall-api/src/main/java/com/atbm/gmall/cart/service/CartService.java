package com.atbm.gmall.cart.service;

import com.atbm.gmall.cart.vo.CartItem;
import com.atbm.gmall.cart.vo.CartResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

/*
* 购物车服务
* */
public interface CartService {
    //添加商品去购物车
    CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException;

    CartResponse updateCartItemNum(Long skuId, Integer num, String cartKey, String accessToken);

    CartResponse cartList(String cartKey, String accessToken);

    CartResponse delCart(Long skuId, String cartKey, String accessToken);

    CartResponse clearCart(String cartKey, String accessToken);

    CartResponse cartCheckItems(String skuIds, Integer ops, String cartKey, String accessToken);

    List<CartItem> getCartItemForOrder(String asseccTkoen);

    void removeCartItem(String accessToken, List<Long> cartSkuIds);
}
