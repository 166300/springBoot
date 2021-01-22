package com.atbm.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.pms.service.ProductService;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.to.es.EsProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


/*
* 商品详情
* */
@RestController
public class ProductItemController {

    @Reference
    ProductService productService;

    //注入mainThreadPoolExecutor
    @Qualifier("mainThreadPoolExecutor")
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    /*
    * 数据库(商品基本信息表,商品属性表,商品促销表)和 es (info/attr/sale)
    * 商品请求异步化:
    * */
    @GetMapping("/item/{id}.html")
    public CommonResult productInfo2(@PathVariable("id") Long id){
        //1.商品基本数据(名字介绍)
        //创建异步线程supplyAsync()->提交给threadPoolExecutor线程池->
        // 执行结束return product;->whenComplete()处理返回结果,执行时的异常->get()得到执行后的结果
        CompletableFuture<Product> productCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Product product = null;
            product = productService.productInfo2(id);
            return product;
        }, threadPoolExecutor).whenComplete((r, e) -> {
            System.out.println("处理结果" + r);
            System.out.println("处理异常" + e);
        });
        Product product = null;
        try {
            product = productCompletableFuture.get();
        } catch (Exception e) {
        }
        //2.商品属性数据
        //3.商品营销数据
        //4.商品配送数据
        //5.商品增值服务数据
        return new CommonResult().success(product);
    }



    //通过商品ID查商品
    public CommonResult productInFo(@PathVariable("id") Long id){
        EsProduct esProduct=productService.productAllInfo(id);
        return new CommonResult().success(esProduct);
    }

    //通过skuID查商品
    @GetMapping("/item/sku{id}.html")
    public CommonResult productSkuInFo(@PathVariable("id") Long id){
        EsProduct esProduct=productService.productSkuInFo(id);
        return new CommonResult().success(esProduct);
    }







}













