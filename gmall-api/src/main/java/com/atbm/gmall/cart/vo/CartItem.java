package com.atbm.gmall.cart.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/*
* 购物项
* */
@Setter
@ToString
public class CartItem implements Serializable {
    //当前购物项的基本信息
    //商品名
    @Getter
    private String name;
    //skuID
    @Getter
    private Long skuId;
    //sku编码
    @Getter
    private String skuCode;
    //sku库存
    @Getter
    private Integer stok;
    //销售属性
    @Getter
    private String sp1;
    @Getter
    private String sp2;
    @Getter
    private String sp3;
    //sku图片
    @Getter
    private String pic;
    //sku价格
    @Getter
    private BigDecimal price;
    //促销价格
    @Getter
    private BigDecimal promotionPrice;

    //以上是购物项的基本信息
    //购买的数量
    @Getter
    private Integer count;
    //购物项的选中状态
    @Getter
    private boolean check = true;
    //总价格
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        BigDecimal bigDecimal = price.multiply(new BigDecimal(count.toString()));
        return bigDecimal;
    }
}
