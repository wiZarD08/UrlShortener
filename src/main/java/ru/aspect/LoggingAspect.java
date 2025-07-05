package ru.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* ru.web.controller.*.*(..))")
    private void allMethodsOfControllers() {
    }

    @AfterThrowing(pointcut = "allMethodsOfControllers()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in {}.{}() with message {}", joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(), ex.getMessage());
    }
}
