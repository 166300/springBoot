package com.atbm.gmall.admin.oms;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.oms.entity.Order;
import com.atbm.gmall.oms.service.OrderService;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.order.OrderSelect;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Api(tags = "PmsBrandController", description = "商品订单管理")
@RequestMapping("/order")
public class PmsOrdercontroller {

    @Reference
    private OrderService orderService;

    @ApiOperation("订单查询")
    @GetMapping(value = "/list")
    public Object orderList(OrderSelect orderSelect) {
        PageInfoVo pageInfoVo = orderService.orderList(orderSelect);
        return new CommonResult().success(pageInfoVo);
    }
    @ApiOperation(value = "根据编号查询订单信息")
    @PostMapping(value = "/{id}")
    public Object getOrderItem(@PathVariable("id") Long id) {
        CommonResult commonResult = new CommonResult();
        //TODO 根据编号查询品牌信息

        Order orderItem = orderService.getOrderItem(id);
        return commonResult.success(null);
    }
}
