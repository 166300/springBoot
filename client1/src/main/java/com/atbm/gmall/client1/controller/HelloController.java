package com.atbm.gmall.client1.controller;

import com.atbm.gmall.client1.config.SsoConfig;
import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sun.net.httpserver.HttpServerImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HelloController {

    @Autowired
    SsoConfig ssoConfig;

    @GetMapping("/")
    public String index(Model model,
                        @CookieValue(value = "sso_user",required = false)
                                String ssoUserCookie,
                        @RequestParam(value = "sso_user",required = false)
                                String ssoUserParam,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        /*
        * 不把无意义的uuid放进去把token制作成jwt
        * */
        if(!StringUtils.isEmpty(ssoUserParam)){
            //没有调用登陆服务器登陆就跳转回来证明登陆过
            Cookie sso_user = new Cookie("sso_user", ssoUserParam);
            response.addCookie(sso_user);
            return "index";
        }
        StringBuffer requestURL = request.getRequestURL();
        System.out.println("请求地址是:"+requestURL.toString());
        //判断是否登录
        if(StringUtils.isEmpty(ssoUserCookie)){
            //没登录,重定向到登陆服务器
            System.out.println("判断");
            String url=ssoConfig.getUrl()+ssoConfig.getLoginpath()+"?redirec_url="+requestURL.toString();
            response.sendRedirect(url);
            //redirect:/重定向到绝对路径
            //return "redirect:/"+ssoConfig.getUrl()+ssoConfig.getLoginpath()+"?redirec_url="+requestURL.toString();
            return null;
        }else{
            //登录,redis.get(ssoUserCookie);用户信息
            model.addAttribute("loginUser","张三");
            return "index";
        }

    }
}
