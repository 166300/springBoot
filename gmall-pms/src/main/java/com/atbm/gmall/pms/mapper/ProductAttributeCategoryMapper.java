package com.atbm.gmall.pms.mapper;

import com.atbm.gmall.pms.entity.ProductAttributeCategory;
import com.atbm.gmall.vo.product.PmsProductAttributeCategoryItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 产品属性分类表 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductAttributeCategoryMapper extends BaseMapper<ProductAttributeCategory> {
    List<PmsProductAttributeCategoryItem> listProductAttributeCategoryItem(Integer i);
    List<PmsProductAttributeCategoryItem> listItem(Integer i);
}
