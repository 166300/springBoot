package com.atbm.gmall.admin.aop;


/*
 *
 * 统一处理所有异常500、json
 *
 *
 * */

import com.atbm.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j

//异常处理类
@ControllerAdvice
@ResponseBody
//@RestControllerAdvice//二合一
public class GlobalExcptionHandler {
    //数学运算异常
    @ExceptionHandler(value = {ArithmeticException.class})
    public Object handlerExcption01(Exception e) {
        log.error("出现异常，信息：{}", e.getStackTrace());
        return new CommonResult().validateFailed("计算错误");
    }

    //null指针
    @ExceptionHandler(value = {NullPointerException.class})
    public Object handlerExcption02(Exception e) {
        log.error("出现异常，信息：{}", e.getStackTrace());
        return new CommonResult().validateFailed("空指针");
    }
}
