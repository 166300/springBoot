package com.atbm.gmall.oms.component;

import com.alibaba.fastjson.JSON;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.ums.entity.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberComponent {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public Member getMenmberByAccessToken(String accessToken){
        String json = stringRedisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEBER + accessToken);
        if(!StringUtils.isEmpty(json)){
            Member member = JSON.parseObject(json, Member.class);
            return member;
        }
        return null;
    }
}
