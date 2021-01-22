package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.ProductCategory;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.product.PmsProductCategoryParam;
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

    ProductCategory getItem(Long id);

    PageInfoVo getList(Long parentId, Integer pageSize, Integer pageNum);

    void create(PmsProductCategoryParam productCategoryParam);

    void delete(Long id);

    void update(Long id, PmsProductCategoryParam productCategoryParam);

    void updateShowStatus(List<Long> ids, Integer showStatus);

    void updateNavStatus(List<Long> ids, Integer navStatus);
}
