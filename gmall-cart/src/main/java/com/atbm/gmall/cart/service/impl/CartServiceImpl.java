package com.atbm.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atbm.gmall.cart.component.MemberComponent;
import com.atbm.gmall.cart.service.CartService;
import com.atbm.gmall.cart.vo.Cart;
import com.atbm.gmall.cart.vo.CartItem;
import com.atbm.gmall.cart.vo.CartResponse;
import com.atbm.gmall.cart.vo.UserCartKey;
import com.atbm.gmall.constant.CartConstant;
import com.atbm.gmall.pms.entity.Product;
import com.atbm.gmall.pms.entity.SkuStock;
import com.atbm.gmall.pms.service.ProductService;
import com.atbm.gmall.pms.service.SkuStockService;
import com.atbm.gmall.ums.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@Component
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    RedissonClient redissonClient;

    @Reference
    SkuStockService skuStockService;

    @Reference
    ProductService productService;
    /*
    * 添加商品到购物车*/
    @Override
    public CartResponse addToCart(Long skuId, Integer num, String cartKey, String accessToken)
            throws ExecutionException, InterruptedException {
        //根据accessToken获取用户的ID
        Member member = memberComponent.getMeberByAccessToken(accessToken);
        //用户有离线购物车的时候登陆了
        if(member!=null && !StringUtils.isEmpty(accessToken)){
            //合并购物车
            mergeCar(cartKey,member.getId());
        }
        //判断用户状态得到对应使用的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey,accessToken);
        String fianlCartKey=userCartKey.getFinalCartKey();
        //添加到购物车
        CartItem cartItem = addItemToCart(skuId,num,fianlCartKey);
        CartResponse cartResponse = new CartResponse();
        Cart cart=new Cart();
        cart.setCartItems(Arrays.asList(cartItem));
        cartResponse.setCart(cart);
        //没有购物车要返回一个购物车
        cartResponse.setCartKey(userCartKey.getTempCartkey());
        //第一次查询购物车cartKey->是刚生成的没这个参数所以查不到数据
        if(StringUtils.isEmpty(userCartKey.getTempCartkey())){
            cartResponse.setCart(cartList(cartKey, accessToken).getCart());
        }
        return cartResponse;
    }

    /*
    * 修改购物车数量
    * */
    @Override
    public CartResponse updateCartItemNum(Long skuId, Integer num, String cartKey, String accessToken) {
        System.out.println("OK+++++++ MemberComponent: +++++++cartKey+++++++++"+cartKey);
        System.out.println("OK+++++++ MemberComponent: +++++++++accessToken+++++++"+accessToken);
        //获得用户要使用的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey,accessToken);
        //得到购物车
        String finalCartKey = userCartKey.getFinalCartKey();
        //通过购物车得到所有商品
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        //把要改动的商品从Redis中得到转成对象
        String json = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        //修改数量->在转成json存进去
        cartItem.setCount(num);
        String cartItemJson = JSON.toJSONString(cartItem);
        //根据id再存入
        map.put(skuId.toString(),cartItemJson);
        //返回商品
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        cartResponse.setCart(cartList(cartKey, accessToken).getCart());
        System.out.println("整个购物车"+cartResponse.getCart());
        return cartResponse;
    }
    /*
    *
    * 查看购物车所有商品
    * */
    @Override
    public CartResponse cartList(String cartKey, String accessToken) {
        //查询购物车的时候 判断购物车是否需要合并
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey,accessToken);
        if(userCartKey.getLogin()){
            //用户登录就合并购物车
            mergeCar(cartKey,userCartKey.getUserId());
        }
        //设置过期时间,会自动续期
        stringRedisTemplate.expire(userCartKey.getFinalCartKey(),1L, TimeUnit.DAYS);
        //查出购物车数据
        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        //创建购物车
        Cart cart = new Cart();
        //得到到购物车商品
        List<CartItem> cartItems=new ArrayList<>();
        CartResponse cartResponse = new CartResponse();
        if(map!=null&&!map.isEmpty()){
            //有维护选中状态的key去除
            map.entrySet().forEach((item)->{
                if(!item.getKey().equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    cartItems.add(cartItem);
                }
            });
            cart.setCartItems(cartItems);
        }else{
            //用户没有购物车->创建购物车
            cartResponse.setCartKey(userCartKey.getTempCartkey());
        }
        cartResponse.setCart(cart);
        return cartResponse;
    }
    /*
    * 删除商品
    * */
    @Override
    public CartResponse delCart(Long skuId, String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey, accessToken);
        //维护勾选状态列表
        checkItem(Arrays.asList(skuId),false,userCartKey.getFinalCartKey());

        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        map.remove(skuId.toString());
        CartResponse cartResponse = cartList(cartKey,accessToken);
        return cartResponse;
    }
    /*
    * 清空购物车
    * */
    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey, accessToken);
        RMap<String, String> map = redissonClient.getMap(userCartKey.getFinalCartKey());
        map.clear();
        CartResponse cartResponse = new CartResponse();
        return cartResponse;
    }
    /*
    * 商品选中状态
    * */
    @Override
    public CartResponse cartCheckItems(String skuIds, Integer ops, String cartKey, String accessToken) {
        //记录被改过状态的skuId
        List<Long> skuIdList = new ArrayList<>();
        //得到当前状态的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(cartKey, accessToken);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> cart = redissonClient.getMap(finalCartKey);
        //选中状态
        boolean checked = ops==1?true:false;
        //修改购物项状态
        if(!StringUtils.isEmpty(skuIds)){
            //每个skuId对应商品的check属性
            String[] ids = skuIds.split(",");
            for (String id:ids) {
                long skuId = Long.parseLong(id);
                skuIdList.add(skuId);
                //找到skuId对应个购物车中的状态进行修改
                if(cart!=null&&!cart.isEmpty()){
                    //当前skuId对应的商品
                    String jsonValue = cart.get(id);
                    CartItem cartItem = JSON.parseObject(jsonValue, CartItem.class);
                    cartItem.setCheck(checked);
                    //覆盖原来的数据
                    cart.put(id,JSON.toJSONString(cartItem));
                }
            }
        }
        //修改状态
        checkItem(skuIdList,checked,finalCartKey);
        //返回整个购物车
        CartResponse cartResponse = cartList(cartKey, accessToken);
        return cartResponse;
    }

    @Override
    public List<CartItem> getCartItemForOrder(String accessToken) {
        List<CartItem> cartItems = new ArrayList<>();
        //根据用户asseccTkoen获取被选中的数据
        UserCartKey cartKey = memberComponent.getCartKey(null, accessToken);
        //获得对应购物车
        RMap<String , String> cart = redissonClient.getMap(cartKey.getFinalCartKey());
        //购物车内被选中的商品ID
        String checkItemJson = cart.get(CartConstant.CART_CHECKED_KEY);
        //json转成set集合
        Set<Long> items = JSON.parseObject(checkItemJson, new TypeReference<Set<Long>>() {
        });
        //迭代ID得到对应的商品
        items.forEach((item)->{
            //得到ID
            String itemJson = cart.get(item .toString());
            //得到商品封装
            cartItems.add(JSON.parseObject(itemJson,CartItem.class));
        });
        return cartItems;
    }


    /*
    * 清除购物车商品
    * */
    @Override
    public void removeCartItem(String accessToken, List<Long> cartSkuIds) {
        UserCartKey cartKey = memberComponent.getCartKey(null, accessToken);
        String finalCartKey = cartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        cartSkuIds.forEach((skuid)->{
            //移除商品
            map.remove(skuid.toString());
        });
        //移除勾选状态->覆盖成null
        map.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(new LinkedHashSet<Long>()));


    }


    /*
    * cartKey离线购物车
    * id用户ID
    * */
    private void mergeCar(String cartKey, Long id) {
        String oldcartKey = CartConstant.TEMP_CART_KEY_PREFIX+cartKey;
        String usercartKey = CartConstant.USER_CART_KEY_PREFIX+id.toString();

        //获取老购物车的数据
        RMap<String, String> map = redissonClient.getMap(oldcartKey);

        if(map!=null&&!map.isEmpty()){
            //map不是空的而且有数据,才需要合并
            map.entrySet().forEach((item)->{
                //有维护选中状态的key去除
                if(!item.getKey().equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    //key就是商品skuId
                    String key = item.getKey();
                    //购物项的json数据
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    try {
                        //拿到老购物车的skuId 和商品数量 添加到新商品购物车
                        addItemToCart(Long.parseLong(key),cartItem.getCount(),usercartKey);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            map.clear();
        }
    }

    /*
    * 给指定商品添加到购物车
    *   是否登录购物车状态都是一样的
    * */
    //1.按照skuId找到sku真正的信息
    //2.给指定的购物车添加记录,如果有了这个商品只是数量增加
    private CartItem addItemToCart(Long skuId, Integer num, String fianlCartKey) throws ExecutionException, InterruptedException {
        CartItem newItem = new CartItem();

        /**
         * 只接收上一步结果
         * .thenAccept((r)->{
         *      //运算
         * });
         * 等待接收上一步的结果后返回结果后才进行下一步
         * .thenApply((r)->{
         *      //运算
         *      return r;
         * })
         * 等待接收上一步的结果后异步运行这一步同时开始下一步
         * .thenAcceptAsync((r)->{
         *      //运算
         *      return r;
         * })
         */

        //启动异步任务
        CompletableFuture skuFuture = CompletableFuture.supplyAsync(() -> {
            //通过skuId查出数据库中对应的sku详情
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAcceptAsync((stock)->{
            //得到上一步商品ID
            Long productId = stock.getProductId();
            //得到商品
            Product product = productService.getById(productId);
            System.out.println("OK+++++++++++++++++++++++++++"+product);
            //封装数据
            BeanUtils.copyProperties(stock, newItem);
            newItem.setSkuId(stock.getId());
            newItem.setName(product.getName());
            newItem.setCount(num);
        });

        /*
        * 购物车集合 k[skuId]是str v[购物项]是str(json)
        * */
        RMap<String, String> map = redissonClient.getMap(fianlCartKey);
        //获取购物车中sku对应的购物项
        String itemJson = map.get(skuId.toString());

        //因为异步原因 需要在这里等结果,结束后自然运行
        skuFuture.get();
        //检查购物车是否已经存在这个购物项
        if(!StringUtils.isEmpty(itemJson)){
            //存在这个商品数量叠加
            //因为价格会变动所以一切按照数据库为准
            CartItem oldItem = JSON.parseObject(itemJson, CartItem.class);
            Integer oldcount = oldItem.getCount();
            newItem.setCount(oldcount+newItem.getCount());
            String newItemString = JSON.toJSONString(newItem);
            //老数据覆盖成新数据
            map.put(skuId.toString(),newItemString);
        }else {
            //不存在这个商品添加商品
            String string = JSON.toJSONString(newItem);
            map.put(skuId.toString(),string);
        }
        //维护勾选状态列表
        checkItem(Arrays.asList(skuId),true,fianlCartKey);
        return newItem;
    }

    private void checkItem(List<Long> skuId,boolean checked,String fianlCartKey){
        RMap<String, String> cart = redissonClient.getMap(fianlCartKey);
        //快速找到选中的商品创建数组用map->key是set集合
        String checkedJson = cart.get(CartConstant.CART_CHECKED_KEY);
        //自定义引用类型set不会重复保存相同skuId
        Set<Long> longSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() {
        });
        //防止空指针
        if(longSet==null||longSet.isEmpty()){
            longSet = new LinkedHashSet<>();
        }
        if(checked){
            //全是选中状态
            longSet.addAll(skuId);
            log.info("被选中的商品:{}",longSet);
        }else{
            //全是未选中状态
            longSet.removeAll(skuId);
            log.info("反选中的商品:{}",longSet);
        }
        //重新保存被选中的商品
        cart.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(longSet));
    }

}














