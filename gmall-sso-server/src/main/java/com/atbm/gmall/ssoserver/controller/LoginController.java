package com.atbm.gmall.ssoserver.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atbm.gmall.constant.SysCacheConstant;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.ums.entity.Member;
import com.atbm.gmall.ums.service.MemberService;
import com.atbm.gmall.vo.ums.LoginResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Reference
    MemberService memberService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
/*
* 登录
* */
    @ResponseBody
    @PostMapping("/applogin")
    public CommonResult loginForGmall(@RequestParam("username") String username,
                                      @RequestParam("password") String password){
        Member member=memberService.login(username,password);
        if(member==null){
            System.out.println("已经NO+++");
            //登陆失败
            CommonResult result = new CommonResult().failed();
            result.setMessage("账号密码不匹配,请重新登录");
            return result;
        }else{
            System.out.println("已经OK+++");
            //登陆成功生成对应的token
            String token = UUID.randomUUID().toString().replace("-","");
            //序列化用户信息
            String memberJson = JSON.toJSONString(member);
            //拼接对象令牌 存入redis 设置失效时间30min
            stringRedisTemplate.opsForValue().set(SysCacheConstant.LOGIN_MEBER+token,memberJson,
                    SysCacheConstant.LOGIN_MEBER_TIMEOUT, TimeUnit.MINUTES);
            LoginResponseVo vo = new LoginResponseVo();
            BeanUtils.copyProperties(member,vo);
            vo.setAccessToken(token);
            return new CommonResult().success(vo);
        }
    }

    @ResponseBody
    @GetMapping("/userinfo")
    public CommonResult getUserInfo(@RequestParam("accessToken") String accessToken){
        //拼接key
        String redisKey = SysCacheConstant.LOGIN_MEBER+accessToken;
        //拿到对象的json
        String member = stringRedisTemplate.opsForValue().get(redisKey);
        //转成对象
        Member loginMeber = JSON.parseObject(member, Member.class);
        //核心数据不能返回
        loginMeber.setId(null);
        loginMeber.setPassword(null);
        return new CommonResult().success(loginMeber);
    }



    @GetMapping("/login")
    public String login(@RequestParam(value = "redirec_url")String redirec_url,
                        @CookieValue(value = "sso_user",required = false) String ssoUser,
                        HttpServletResponse response,
                        Model model) throws IOException {
        System.out.println("进入ssoServer"+ssoUser);
        //判断是否登陆过
        if(!StringUtils.isEmpty(ssoUser)){
            //登陆过,回到请求的地址,并且吧获得到的cookie以url返回
            System.out.println(ssoUser);
            String url=redirec_url+"?"+"sso_user="+ssoUser;
            response.sendRedirect(url);
            return null;
        }else{
            //没登陆过
            model.addAttribute("redirec_url",redirec_url);
            return "login";
        }
    }
    @PostMapping("/doLogin")
    public String doLogin(String userName,String password,HttpServletResponse response,String redirec_url) throws IOException {
        System.out.println("请求的地址"+redirec_url);
        System.out.println("请求的地址"+userName);
        //模拟用户登录
        Map<String,Object> map=new HashMap<>();
        map.put("userName",userName);
        map.put("email",userName+"@qq.com");
        //标识用户登录
        String token = UUID.randomUUID().toString().replace("-","");
        stringRedisTemplate.opsForValue().set(token,JSON.toJSONString(map));
        //登录成功 吧token保存cookie sso_user=token,重定向到原来的位置
        Cookie cookie = new Cookie("sso_user",token);
        System.out.println(cookie);
        response.addCookie(cookie);
        response.sendRedirect(redirec_url+"?"+"sso_user="+token);
        return null;
    }
}
