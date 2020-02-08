package com.atbm.gmall.oms.service.impl;

import com.atbm.gmall.oms.entity.Order;
import com.atbm.gmall.oms.mapper.OrderMapper;
import com.atbm.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

}
