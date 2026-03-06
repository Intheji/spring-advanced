package org.example.expert.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class AdminLogAspectTest {

    private final AdminLogAspect adminLogAspect = new AdminLogAspect();

    // 그냥 호출용...................... 하.........
    @Test
    @DisplayName("adminLog 포인트컷 메서드를 호출한다")
    void adminLog_pointcut_method_call() {
        // given
        AdminLogAspect adminLogAspect = new AdminLogAspect();

        // when
        adminLogAspect.adminLog();
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝나면 RequestContextHolder 를 비워서 다른 테스트에 영향이 가지 않도록 정리
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("logAdmin 성공 - 관리자 요청 정보를 읽고 대상 메서드 실행 결과를 그대로 반환한다")
    void logAdmin_success() throws Throwable {
        // given: 현재 요청 정보를 RequestContextHolder 에 세팅
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/users/1");
        request.setMethod("PATCH");
        request.setAttribute("userId", 1L);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 대역 배우 캐스팅
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        // 대역 배우 훈련(Stubbing)시키기
        given(joinPoint.getArgs()).willReturn(new Object[]{"ADMIN"});
        given(joinPoint.proceed()).willReturn("success-result");

        // when: AOP 메서드를 실행한다
        Object result = adminLogAspect.logAdmin(joinPoint);

        // then: 주연 배우인 Aspect 가 joinPoint.proceed() 를 호출하고 그 결과를 그대로 반환해야 한다
        assertThat(result).isEqualTo("success-result");
        then(joinPoint).should().getArgs();
        then(joinPoint).should().proceed();
    }

    @Test
    @DisplayName("logAdmin 실패 - 대상 메서드 실행 중 예외가 발생하면 예외를 다시 던진다")
    void logAdmin_fail_when_joinPoint_throws_exception() throws Throwable {
        // given: 현재 요청 정보를 RequestContextHolder 에 세팅한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/comments/10");
        request.setMethod("DELETE");
        request.setAttribute("userId", 99L);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 대역 배우 캐스팅
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        // 대역 배우 훈련(Stubbing)시키기
        given(joinPoint.getArgs()).willReturn(new Object[]{10L});
        given(joinPoint.proceed()).willThrow(new IllegalArgumentException("잘못된 요청"));

        // when & then: 예외가 발생하면 Aspect 는 잡아서 로그를 남긴 뒤 다시 던져야 한다.
        assertThatThrownBy(() -> adminLogAspect.logAdmin(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 요청");

        then(joinPoint).should().getArgs();
        then(joinPoint).should().proceed();
    }
}