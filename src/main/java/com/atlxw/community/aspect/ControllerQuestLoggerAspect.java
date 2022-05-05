package com.atlxw.community.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ControllerQuestLoggerAspect {
    /**
     * 对所有的controller都进行AOP，进行日志处理
     */
    @Pointcut("execution(* com.atlxw.community.controller..*(..))")
    private void needToLog(){}

    /**
     * 用来增强对应的切入点表达式的方法
     * pjp.getSignature()表示获得切入点的方法的名称
     * @param pjp
     * @return
     */
    @Around("needToLog()")
    public Object beforeControllerInvocation (ProceedingJoinPoint pjp){
        log.info("===================Controller Request (" + pjp.getSignature() + ") Start=======================");
        log.info(pjp.getSignature() + " invoke with arguments: " + Arrays.toString(pjp.getArgs()));

        long start = System.currentTimeMillis();
        try{
            return pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.info(pjp.getSignature() + " throw an exception " + throwable.getMessage());
            log.info(pjp.getSignature() + " exit unexpectedly.");
        } finally {
            log.info(pjp.getSignature() + "execution duration: " + (System.currentTimeMillis() - start) + "ms");
            log.info("====================Controller Request (" + pjp.getSignature() + ") End======================");
        }

        return null;
    }
}
