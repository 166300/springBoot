package com.atbm.gmall.pms;

import com.atbm.gmall.pms.entity.Brand;
import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.pms.service.BrandService;
import com.atbm.gmall.pms.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@Slf4j
@SpringBootTest
class GmallPmsApplicationTests {
    @Autowired
    ProductService productService;
    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
/*        Product byId = productService.getById(1);
        System.out.println(byId.getName());*/
        //测试主从库分工
        //更新
        Brand brand = new Brand();
        brand.setName("哈哈");
        brandService.save(brand);
        System.out.println("OK");

/*        //查询
        Brand byId = brandService.getById(53);
        System.out.println(byId);*/
    }

}
