package com.atbm.gmall.oms.service.impl;

import com.atbm.gmall.oms.entity.CartItem;
import com.atbm.gmall.oms.mapper.CartItemMapper;
import com.atbm.gmall.oms.service.CartItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

}
