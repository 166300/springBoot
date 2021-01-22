package com.atbm.gmall.vo.product;

import com.atbm.gmall.pms.entity.ProductAttribute;
import com.atbm.gmall.pms.entity.ProductAttributeCategory;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 包含有分类下属性的vo
 */
@ToString
@Data
public class PmsProductAttributeCategoryItem extends ProductAttributeCategory implements Serializable {
    private List<ProductAttribute> productAttributeList;


}
