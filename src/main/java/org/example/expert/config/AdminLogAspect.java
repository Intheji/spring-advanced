package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class AdminLogAspect {

    @Pointcut("@annotation(org.example.expert.domain.common.annotation.AdminAspect)")
    public void adminLog() {}

    @Around("adminLog()")
    public Object logAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();
        String method = request.getMethod();
        LocalDateTime now = LocalDateTime.now();

        String requestBody = Arrays.toString(joinPoint.getArgs());

        log.info("(Admin req) userId={} time={} method={} url={} body={}",
                userId, now, method, url, requestBody);

        Object result = joinPoint.proceed();

        String responseBody = String.valueOf(result);

        log.info("(Admin res) userId={} time={} method={} url={} body={}",
                userId, LocalDateTime.now(), method, url, responseBody);

        return result;
    }
}
