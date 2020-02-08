package com.atbm.gmall.admin.ums.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * 用户登录参数
 * Created by atguigu 4/26.
 */
@ToString
@Getter
@Setter
public class UmsAdminParam {
    //规定长度
    @Length(min = 6, max = 19, message = "用户名长度是6-18位")
    @ApiModelProperty(value = "用户名", required = true)
    private String username;
    @ApiModelProperty(value = "密码", required = true)
    private String password;
    //不能是空的
    @NotEmpty
    @ApiModelProperty(value = "用户头像")
    private String icon;
    @Email(message = "邮箱格式错误")
    @ApiModelProperty(value = "邮箱")
    private String email;
    @NotNull
    @ApiModelProperty(value = "用户昵称")
    private String nickName;
    @ApiModelProperty(value = "备注")
    private String note;
}
