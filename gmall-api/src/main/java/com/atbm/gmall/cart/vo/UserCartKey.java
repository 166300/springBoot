package com.atbm.gmall.cart.vo;

import lombok.Data;

@Data
public class UserCartKey {
    private Boolean login;//用户是否登录
    private Long userId;//用户登陆的Id
    private String tempCartkey;//用户没有登录的临时购物车

    private String finalCartKey;//用户最终用的购物车
}
