package com.atbm.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.pms.entity.ProductAttributeCategory;
import com.atbm.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.atbm.gmall.pms.service.ProductAttributeCategoryService;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.product.PmsProductAttributeCategoryItem;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
@Component
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {

    @Autowired
    ProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Override
    public PageInfoVo productAttributeCategoryPageInfo(Integer pageNum, Integer pageSize) {
        IPage<ProductAttributeCategory> page = productAttributeCategoryMapper.selectPage(new Page<ProductAttributeCategory>(pageNum, pageSize), null);
        //返回分页数据对象
        return PageInfoVo.getVo(page,pageSize.longValue());
    }

    @Override
    public List<PmsProductAttributeCategoryItem> getListWithAttr(int i) {
        List<PmsProductAttributeCategoryItem> items = productAttributeCategoryMapper.listProductAttributeCategoryItem(i);
        List<PmsProductAttributeCategoryItem> items1 = productAttributeCategoryMapper.listItem(i);
        System.out.println("OK+++++++++++++++++++++++++"+items);
        System.out.println("OK+++++++++++++++++++++++++"+items1);
        return items1;
    }
}
