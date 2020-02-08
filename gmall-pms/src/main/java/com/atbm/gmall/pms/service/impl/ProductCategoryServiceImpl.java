package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.pms.entity.ProductCategory;
import com.atbm.gmall.pms.mapper.ProductCategoryMapper;
import com.atbm.gmall.pms.service.ProductCategoryService;
import com.atbm.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    ProductCategoryMapper productCategoryMapper;

    @Autowired
    RedisTemplate<Object,Object> redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listCatelogWithChilder(Integer i) {
        Object cachMen = redisTemplate.opsForValue().get(SysCacheConstant.CATEGORY_MENU_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;
        if(cachMen!=null){
            //缓存中存在
            log.debug("菜单数据");
            items= (List<PmsProductCategoryWithChildrenItem>) cachMen;
        }else{
            items = productCategoryMapper.listCatelogWithChilder(i);
            //放到缓存中，redis;
            redisTemplate.opsForValue().set(SysCacheConstant.CATEGORY_MENU_CACHE_KEY,items);
        }
        return items;
    }
}
