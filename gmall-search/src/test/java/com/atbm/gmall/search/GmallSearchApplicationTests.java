package com.atbm.gmall.search;

import com.atbm.gmall.vo.search.SearchParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.security.RunAs;
import java.io.IOException;
import java.util.Arrays;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    JestClient jestClient;

    @Autowired
    SearchProductService searchProductService;


    @Test
    public void dslTest() throws IOException {
        //查询-->索引名字
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("手机");

        //过滤-->品牌
        String[] brand = new String[]{"苹果"};
        searchParam.setBrand(brand);

        //分类ID
        String[] cate = new String[]{"19","20"};
        searchParam.setCatelog3(cate);

        //价格区间
        searchParam.setPriceFrom(5000);
        searchParam.setPriceTo(10000);

        //属性ID
        String[] props = new String[]{"45:4.7","46:4G"};
        searchParam.setProps(props);



        searchProductService.searchProduct(searchParam);
    }

    @Test
    void contextLoads() throws IOException {
        Search info = new Search.Builder("").addIndex("product").addType("info").build();
        SearchResult execute = jestClient.execute(info);
        System.out.println(execute.getTotal());
    }
    @Test
    void searchSourceBuilder() throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());

        builder.query(boolQueryBuilder);

        String s = builder.toString();
        System.out.println(s);
    }


}
