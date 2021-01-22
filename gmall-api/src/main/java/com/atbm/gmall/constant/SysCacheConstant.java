package com.atbm.gmall.constant;
//系统中使用的常量
public class SysCacheConstant {
    //系统菜单缓存
    public static final String CATEGORY_MENU_CACHE_KEY = "sys_category";
    //登录的用户 令牌:login:member:token={userObj}
    public static final String LOGIN_MEBER = "login:member:";
    //登陆令牌过期时间
    public static final Long LOGIN_MEBER_TIMEOUT = 30L;
    //订单唯一检查防重令牌
    public static final String ORDER_UNIQUE_TOKEN = "order:unique:token";

}
