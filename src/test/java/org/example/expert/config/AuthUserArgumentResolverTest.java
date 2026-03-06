package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    // 테스트용 더미 메서드들
    static class TestController {
        public void validMethod(@Auth AuthUser authUser) {
        }

        public void authAnnotationOnly(@Auth String value) {
        }

        public void authUserTypeOnly(AuthUser authUser) {
        }

        public void noAuthNoAuthUser(String value) {
        }
    }

    @Test
    @DisplayName("supportsParameter 성공 - @Auth 어노테이션과 AuthUser 타입이 함께 있으면 true를 반환한다")
    void supportsParameter_success_when_auth_annotation_and_auth_user_type_are_together() throws Exception {
        // given
        Method method = TestController.class.getMethod("validMethod", AuthUser.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("supportsParameter 실패 - @Auth 어노테이션만 있고 AuthUser 타입이 아니면 예외가 발생한다")
    void supportsParameter_fail_when_only_auth_annotation_exists() throws Exception {
        // given
        Method method = TestController.class.getMethod("authAnnotationOnly", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when & then
        assertThatThrownBy(() -> resolver.supportsParameter(parameter))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }

    @Test
    @DisplayName("supportsParameter 실패 - AuthUser 타입만 있고 @Auth 어노테이션이 없으면 예외가 발생한다")
    void supportsParameter_fail_when_only_auth_user_type_exists() throws Exception {
        // given
        Method method = TestController.class.getMethod("authUserTypeOnly", AuthUser.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when & then
        assertThatThrownBy(() -> resolver.supportsParameter(parameter))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }

    @Test
    @DisplayName("supportsParameter 성공 - @Auth 어노테이션도 없고 AuthUser 타입도 아니면 false를 반환한다")
    void supportsParameter_returns_false_when_no_auth_annotation_and_not_auth_user_type() throws Exception {
        // given
        Method method = TestController.class.getMethod("noAuthNoAuthUser", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("resolveArgument 성공 - request attribute 값을 기반으로 AuthUser를 생성한다")
    void resolveArgument_success() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);
        request.setAttribute("email", "hyunji@email.com");
        request.setAttribute("userRole", "USER");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        // when
        Object result = resolver.resolveArgument(null, null, webRequest, null);

        // then
        assertThat(result).isInstanceOf(AuthUser.class);

        AuthUser authUser = (AuthUser) result;
        assertThat(authUser.getId()).isEqualTo(1L);
        assertThat(authUser.getEmail()).isEqualTo("hyunji@email.com");
        assertThat(authUser.getUserRole()).isEqualTo(UserRole.USER);
    }
}
