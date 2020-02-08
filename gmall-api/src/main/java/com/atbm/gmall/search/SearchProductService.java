package com.atbm.gmall.search;

import com.atbm.gmall.vo.search.SearchParam;
import com.atbm.gmall.vo.search.SearchResponse;

/*
* 商品检索服务
* */
public interface SearchProductService {
    SearchResponse searchProduct(SearchParam searchParam);
}
