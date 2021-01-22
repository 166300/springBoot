package com.atbm.gmall.vo.order;


import com.atbm.gmall.cart.vo.CartItem;
import com.atbm.gmall.sms.entity.Coupon;
import com.atbm.gmall.ums.entity.MemberReceiveAddress;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo implements Serializable {

    //购物项
    List<CartItem> items;
    //地址列表
    List<MemberReceiveAddress> addresses;
    //优惠卷
    List<Coupon> coupons;
    //支付,配送,发票

    //订单令牌,这个令牌下一步提交必须带上
    private String orderToken;
    //商品总额
    private BigDecimal productTotalPrice = new BigDecimal("0");
    //订单总额
    private BigDecimal totalPrice = new BigDecimal("0");
    //商品总数
    private Integer count = 0;
    //优惠卷减免
    private BigDecimal couponPrice = new BigDecimal("0");
    //运费
    private BigDecimal transPrice = new BigDecimal("10");

}
