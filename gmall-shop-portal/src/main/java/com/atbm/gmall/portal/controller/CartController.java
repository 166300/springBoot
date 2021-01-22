package com.atbm.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.cart.service.CartService;
import com.atbm.gmall.cart.vo.CartItem;
import com.atbm.gmall.cart.vo.CartResponse;
import com.atbm.gmall.to.CommonResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/*
* 购物车
* */
@RequestMapping("/cart")
@RestController
public class CartController {

    @Reference
    CartService cartService;
    /*
    * 返回添加的购物项的详细信息
    * */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId",value = "商品的skuId"),
            @ApiImplicitParam(name = "num",defaultValue = "1",value = "商品数量"),
            @ApiImplicitParam(name = "cartKey",value = "离线购物车,可以没有"),
            @ApiImplicitParam(name = "accessToken",value = "登陆后的访问令牌,可以没有")
    })
    public CommonResult addToCart(@RequestParam("skuId") Long skuId,
                                  @RequestParam(value = "num",required = false,defaultValue = "1") Integer num,
                                  @RequestParam(value = "cartKey",required = false) String cartKey,
                                  @RequestParam(value = "accessToken",required = false) String accessToken) throws ExecutionException, InterruptedException {
        System.out.println("OK+++++++++++++++++++++++++++++++++++++"+cartKey);
        CartResponse cartResponse = cartService.addToCart(skuId, num, cartKey, accessToken);

        return new CommonResult().success(cartResponse);
    }

    /*
    * 修改购物项数量
    * */
    @PostMapping("/update")
    public CommonResult updateCartItemNum(@RequestParam("skuId") Long skuId,
                                  @RequestParam(value = "num",required = false,defaultValue = "1") Integer num,
                                  @RequestParam(value = "cartKey",required = false) String cartKey,
                                  @RequestParam(value = "accessToken",required = false) String accessToken) throws ExecutionException, InterruptedException {
        CartResponse cartResponse=cartService.updateCartItemNum(skuId,num,cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }
    /*
    * 查看我的购物车
    * */
    @PostMapping("/list")
    public CommonResult cartList(@RequestParam(value = "cartKey",required = false) String cartKey,
                                 @RequestParam(value = "accessToken",required = false) String accessToken){
         CartResponse cartResponse=cartService.cartList(cartKey,accessToken);
         return new CommonResult().success(cartResponse);
    }
    /*
     * 删除商品
     * */
    @DeleteMapping("/del")
    public CommonResult delCart(@RequestParam(value = "skuId",defaultValue ="0") Long skuId,
                                @RequestParam(value = "cartKey",required = false) String cartKey,
                                @RequestParam(value = "accessToken",required = false) String accessToken){
        System.out.println("OK+++++++++++++++++++++++++++++++++++++"+skuId);
        CartResponse cartResponse=cartService.delCart(skuId,cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }
    /*
     * 清空购物车
     * */
    @GetMapping("/clear")
    public CommonResult clearCart(@RequestParam(value = "cartKey",required = false) String cartKey,
                                @RequestParam(value = "accessToken",required = false) String accessToken){
        CartResponse cartResponse=cartService.clearCart(cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }
    /*
    * 是否选中
    * */
    @ApiOperation("选中状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuIds",value = "操作所有商品的skuId集合多个值用','隔开" ),
            @ApiImplicitParam(name = "ops",value = "1->选中,0->未选中" ),
            @ApiImplicitParam(name = "cartKey",value = "离线购物车,可以没有"),
            @ApiImplicitParam(name = "accessToken",value = "登陆后的访问令牌,可以没有")
    })
    @PostMapping("/check")
    public CommonResult cartCheckItems(@RequestParam(value = "skuIds") String skuIds,
                                  @RequestParam(value = "ops",defaultValue = "1") Integer ops,
                                  @RequestParam(value = "cartKey",required = false) String cartKey,
                                  @RequestParam(value = "accessToken",required = false) String accessToken){
        CartResponse cartResponse=cartService.cartCheckItems(skuIds,ops,cartKey,accessToken);
        return new CommonResult().success(cartResponse);
    }
}
