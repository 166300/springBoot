package com.atbm.gmall.cart.component;

import com.alibaba.fastjson.JSON;
import com.atbm.gmall.cart.vo.UserCartKey;
import com.atbm.gmall.constant.CartConstant;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.ums.entity.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberComponent {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /*
    * 根据accessToken查询用户信息
    * */
    public Member getMeberByAccessToken(String accessToken){
        String userJson = stringRedisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEBER + accessToken);
        return JSON.parseObject(userJson, Member.class);
    }
    /*
    * 判断用户用的那个购物车
    * */
    public UserCartKey getCartKey( String cartKey,String accessToken){
        System.out.println("OK+++++++ MemberComponent: +++++++cartKey+++++++++"+cartKey);
        System.out.println("OK+++++++ MemberComponent: +++++++++accessToken+++++++"+accessToken);
        UserCartKey userCartKey = new UserCartKey();
        Member member = null;
        if(!StringUtils.isEmpty(accessToken)){
            //获取用户信息
            member = getMeberByAccessToken(accessToken);
        }
        if(member!=null){
            //在线用户--返回在线用户的->购物车
            userCartKey.setLogin(true);
            userCartKey.setUserId(member.getId());
            userCartKey.setFinalCartKey(CartConstant.USER_CART_KEY_PREFIX+member.getId().toString());
            return userCartKey;
        }else if(!StringUtils.isEmpty(cartKey)){
            System.out.println("OK+++++++++++++++++++++++++++++++++++++");
            //离线用户--返回离线用户的->购物车
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+cartKey);
            return userCartKey;
        }else{
            //没有购物车--返回新创建离线用户的->购物车
            userCartKey.setLogin(false);
            String replace = UUID.randomUUID().toString().replace("-", "");
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+replace);
            userCartKey.setTempCartkey(replace);
            return userCartKey;
        }
    }
}
//bmhyjw@gmail.com