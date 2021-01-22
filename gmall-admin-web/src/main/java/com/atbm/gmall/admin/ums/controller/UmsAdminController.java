package com.atbm.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.admin.ums.vo.UmsAdminLoginParam;
import com.atbm.gmall.admin.ums.vo.UmsAdminParam;
import com.atbm.gmall.admin.utils.JwtTokenUtil;
import com.atbm.gmall.ums.entity.Admin;
import com.atbm.gmall.ums.service.AdminService;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理
 */



@Slf4j
@CrossOrigin
@RestController
@Api(tags = "AdminController", description = "后台用户管理")
@RequestMapping("/admin")
public class UmsAdminController {
    @Reference
    private AdminService adminService;
    @Value("${gmall.jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${gmall.jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    JwtTokenUtil jwtTokenUtil;
/*
 *
 * 使用JSR303步骤
 * 1）、给要校验的javaBean上标注校验注解
 * 2）、告诉spring这个数据需要校验@Valid
 * 3)、感知校验成功/失败：
 *      需要给开启校验的javaBean参数后面。跟上BindingResult对象
 *          加上后即使校验失败也会执行方法我们需要手动处理
 * */

/*
*
*
*
* */
    @ApiOperation(value = "用户注册")
    @PostMapping(value = "/register")
    public Object register(@Valid @RequestBody UmsAdminParam umsAdminParam,BindingResult result) {
        Admin admin = null;
        //TODO 完成注册功能
        int errorCount = result.getErrorCount();
        if (errorCount>0){
            //有错
            List<FieldError> fieldErrors = result.getFieldErrors();
            fieldErrors.forEach((fieldError)->{
                String field = fieldError.getField();
                log.debug("属性：{},传来的值是：{},出错的提示消息：{}",
                        field,fieldError.getRejectedValue(),fieldError.getDefaultMessage());
            });
            return new CommonResult().validateFailed(result);
        }else{

        }
        log.debug("注册详情：{}",umsAdminParam);
        return new CommonResult().success(admin);
    }

    @ApiOperation(value = "登录以后返回token")
    @PostMapping(value = "/login")
    public Object login(@RequestBody UmsAdminLoginParam umsAdminLoginParam, BindingResult result) {
        //去数据库登陆
        Admin admin = adminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        //登陆成功生成token，此token携带基本用户信息，以后就不用去数据库了
        String token = jwtTokenUtil.generateToken(admin);
        if (token == null) {
            return new CommonResult().validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return new CommonResult().success(tokenMap);
    }

    @ApiOperation(value = "刷新token")
    @GetMapping(value = "/token/refresh")
    public Object refreshToken(HttpServletRequest request) {
        //1、获取请求头中的Authorization完整值
        String oldToken = request.getHeader(tokenHeader);
        String refreshToken = "";

        //2、从请求头中的Authorization中分离出jwt的值
        String token = oldToken.substring(tokenHead.length());

        //3、是否可以进行刷新（未过刷新时间）
        if (jwtTokenUtil.canRefresh(token)) {
            refreshToken =  jwtTokenUtil.refreshToken(token);
        }else  if(refreshToken == null && "".equals(refreshToken)){
            return new CommonResult().failed();
        }

        //将新的token交给前端
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return new CommonResult().success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Object getAdminInfo(HttpServletRequest request) {
        String oldToken = request.getHeader(tokenHeader);
        String userName = jwtTokenUtil.getUserNameFromToken(oldToken.substring(tokenHead.length()));
        //查询用户头像
        //dubbo不能调用带有泛型的service
//        Admin umsAdmin = adminService.getOne(new QueryWrapper<Admin>().eq("username", userName));
        Admin umsAdmin = adminService.getUserInfo(userName);
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsAdmin.getUsername());
        data.put("roles", new String[]{"TEST"});
        data.put("icon", umsAdmin.getIcon());
        return new CommonResult().success(data);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public Object logout() {
        //TODO 用户退出

        return new CommonResult().success(null);
    }

    @ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public Object list(@RequestParam(value = "name",required = false) String name,
                       @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum){
        //TODO 分页查询用户信息

        //TODO 响应需要包含分页信息；详细查看swagger规定
        return new CommonResult().failed();
    }

    @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Object getItem(@PathVariable Long id){

        //TODO 获取指定用户信息
        return new CommonResult().failed();
    }

    @ApiOperation("更新指定用户信息")
    @RequestMapping(value = "/update/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Object update(@PathVariable Long id,@RequestBody Admin admin){

        //TODO 更新指定用户信息
        return new CommonResult().failed();
    }

    @ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Object delete(@PathVariable Long id){
        //TODO 删除指定用户信息
        return new CommonResult().failed();
    }

    @ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update",method = RequestMethod.POST)
    @ResponseBody
    public Object updateRole(@RequestParam("adminId") Long adminId,
                             @RequestParam("roleIds") List<Long> roleIds){
        //TODO 给用户分配角色
        return new CommonResult().failed();
    }

    @ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}",method = RequestMethod.GET)
    @ResponseBody
    public Object getRoleList(@PathVariable Long adminId){
        //TODO 获取指定用户的角色

        return new CommonResult().success(null);
    }

    @ApiOperation("给用户分配(增减)权限")
    @RequestMapping(value = "/permission/update",method = RequestMethod.POST)
    @ResponseBody
    public Object updatePermission(@RequestParam Long adminId,
                                   @RequestParam("permissionIds") List<Long> permissionIds){
        //TODO 给用户分配(增减)权限

        return new CommonResult().failed();
    }

    @ApiOperation("获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission/{adminId}",method = RequestMethod.GET)
    @ResponseBody
    public Object getPermissionList(@PathVariable Long adminId){
        //TODO 获取用户所有权限（包括+-权限）
        return new CommonResult().failed();
    }
}
