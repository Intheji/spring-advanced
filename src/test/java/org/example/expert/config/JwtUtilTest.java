package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // given: JwtUtil 이 동작하려면 @Value 로 주입받는 secretKey 와 init() 이 필요하다.
        // 테스트에서는 스프링 컨테이너를 띄우지 않으므로 ReflectionTestUtils 로 값을 넣어준다.
        String rawSecretKey = "abcdefghijklmnopqrstuvwxyz123456";
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString(rawSecretKey.getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(jwtUtil, "secretKey", encodedSecretKey);

        // @PostConstruct 메서드는 테스트에서 자동 호출되지 않으므로 직접 호출한다.
        jwtUtil.init();
    }

    @Test
    @DisplayName("createToken 성공 - Bearer 토큰을 생성하고 claims 를 추출할 수 있다")
    void createToken_success() {
        // given: 토큰에 담을 사용자 정보
        Long userId = 1L;
        String email = "hyunji@email.com";
        UserRole userRole = UserRole.USER;

        // when: 토큰을 생성한다
        String bearerToken = jwtUtil.createToken(userId, email, userRole);

        // then: Bearer 접두사가 포함되어 있어야 한다.
        assertThat(bearerToken).startsWith("Bearer ");

        // 그리고 substringToken + extractClaims 로 다시 정보를 꺼낼 수 있어야 한다.
        String jwt = jwtUtil.substringToken(bearerToken);
        Claims claims = jwtUtil.extractClaims(jwt);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email")).isEqualTo("hyunji@email.com");
        assertThat(claims.get("userRole").toString()).isEqualTo("USER");
    }

    @Test
    @DisplayName("substringToken 성공 - Bearer 접두사가 있으면 실제 토큰 문자열만 반환한다")
    void substringToken_success() {
        // given
        String tokenValue = "Bearer access.token.value";

        // when
        String result = jwtUtil.substringToken(tokenValue);

        // then
        assertThat(result).isEqualTo("access.token.value");
    }

    @Test
    @DisplayName("substringToken 실패 - Bearer 접두사가 없으면 예외가 발생한다")
    void substringToken_fail_when_token_has_no_bearer_prefix() {
        // given
        String tokenValue = "access.token.value";

        // when & then
        assertThatThrownBy(() -> jwtUtil.substringToken(tokenValue))
                .isInstanceOf(ServerException.class)
                .hasMessage("Not Found Token");
    }

    @Test
    @DisplayName("extractClaims 성공 - 생성한 토큰에서 사용자 정보를 추출한다")
    void extractClaims_success() {
        // given: 먼저 정상 토큰을 하나 생성한다.
        String bearerToken = jwtUtil.createToken(10L, "admin@email.com", UserRole.ADMIN);
        String jwt = jwtUtil.substringToken(bearerToken);

        // when: claims 를 추출한다
        Claims claims = jwtUtil.extractClaims(jwt);

        // then: 토큰 안에 담긴 값이 정확해야 한다.
        assertThat(claims.getSubject()).isEqualTo("10");
        assertThat(claims.get("email")).isEqualTo("admin@email.com");
        assertThat(claims.get("userRole").toString()).isEqualTo("ADMIN");
    }
}
