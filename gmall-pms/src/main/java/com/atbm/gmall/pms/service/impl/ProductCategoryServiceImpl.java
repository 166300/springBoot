package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.pms.entity.ProductCategory;
import com.atbm.gmall.pms.mapper.ProductCategoryMapper;
import com.atbm.gmall.pms.service.ProductCategoryService;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.product.PmsProductCategoryParam;
import com.atbm.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    @Override
    public ProductCategory getItem(Long id) {
        ProductCategory productCategory = productCategoryMapper.selectById(id);
        return productCategory;
    }
    /*
    * 分类全查
    * */
    @Override
    public PageInfoVo getList(Long parentId, Integer pageSize, Integer pageNum) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",parentId);
        IPage<ProductCategory> page = productCategoryMapper.selectPage(
                new Page<ProductCategory>(pageNum,pageSize), wrapper);
        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),pageSize.longValue(),
                page.getRecords(),page.getCurrent());

        return pageInfoVo;
    }
    /*
    * 添加分类
    * */
    @Override
    public void create(PmsProductCategoryParam productCategoryParam) {

        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryParam,productCategory);
        productCategory.setLevel(0);
        productCategory.setProductCount(100);
        productCategoryMapper.insert(productCategory);

    }
    /*
    * 删除分类
    * */
    @Override
    public void delete(Long id) {
        System.out.println(id+"aaaaaaaaaaaaaaa");
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        productCategoryMapper.delete(wrapper.eq("id",id));
    }
    /*
    * 修改分类
    * */
    @Override
    public void update(Long id, PmsProductCategoryParam productCategoryParam) {
        ProductCategory productCategory = new ProductCategory();
        BeanUtils.copyProperties(productCategoryParam,productCategory);
        productCategory.setId(id);
        productCategoryMapper.updateById(productCategory);
    }
    /*
    * 显示
    * */
    @Override
    public void updateShowStatus(List<Long> ids, Integer showStatus) {
        System.out.println("OK++++++++showStatus+++++++"+showStatus);
        ids.forEach((id)->{
            productCategoryMapper.updateById(new ProductCategory().setShowStatus(showStatus).setId(id));
        });
    }
    /*
    * 导航栏
    * */
    @Override
    public void updateNavStatus(List<Long> ids, Integer navStatus) {
        System.out.println("OK++++++++navStatus+++++++"+navStatus);
        ids.forEach((id)->{
            productCategoryMapper.updateById(new ProductCategory().setNavStatus(navStatus).setId(id));
        });
    }
}
