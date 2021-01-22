package com.atbm.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atbm.gmall.cart.service.CartService;
import com.atbm.gmall.cart.vo.CartItem;
import com.atbm.gmall.constant.OrderStatusEnume;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.oms.component.MemberComponent;
import com.atbm.gmall.oms.config.AlipayConfig;
import com.atbm.gmall.oms.entity.Order;
import com.atbm.gmall.oms.entity.OrderItem;
import com.atbm.gmall.oms.mapper.OrderItemMapper;
import com.atbm.gmall.oms.mapper.OrderMapper;
import com.atbm.gmall.oms.service.OrderService;
import com.atbm.gmall.pms.entity.SkuStock;
import com.atbm.gmall.pms.service.ProductService;
import com.atbm.gmall.pms.service.SkuStockService;
import com.atbm.gmall.to.es.EsProduct;
import com.atbm.gmall.to.es.EsProductAttributeValue;
import com.atbm.gmall.to.es.EsSkuproductIofn;
import com.atbm.gmall.ums.entity.Member;
import com.atbm.gmall.ums.entity.MemberReceiveAddress;
import com.atbm.gmall.ums.service.MemberService;
import com.atbm.gmall.vo.PageInfoVo;
import com.atbm.gmall.vo.order.OrderConfirmVo;
import com.atbm.gmall.vo.order.OrderCreateVo;
import com.atbm.gmall.vo.order.OrderSelect;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Slf4j
@Service
@Component
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Reference
    MemberService memberService;

    @Reference
    CartService cartService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    ProductService productService;

    @Reference
    SkuStockService skuStockService;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    OrderMapper orderMapper;

    ThreadLocal<List<CartItem>> threadLocal =new ThreadLocal<>();//ThreadLocal->同一个线程共享数据

    @Override
    public OrderConfirmVo orderConfirm(Long id) {
        //获取上一步隐式传参带来的asseccTkoen
        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        OrderConfirmVo orderConfirm = new OrderConfirmVo();
        //会员收货地址
        orderConfirm.setAddresses(memberService.getMemberAddresses(id));

        //优惠卷
        orderConfirm.setCoupons(null);
        List<CartItem> artItem = cartService.getCartItemForOrder(accessToken);
        //设置购物项
        orderConfirm.setItems(artItem);
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        //过期时间 System.currentTimeMillis()--系统时间->
        orderToken = orderToken+"_"+System.currentTimeMillis()+"_"+5*60*1000;
        //保存放重令牌
        redisTemplate.opsForSet().add(SysCacheConstant.ORDER_UNIQUE_TOKEN,orderToken);


        //设置订单的防重令牌
        orderConfirm.setOrderToken(orderToken);

        //计算价格
        orderConfirm.setCouponPrice(null);

        //运费是远程计算的
        orderConfirm.setTotalPrice(new BigDecimal("10"));

        artItem.forEach((item)->{
            Integer count = item.getCount();
            //设置总数量
            orderConfirm.setCount(orderConfirm.getCount()+count);
            BigDecimal totalPrice = item.getTotalPrice();
            //商品总价格
            orderConfirm.setProductTotalPrice(orderConfirm.getProductTotalPrice().add(totalPrice));
        });
        orderConfirm.setTotalPrice(orderConfirm.getProductTotalPrice().add(orderConfirm.getTotalPrice()));
        return orderConfirm;
    }


    @Transactional
    @Override
    public OrderCreateVo createOrder(BigDecimal frontPrice, Long addressId, String note) {
        //防止重复
        String orderToken = RpcContext.getContext().getAttachment("orderToken");
        //验证令牌第一种失败
        if(StringUtils.isEmpty(orderToken)){
            //没有orderToken
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("没有orderToken->此次操作出现错误,请重新尝试");
            return orderCreateVo;
        }
        //orderToken = orderToken+"_"+System.currentTimeMillis()+"_"+30*10;
        //验证令牌第二种失败
        String[] split = orderToken.split("_");
        if(split.length != 3){
            //orderToken错误
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("orderToken错误->非法操作,请重新尝试");
            return orderCreateVo;
        }
        //创建时的时间
        long createTime = Long.parseLong(split[1]);
        //超时时间
        long timeOut = Long.parseLong(split[2]);
        //验证令牌第三种失败
        //系统当前时间 减去 创建时的时间 大于或等于 就证明超时
        if(System.currentTimeMillis()-createTime >= timeOut){
            //orderToken超出时间
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("orderToken超出时间->订单超时,请刷新");
            return orderCreateVo;
        }

        //验证重复
        //删除一个值返回0/1操作的个数
        Long remove = redisTemplate.opsForSet().remove(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        if(remove == 0){
            //orderToken令牌已经被移除
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("orderToken令牌已经被移除->创建失败,请刷新重试");
            return orderCreateVo;
        }

        String accessToken = RpcContext.getContext().getAttachment("accessToken");
        Boolean validPrice = validPrice(frontPrice, accessToken, addressId);
        if (!validPrice){
            //限制价格失败
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setLimit(false);
            orderCreateVo.setToken("验证价格失败,请刷新重试");
            return orderCreateVo;
        }
        //获取当前会员信息
        Member member = memberComponent.getMenmberByAccessToken(accessToken);

        OrderCreateVo orderCreateVo = initOrderCreateVo(frontPrice, addressId, accessToken, member);

        Order order = initOrder(frontPrice, addressId, note, member, orderCreateVo);
        //加工处理数据
        //1). 保存订单信息;数据库幂等性; 保存的时候 幂等的字段需要唯一索引
                //设计表->索引->字段->order_sn->unique(唯一)->OK
        //问题: 多次提交会多次添加->解决: 数据库幂等设置索引唯一
        orderMapper.insert(order);

        //2). 保存订单购物项信息
        saveOrderItem(order,accessToken);

        //3). 清除购物车中已经下单的商品->就是被选中的商品


        return orderCreateVo;
    }

    @Override
    public String pay(String orderSn, String accessToken) {
        System.out.println("pay++++++++++++++orderSn++++++++++++"+orderSn);
        System.out.println("pay++++++++++++++accessToken++++++++++++"+accessToken);
        //通过订单号得到订单
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        //通过订单号得到订单内的商品
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_sn", orderSn));
        //第一个商品
        String productName = orderItems.get(0).getProductName();
        StringBuffer body = new StringBuffer();
        for (OrderItem item:orderItems) {
            body.append(item.getProductName()).append("</br>");
        }
        //调用支付宝的支付方法会返回支付页
        //orderSn-订单编号->总价格-order.getTotalAmount()->商品描述-"[杂货铺]-"+productName,->商品名body.toString()
        String result = payOrder(orderSn,order.getTotalAmount().toString(),"[杂货铺]-"+productName,body.toString());

        return null;
    }

    @Override
    public String resolvePayResult(Map<String, String> params) {

        boolean signVerified = true;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset,
                    AlipayConfig.sign_type);
            System.out.println("验签：" + signVerified);

        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
        }
        // 商户订单号
        String out_trade_no = params.get("out_trade_no");
        // 支付宝流水号
        String trade_no = params.get("trade_no");
        // 交易状态
        String trade_status = params.get("trade_status");
        ///只要支付成功 支付宝立即通知 通知数次
        if (trade_status.equals("TRADE_FINISHED")) {
            //改订单状态
            log.debug("订单【{}】,已经完成...不能再退款。数据库都改了",out_trade_no);
        } else if (trade_status.equals("TRADE_SUCCESS")) {
            //改数据库订单状态
            //修改order表内数据PAYED支付状态
            Order order = new Order();
            order.setStatus(OrderStatusEnume.PAYED.getCode());
            orderMapper.update(order,new UpdateWrapper<Order>().eq("order_sn",out_trade_no));
            log.debug("订单【{}】,已经支付成功...可以退款。数据库都改了",out_trade_no);
        }
        return "success";
    }
    /*
    * 后台商品订单查询
    * */
    @Override
    public PageInfoVo orderList(OrderSelect orderSelect) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        //订单号->模糊查
        if(!StringUtils.isEmpty(orderSelect.getOrderSn())){
            wrapper.like("order_sn",orderSelect.getOrderSn());
        }
        //收货人->模糊查
        if(!StringUtils.isEmpty(orderSelect.getReceiverKeyword())){
            wrapper.like("receiver_name",orderSelect.getReceiverKeyword());
        }
        //订单状态
        if(!StringUtils.isEmpty(orderSelect.getStatus())){
            wrapper.eq("status",orderSelect.getStatus());
        }
        //订单分类
        if(!StringUtils.isEmpty(orderSelect.getOrderType())){
            wrapper.eq("order_type",orderSelect.getOrderType());
        }
        //订单来源
        if(!StringUtils.isEmpty(orderSelect.getSourceType())){
            wrapper.eq("source_type",orderSelect.getSourceType());
        }
        //提交时间->模糊查
        if(!StringUtils.isEmpty(orderSelect.getCreateTime())){
            wrapper.like("create_time",orderSelect.getCreateTime());
        }

        IPage<Order> page = orderMapper.selectPage(
                new Page<Order>(orderSelect.getPageNum(),orderSelect.getPageSize()),wrapper);
        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),orderSelect.getPageSize(),
                page.getRecords(),page.getCurrent());
        return pageInfoVo;
    }
    /*
    * 后台ID查询
    * */
    @Override
    public Order getOrderItem(Long id) {
        Order order = orderMapper.selectById(id);
        return order;
    }

    /*
    * 保存订单购物项信息
    * */
    public void saveOrderItem(Order order, String accessToken) {
        //得到下订单的商品ID准备删除
        List<Long> cartSkuIds = new ArrayList<>();

        List<CartItem> cartItems = threadLocal.get();
        List<OrderItem> orderItems = new ArrayList<>();
        cartItems.forEach((cartItem)->{
            //得到所有下订单的ID
            cartSkuIds.add(cartItem.getSkuId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());//订单ID
            orderItem.setOrderSn(order.getOrderSn());//订单号
            //查询sku对应的商品信息
            Long skuId = cartItem.getSkuId();
            //sku信息在 esProduct->SkuproductIofns
            EsProduct esProduct = productService.productSkuInFo(skuId);//从ES中得到商品->ES要上架的商品
            List<EsSkuproductIofn> skuproductIofns = esProduct.getSkuproductIofns();
            SkuStock skuStock = new SkuStock();
            String attrValuejsonString = "";
            //迭代sku信息
            for(EsSkuproductIofn skuproductIofn : skuproductIofns){
                //找到当前sku商品信息
                if(skuId == skuproductIofn.getId()){
                    List<EsProductAttributeValue> values = skuproductIofn.getAttributeValues();
                    attrValuejsonString = JSON.toJSONString(values);
                    BeanUtils.copyProperties(skuproductIofn,skuStock);
                }
            }
            //当前商品的sku信息
            orderItem.setProductId(esProduct.getId());//商品ID
            orderItem.setProductPic(esProduct.getPic());//图片
            orderItem.setProductName(esProduct.getName());//名字
            orderItem.setProductBrand(esProduct.getBrandName());//品牌
            orderItem.setProductSn(esProduct.getProductSn());//编号
            orderItem.setProductPrice(cartItem.getPrice());//购物车价格
            orderItem.setProductQuantity(cartItem.getCount());//当前商品的数量
            orderItem.setProductSkuId(skuId);//skuId
            orderItem.setProductSkuCode(skuStock.getSkuCode());//sku编码
            orderItem.setProductCategoryId(esProduct.getProductCategoryId());//分类
            orderItem.setSp1(skuStock.getSp1());//属性1
            orderItem.setSp2(skuStock.getSp2());//属性2
            orderItem.setSp3(skuStock.getSp3());//属性3
            orderItem.setProductAttr(attrValuejsonString);//销售属性
            orderItems.add(orderItem);
            //保存
            orderItemMapper.insert(orderItem);

        });
        //3). 清除购物车中已经下单的商品
        cartService.removeCartItem(accessToken,cartSkuIds);
    }

    //构造订单VO
    private OrderCreateVo initOrderCreateVo(BigDecimal frontPrice, Long addressId, String accessToken, Member member) {
        OrderCreateVo orderCreateVo = new OrderCreateVo();
        //MyBatisplus->自带ID生成器->时间
        String timeId = IdWorker.getTimeId();
        //订单号
        orderCreateVo.setOrderSn(timeId);

        //收货地址
        orderCreateVo.setAddressId(addressId);

        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        orderCreateVo.setCartItems(cartItems);//购物车商品

        //设置会员ID
        orderCreateVo.setMemberId(member.getId());

        //总价格
        orderCreateVo.setTotalPrice(frontPrice);

        //描述信息
        orderCreateVo.setDetailInfo(cartItems.get(0).getName());
        return orderCreateVo;
    }

    //构造保存订单信息
    private Order initOrder(BigDecimal frontPrice, Long addressId, String note, Member member, OrderCreateVo orderCreateVo) {
        Order order = new Order();
        order.setMemberId(member.getId());//会员ID
        order.setOrderSn(orderCreateVo.getOrderSn());//订单号
        order.setCreateTime(new Date());//创建时间
        order.setOrderType(7);//确认收货天数
        order.setNote(note);//订单备注
        order.setMemberUsername(member.getUsername());//会员名
        //验证价格
        order.setTotalAmount(frontPrice);//订单总额
        order.setFreightAmount(new BigDecimal("10"));//运费
        order.setStatus(OrderStatusEnume.UNPAY.getCode());//商品状态码
        //收货人信息
        MemberReceiveAddress address = memberService.getMemberAddressesByAddresseId(addressId);
        order.setReceiverName(address.getName());//收货人姓名
        order.setReceiverPhone(address.getPhoneNumber());//收货人电话
        order.setReceiverPostCode(address.getPostCode());//收货人邮编
        order.setReceiverProvince(address.getProvince());//省份/直辖市
        order.setReceiverCity(address.getCity());//城市
        order.setReceiverRegion(address.getRegion());//区
        order.setReceiverDetailAddress(address.getDetailAddress());//详细地址
        return order;
    }

    /*
    * 前端传来的价钱和后台查询的价格比较
    * */
    private Boolean validPrice(BigDecimal frontPrice,String accessToken,Long addressId){

        //拿到购物车
        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        //共享资源
        threadLocal.set(cartItems);
        BigDecimal bigDecimal = new BigDecimal("0");
        //总价必须去数据库查最新价格
        for (CartItem item: cartItems){
//            bigDecimal=bigDecimal.add(item.getTotalPrice());
            //查出真正价格
            Long skuId = item.getSkuId();
            BigDecimal newPrice = skuStockService.getSkuPriceBySkuId(skuId);
            item.setPrice(newPrice);
            //数量
            Integer count = item.getCount();
            //单个商品总价multiply->数量*单价
            BigDecimal multiply = newPrice.multiply(new BigDecimal(count.toString()));
            //总价
            bigDecimal= bigDecimal.add(multiply);
        }
        //根据收货地址获取运费
        BigDecimal tranPrice = new BigDecimal("10");
        BigDecimal totalPrice = bigDecimal.add(tranPrice);

        return totalPrice.compareTo(frontPrice) == 0?true:false;
    }

    /*
    * out_trade_no->订单号
    * total_amount->总金额
    * subject->标题
    * body->描述
    * */
    // 支付宝支付
    private String payOrder(String out_trade_no,
                            String total_amount,
                            String subject,
                            String body) {
        // 1、创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.gatewayUrl,
                AlipayConfig.app_id,
                AlipayConfig.merchant_private_key,
                "json",
                AlipayConfig.charset,
                AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);

        // 2、创建一次支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        // 付款金额，必填
        // 订单名称，必填
        // 商品描述，可空

        // 3、构造支付请求数据
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\"," + "\"total_amount\":\"" + total_amount
                + "\"," + "\"subject\":\"" + subject + "\"," + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = "";
        try {
            // 4、请求发送支付数据
            result = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;// 支付跳转页的代码

    }

}
