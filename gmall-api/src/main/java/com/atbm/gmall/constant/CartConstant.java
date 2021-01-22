package com.atbm.gmall.constant;

public class CartConstant {
    //临时购物车前缀
    public final static String TEMP_CART_KEY_PREFIX = "cart:temp:";//后面加cartKey
    //线上购物车前缀
    public final static String USER_CART_KEY_PREFIX = "cart:user:";//后面加用户ID
    //购物车在redis中存储被选中的key
    public final static String CART_CHECKED_KEY = "checked";//选中状态
}
