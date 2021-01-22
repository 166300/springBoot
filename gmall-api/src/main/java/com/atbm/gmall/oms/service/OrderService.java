package com.atbm.gmall.oms.service;

import com.atbm.gmall.oms.entity.Order;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.order.OrderConfirmVo;
import com.atbm.gmall.vo.order.OrderCreateVo;
import com.atbm.gmall.vo.order.OrderSelect;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
public interface OrderService extends IService<Order> {

    OrderConfirmVo orderConfirm(Long id);

    OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId, String note);

    String pay(String orderSn, String accessToken);

    String resolvePayResult(Map<String, String> params);

    PageInfoVo orderList(OrderSelect orderSelect);

    Order getOrderItem(Long id);
}
