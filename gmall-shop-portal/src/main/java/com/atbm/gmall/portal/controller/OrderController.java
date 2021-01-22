package com.atbm.gmall.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.oms.service.OrderService;
import com.atbm.gmall.pms.entity.Comment;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.ums.entity.Member;
import com.atbm.gmall.vo.order.OrderConfirmVo;
import com.atbm.gmall.vo.order.OrderCreateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Api(tags = "订单系统")
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    OrderService orderService;

    /*
    * 结算
    *   信息确认完成下一步要提交订单,我们下一步必须要防重复验证[接口的幂等性设置]
    *   1). 利用UUID生成一个唯一的字符串 防止重复
    *   接口幂等性设计:
    *       select;不需要
    *       insert/delete/update;需要
    *   2). 数据库层面->锁机制保证多次请求幂等
    *       insert();[ID不是自增传入ID相同就不添加]
    *       delete();[传入ID删除,只能删除一次]
    *       update();[乐观锁] update set kucun=kucun-1,version=version+1 where skuId=1 and version=1
    *   3). 业务层面;
    *       分布式锁-+>[令牌放重]. order:member:用户ID
    *       分布式锁防止并发下单
    *
    * */
    @ApiOperation("订单确认")
    @PostMapping("/confirm")
    public CommonResult confirmOrder(@RequestParam("accessToken") String accessToken){
        //检查用户是否存在
        String memberJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEBER + accessToken);
        if(StringUtils.isEmpty(accessToken)||StringUtils.isEmpty(memberJson)){
            //未登录
            CommonResult failed = new CommonResult().failed();
            failed.setMessage("用户未登录");
            return failed;
        }
        //登录的用户
        Member member = JSON.parseObject(memberJson, Member.class);
        /*
        * 返回如下数据
        *   当前用户的可选地址列表
        *   当前购物车选中的商品信息
        *   可有的优惠信息
        *   支付,配送,发票信息
        * */
        //dubbo的rpc隐式传参.setAttachment保存下一个远程服务需要的参数
        //dubbo标签的所有属性关键字都是占有的不能传参asseccTkoen
        RpcContext.getContext().setAttachment("accessToken",accessToken);
        //下一个远程服务
        OrderConfirmVo orderConfirm = orderService.orderConfirm(member.getId());
        return new CommonResult().success(orderConfirm);
    }
    /*
    * 下单
    *   创建订单必须用确认订单的那些数据
    * */
    @ApiOperation("下单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "totalPrice",value = "商品价格"),
            @ApiImplicitParam(name = "accessToken",value = "登录令牌"),
            @ApiImplicitParam(name = "addressId",value = "地址ID"),
            @ApiImplicitParam(name = "note",value = "订单备注"),
            @ApiImplicitParam(name = "orderToken",value = "防止重复提交的交易令牌")
    })
    @PostMapping("/create")
    public CommonResult createOrder(@RequestParam(value = "totalPrice") BigDecimal totalPrice,
                              @RequestParam(value = "accessToken") String accessToken,
                              @RequestParam(value = "addressId")Long addressId,
                              @RequestParam(value = "note",required = false)String note,
                              @RequestParam(value = "orderToken") String orderToken){
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        //防止重复提交
        RpcContext.getContext().setAttachment("orderToken", orderToken);

        //创建订单要生成订单(总额)和订单项(购物车里勾选的商品)
        OrderCreateVo orderCreateVo = orderService.createOrder(totalPrice,addressId,note);
        //传入错误的值->有内容就是失败
        if(!StringUtils.isEmpty(orderCreateVo.getToken())){
            CommonResult result = new CommonResult().failed();;
            result.setMessage(orderCreateVo.getToken());
            return result;
        }
        return new CommonResult().success(orderCreateVo);
    }
    /*
    * 去支付
    * produces = {"text/html"}返回值内容类型
    * */
    @ResponseBody
    @PostMapping(value = "/pay",produces = {"text/html"})
    public String pay(@RequestParam(value = "orderSn") String orderSn,
                      @RequestParam(value = "accessToken") String accessToken){
        String string = orderService.pay(orderSn,accessToken);
        return string;
    }

    /*
    * 接受支付宝异步通知
    * */
    @ResponseBody
    @RequestMapping("/pay/async/success")
    public String paySuccess(HttpServletRequest request) throws UnsupportedEncodingException {
        log.debug("支付宝支付异步通知进来....");
        // 修改订单的状态
        // 支付宝收到了success说明处理完成，不会再通知
        //封装支付宝
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        log.debug("订单[{}]支付宝支付异步通知进来....",params.get("out_trade_no"));
        //检查最终支付结果->像支付宝发送成功success->证明成功支付宝收到后就不再发送请求
        String resutl = orderService.resolvePayResult(params);
    return resutl;

    }


}
