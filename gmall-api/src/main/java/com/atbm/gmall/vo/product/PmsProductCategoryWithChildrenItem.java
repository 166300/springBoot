package com.atbm.gmall.vo.product;


import com.atbm.gmall.pms.entity.ProductCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 */
@Data
public class PmsProductCategoryWithChildrenItem extends ProductCategory  implements Serializable {
    private List<ProductCategory> children;

}
