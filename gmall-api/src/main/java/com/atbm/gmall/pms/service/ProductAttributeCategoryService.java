package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.ProductAttributeCategory;
import com.atbm.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 产品属性分类表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductAttributeCategoryService extends IService<ProductAttributeCategory> {

    /*
    * 分页查询所有属性分类
    *
    * */

    PageInfoVo productAttributeCategoryPageInfo(Integer pageNum, Integer pageSize);
}
