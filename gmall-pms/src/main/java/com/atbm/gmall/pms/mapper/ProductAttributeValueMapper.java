package com.atbm.gmall.pms.mapper;

import com.atbm.gmall.pms.entity.ProductAttribute;
import com.atbm.gmall.pms.entity.ProductAttributeValue;
import com.atbm.gmall.to.es.EsProductAttributeValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 存储产品参数信息的表 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface ProductAttributeValueMapper extends BaseMapper<ProductAttributeValue> {

    List<EsProductAttributeValue> selectProdectBaseAttrAndValue(Long id);

    List<ProductAttribute> selectProdectSaleAttrName(Long id);
}
