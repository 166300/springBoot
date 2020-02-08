package com.atbm.gmall.admin.aop;

import com.atbm.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

/*
 *
 * 编写切面
 *   1）、导入切面场景
 *   2）、编写切面
 *       1、@Aspect
 *       2、切入点表达式
 *       3、通知
 *           前置：执行之前
 *           后置：执行之后
 *           环绕：4合1、拦截方法执行
 *           返回：正常返回之后
 *           异常：异常出现之后
 * */
//利用aop完成统一的数据校验，出错就返回前端错误提示
@Slf4j
@Aspect
@Component
public class DataVaildAspect {
    //admin包下的、任意结尾是controller类下的、任意方法、任意参数
    @Around("execution(* com.atbm.gmall.admin..*Controller.*(..))")
    public Object validAround(ProceedingJoinPoint point) {
        Object proceed = null;
        //获得的是方法的参数
        Object[] args = point.getArgs();
        for (Object obj : args) {
            //判断obj是不是BindingResult类型的
            //多个参数需要挑选
            if (obj instanceof BindingResult) {
                BindingResult r = (BindingResult) obj;
                if (r.getErrorCount() > 0) {
                    //校验到错误
                    return new CommonResult().validateFailed(r);
                }
            }
        }
        try {
//            System.out.println("前置");
            log.debug("校验切面介入工作...");
            //需要进入的方法
            proceed = point.proceed(point.getArgs());
            log.debug("校验切面放行...");
//            System.out.println("返回");
        } catch (Throwable throwable) {
            //异常过大封装运行时抛出
            throw new RuntimeException(throwable);
//            System.out.println("异常");
        } finally {
//            System.out.println("后置");
        }
        return proceed;
    }


}
