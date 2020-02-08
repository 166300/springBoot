package com.atbm.gmall.ums.service.impl;

import com.atbm.gmall.ums.entity.Admin;
import com.atbm.gmall.ums.mapper.AdminMapper;
import com.atbm.gmall.ums.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-22
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}
