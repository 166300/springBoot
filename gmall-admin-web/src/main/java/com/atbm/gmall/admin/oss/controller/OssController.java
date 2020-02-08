package com.atbm.gmall.admin.oss.controller;


import com.atbm.gmall.admin.oss.component.OssCompent;
import com.atbm.gmall.to.CommonResult;
import com.atbm.gmall.to.OssPolicyResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Oss相关操作接口
 */
@CrossOrigin
@Controller
@Api(tags = "OssController", description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
    @Autowired
    private OssCompent ossCompent;

    @ApiOperation(value = "oss上传签名生成")
    @GetMapping(value = "/policy")
    @ResponseBody
    public Object policy() {
        OssPolicyResult policy = ossCompent.policy();
        return new CommonResult().success(policy);
    }

}
