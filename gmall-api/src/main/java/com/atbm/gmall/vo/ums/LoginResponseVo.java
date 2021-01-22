package com.atbm.gmall.vo.ums;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponseVo implements Serializable {

    private Long memberLevelId;

    private String username;

    private String nickname;

    private String phone;

    private String accessToken;
}
