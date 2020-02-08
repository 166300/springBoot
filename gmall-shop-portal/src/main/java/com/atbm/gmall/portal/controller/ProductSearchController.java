package com.atbm.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.search.SearchProductService;
import com.atbm.gmall.vo.search.SearchParam;
import com.atbm.gmall.vo.search.SearchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/*
* 商品检索controller
* */
@Api(tags = "检索功能")
@RestController
public class ProductSearchController {

    @Reference
    SearchProductService searchProductService;

    /*
    * 检索商品
    * */
    @ApiOperation("商品检索")
    @GetMapping("/search")
    public SearchResponse productsearchResponse(SearchParam searchParam){
        SearchResponse searchResponse = searchProductService.searchProduct(searchParam);
        return searchResponse;
    }

}
