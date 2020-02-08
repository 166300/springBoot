package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.ProductCategory;
import com.atbm.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 产品分类 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductCategoryService extends IService<ProductCategory> {
    /*
    * 查询菜单以及子菜单
    * */
    List<PmsProductCategoryWithChildrenItem> listCatelogWithChilder(Integer i);
}
