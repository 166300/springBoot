package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.ProductAttribute;
import com.atbm.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品属性参数表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductAttributeService extends IService<ProductAttribute> {
    /*
    * 查询某个属性分类下的所有属性
    * */
    PageInfoVo getCategoryAttributes(Long cid, Integer type, Integer pageSize, Integer pageNum);
}
