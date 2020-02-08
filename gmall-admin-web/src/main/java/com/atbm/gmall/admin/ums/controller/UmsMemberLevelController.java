package com.atbm.gmall.admin.ums.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.ums.entity.MemberLevel;
import com.atbm.gmall.ums.service.MemberLevelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@CrossOrigin
@RestController
public class UmsMemberLevelController {

    @Reference
    MemberLevelService memberLevelService;

    /*
     * 查出所有会员等级
     * */
    @GetMapping("/memberLevel/list")
    public Object meberLevelList() {
        List<MemberLevel> list = memberLevelService.list();
        return new CommonResult().success(list);
    }


}
