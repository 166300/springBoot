package com.atbm.gmall.cart.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class CartResponse implements Serializable {

    private Cart cart;//整个购物车

    private CartItem cartItem;//某个购物项

    private String cartKey;//生成
}
