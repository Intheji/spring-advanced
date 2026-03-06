package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

// Mockito 대역 배우들을 사용하겠다고 선언!!!!
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // 1. 대역 배우 캐스팅!!!
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;

    // 2. 주연 배우에게 대역 배우들 주입!!!
    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("signup 실패 - 이미 존재하는 이메일이면 예외")
    void signup_fail_when_email_already_exists() {
        // given: 상황 세팅
        SignupRequest request = mock(SignupRequest.class);

        // 대역 배우 훈련(Stubbing)시키기
        given(request.getEmail()).willReturn("hyunji@email.com");
        given(userRepository.existsByEmail("hyunji@email.com")).willReturn(true);

        // when & then 실행했을 때, 터져야만 테스트 통과!
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        then(passwordEncoder).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
        then(jwtUtil).shouldHaveNoInteractions();

    }

    @Test
    @DisplayName("signup 성공 - 유저 저장 후 토큰을 담아서 반환")
    void signup_success() {

        // given
        SignupRequest request = mock(SignupRequest.class);
        given(request.getEmail()).willReturn("hyunji@email.com");
        given(request.getPassword()).willReturn("rawPassword");
        given(request.getUserRole()).willReturn("USER");

        given(userRepository.existsByEmail("hyunji@email.com")).willReturn(false);
        given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");

        // save 결과로 돌아오는 저장된 유저를 mock으로 만들어서 id, email, role 응답을 세팅
        User savedUser = mock(User.class);
        given(savedUser.getId()).willReturn(1L);
        given(savedUser.getEmail()).willReturn("hyunji@email.com");

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        given(jwtUtil.createToken(1L, "hyunji@email.com", UserRole.USER)).willReturn("bearer token");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBearerToken()).isEqualTo("bearer token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());
        User newUser = userCaptor.getValue();
        assertThat(newUser.getEmail()).isEqualTo("hyunji@email.com");
        assertThat(newUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(newUser.getUserRole()).isEqualTo(UserRole.USER);

        then(jwtUtil).should().createToken(1L, "hyunji@email.com", UserRole.USER);

    }

    @Test
    @DisplayName("signin 실패 - 가입되지 않은 이메일이면 예외")
    void signin_fail_user_not_found() {

        // given
        SigninRequest request = mock(SigninRequest.class);
        given(request.getEmail()).willReturn("nono@email.com");
        given(userRepository.findByEmail("nono@email.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("가입되지 않은 유저입니다.");

        then(passwordEncoder).shouldHaveNoInteractions();
        then(jwtUtil).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("signin 실패 - 비밀번호 불일치면 AuthException")
    void signin_fail_wrong_password() {

        // given
        SigninRequest request = mock(SigninRequest.class);
        given(request.getEmail()).willReturn("hyunji@email.com");
        given(request.getPassword()).willReturn("wrongPassword");

        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");

        given(userRepository.findByEmail("hyunji@email.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(AuthException.class)
                .hasMessage("잘못된 비밀번호입니다.");

        then(jwtUtil).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("signin 성공 - 토큰을 담아서 반환")
    void signin_success() {

        // given
        SigninRequest request = mock(SigninRequest.class);
        given(request.getEmail()).willReturn("hyunji@email.com");
        given(request.getPassword()).willReturn("correctPassword");

        User user = mock(User.class);
        given(user.getId()).willReturn(10L);
        given(user.getEmail()).willReturn("hyunji@email.com");
        given(user.getUserRole()).willReturn(UserRole.USER);
        given(user.getPassword()).willReturn("encodedPassword");

        given(userRepository.findByEmail("hyunji@email.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("correctPassword", "encodedPassword")).willReturn(true);

        given(jwtUtil.createToken(10L, "hyunji@email.com", UserRole.USER)).willReturn("bearer.signin.token");

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBearerToken()).isEqualTo("bearer.signin.token");

        then(jwtUtil).should().createToken(10L, "hyunji@email.com", UserRole.USER);
    }
}
