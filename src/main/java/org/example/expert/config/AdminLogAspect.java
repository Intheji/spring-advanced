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

        String params = Arrays.toString(joinPoint.getArgs());
        long start = System.currentTimeMillis();
        LocalDateTime startAt = LocalDateTime.now();


        log.info("[ADMIN API 요청] userId={} time={} method={} url={} params={}",
                userId, startAt, method, url, params);

        try {
            Object result = joinPoint.proceed();

            long durationMs = System.currentTimeMillis() - start;

            log.info("[ADMIN API 응답] userId={} time={} method={} url={} params={} durationMs={}",
                    userId, LocalDateTime.now(), method, url, params, durationMs);

            return result;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - start;

            log.warn("[ADMIN API 에러] userId={} time={} method={} url={} durationMs={} ex={} msg={}",
                    userId, LocalDateTime.now(), method, url, durationMs, e.getClass().getSimpleName(), e.getMessage());

            throw e;
        }
    }
}
