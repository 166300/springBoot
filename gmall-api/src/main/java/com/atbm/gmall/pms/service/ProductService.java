package com.atbm.gmall.pms.service;

import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.product.PmsProductParam;
import com.atbm.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 *
 * 根据条件返回分页数据
 *
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductService extends IService<Product> {
    /*
    * 单查商品详情
    * */
    Product productInfo(Long id);
    /*
    * 全查
    * */
    PageInfoVo productPageInfo(PmsProductQueryParam productQueryParam);
    /*
    * 保存商品数据
    *
    * */
    void saveproduct(PmsProductParam productParam);
    /*
    * 批量上下架
    * */
    void updatePublishStatus(List<Long> ids, Integer publishStatus);
}
