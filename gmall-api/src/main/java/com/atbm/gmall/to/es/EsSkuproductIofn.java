package com.atbm.gmall.to.es;

import com.atbm.gmall.pms.entity.SkuStock;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class EsSkuproductIofn extends SkuStock implements Serializable {
    private String skuTitle;//sku特定标题
    /*
    * 每个sku不同属性以及它的值
    *
    * */
    private List<EsProductAttributeValue> attributeValues;
}
