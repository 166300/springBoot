package com.atbm.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atbm.gmall.ums.entity.Admin;
import com.atbm.gmall.ums.mapper.AdminMapper;
import com.atbm.gmall.ums.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.management.QueryEval;
import java.util.Queue;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
@Component
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    AdminMapper adminMapper;
    /*
    * 获取用户详情
    * */
    @Override
    public Admin getUserInfo(String userName) {
        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("userName",userName));
    }

    @Override
    public Admin login(String username, String password) {
        String s = DigestUtils.md5DigestAsHex(password.getBytes());
        QueryWrapper<Admin> username1 = new QueryWrapper<Admin>().eq("username", username).eq("password",s);
        Admin admin = adminMapper.selectOne(username1);
        return admin;
    }













}
