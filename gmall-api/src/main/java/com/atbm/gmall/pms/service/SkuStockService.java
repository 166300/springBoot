package com.atbm.gmall.pms.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.pms.entity.SkuStock;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * <p>
 * sku的库存 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */

public interface SkuStockService extends IService<SkuStock> {

    BigDecimal getSkuPriceBySkuId(Long skuId);
}
