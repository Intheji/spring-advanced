package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private ObjectMapper objectMapper;
    private JwtFilter jwtFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // given
        jwtUtil = mock(JwtUtil.class);
        objectMapper = new ObjectMapper();
        filterChain = mock(FilterChain.class);

        jwtFilter = new JwtFilter(jwtUtil, objectMapper);
    }

    @Test
    @DisplayName("/auth 경로 요청이면 JWT 검사 없이 바로 통과한다")
    void doFilterInternal_pass_when_auth_path() throws Exception {
        // given: /auth 경로는 인증 없이 통과해야 한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/signin");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 체인이 그대로 호출되고, jwtUtil 은 전혀 사용되지 않아야 한다.
        then(filterChain).should().doFilter(request, response);
        then(jwtUtil).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 401 에러를 반환한다")
    void doFilterInternal_fail_when_authorization_header_missing() throws Exception {
        // given: 인증이 필요한 경로지만 Authorization 헤더가 없다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 401 응답을 반환하고 다음 체인으로 넘기지 않아야 한다.
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Claims 추출 결과가 null 이면 401 에러를 반환한다")
    void doFilterInternal_fail_when_claims_is_null() throws Exception {
        // given: Authorization 헤더는 있지만 claims 추출 결과가 null 이다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");
        request.addHeader("Authorization", "Bearer access.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.substringToken("Bearer access.token")).willReturn("access.token");
        given(jwtUtil.extractClaims("access.token")).willReturn(null);

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 401 응답을 반환하고 다음 체인으로 넘기지 않아야 한다.
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("/admin 경로에 ADMIN 권한이 아닌 유저가 접근하면 403 에러를 반환한다")
    void doFilterInternal_fail_when_user_role_is_not_admin_for_admin_path() throws Exception {
        // given: admin 경로이지만 USER 권한으로 접근한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/dashboard");
        request.addHeader("Authorization", "Bearer access.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Claims claims = mock(Claims.class);

        given(jwtUtil.substringToken("Bearer access.token")).willReturn("access.token");
        given(jwtUtil.extractClaims("access.token")).willReturn(claims);

        given(claims.get("userRole", String.class)).willReturn("USER");
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("user@test.com");
        given(claims.get("userRole")).willReturn("USER");

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 요청 attribute 는 세팅되지만, 권한 부족으로 403 이어야 한다.
        assertThat(request.getAttribute("userId")).isEqualTo(1L);
        assertThat(request.getAttribute("email")).isEqualTo("user@test.com");
        assertThat(request.getAttribute("userRole")).isEqualTo("USER");

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("접근 권한이 없습니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("정상 JWT 이고 ADMIN 권한이면 요청을 통과시킨다")
    void doFilterInternal_success_when_valid_admin_token() throws Exception {
        // given: admin 경로이고 토큰도 정상이며 권한도 ADMIN 이다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/dashboard");
        request.addHeader("Authorization", "Bearer access.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Claims claims = mock(Claims.class);

        given(jwtUtil.substringToken("Bearer access.token")).willReturn("access.token");
        given(jwtUtil.extractClaims("access.token")).willReturn(claims);

        given(claims.get("userRole", String.class)).willReturn("ADMIN");
        given(claims.getSubject()).willReturn("10");
        given(claims.get("email")).willReturn("admin@test.com");
        given(claims.get("userRole")).willReturn("ADMIN");

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: attribute 가 세팅되고 체인이 정상 호출되어야 한다.
        assertThat(request.getAttribute("userId")).isEqualTo(10L);
        assertThat(request.getAttribute("email")).isEqualTo("admin@test.com");
        assertThat(request.getAttribute("userRole")).isEqualTo("ADMIN");

        then(filterChain).should().doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 JWT 이면 401 에러를 반환한다")
    void doFilterInternal_fail_when_token_expired() throws Exception {
        // given: claims 추출 과정에서 만료 예외가 발생한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");
        request.addHeader("Authorization", "Bearer expired.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");

        ExpiredJwtException expiredJwtException = new ExpiredJwtException(null, claims, "expired token");

        given(jwtUtil.substringToken("Bearer expired.token")).willReturn("expired.token");
        given(jwtUtil.extractClaims("expired.token")).willThrow(expiredJwtException);

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 401 응답을 반환해야 한다.
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("형식이 잘못된 JWT 이면 400 에러를 반환한다")
    void doFilterInternal_fail_when_token_malformed() throws Exception {
        // given: claims 추출 과정에서 잘못된 토큰 형식 예외가 발생한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");
        request.addHeader("Authorization", "Bearer malformed.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.substringToken("Bearer malformed.token")).willReturn("malformed.token");
        given(jwtUtil.extractClaims("malformed.token")).willThrow(new MalformedJwtException("malformed"));

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 400 응답을 반환해야 한다.
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("지원하지 않는 JWT 이면 400 에러를 반환한다")
    void doFilterInternal_fail_when_token_unsupported() throws Exception {
        // given: claims 추출 과정에서 지원하지 않는 토큰 예외가 발생한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");
        request.addHeader("Authorization", "Bearer unsupported.token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.substringToken("Bearer unsupported.token")).willReturn("unsupported.token");
        given(jwtUtil.extractClaims("unsupported.token")).willThrow(new UnsupportedJwtException("unsupported"));

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 400 응답을 반환해야 한다.
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("인증이 필요합니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("예상치 못한 예외가 발생하면 500 에러를 반환한다")
    void doFilterInternal_fail_when_unexpected_exception_occurs() throws Exception {
        // given: substringToken 단계에서 예상치 못한 예외가 발생한다.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/me");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.substringToken("Bearer token")).willThrow(new RuntimeException("unexpected"));

        // when: 필터를 실행한다
        jwtFilter.doFilter(request, response, filterChain);

        // then: 500 응답을 반환해야 한다.
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).contains("요청 처리 중 오류가 발생했습니다.");

        then(filterChain).should(never()).doFilter(any(), any());
    }
}
