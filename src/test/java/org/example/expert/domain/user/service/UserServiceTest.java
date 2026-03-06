package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("getUser 실패 - 유저가 존재하지 않으면 예외가 발생한다")
    void getUser_fail_user_not_found() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("getUser 성공 - 유저 정보가 반환된다")
    void getUser_success() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getEmail()).willReturn("test@test.com");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("changePassword 실패 - 유저가 존재하지 않으면 예외가 발생한다")
    void changePassword_fail_user_not_found() {
        // given
        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");

        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("changePassword 실패 - 새 비밀번호가 기존 비밀번호와 같으면 예외가 발생한다")
    void changePassword_fail_new_password_same_as_old_password() {
        // given
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedOldPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);
        given(request.getNewPassword()).willReturn("newPassword");
        given(passwordEncoder.matches("newPassword", "encodedOldPassword")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");

        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("changePassword 실패 - 기존 비밀번호가 일치하지 않으면 예외가 발생한다")
    void changePassword_fail_old_password_not_match() {
        // given
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedOldPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);
        given(request.getNewPassword()).willReturn("newPassword");
        given(request.getOldPassword()).willReturn("wrongOldPassword");

        // 첫 번째 if: 새 비밀번호 == 기존 비밀번호 ? -> false
        given(passwordEncoder.matches("newPassword", "encodedOldPassword")).willReturn(false);

        // 두 번째 if: 기존 비밀번호 일치 ? -> false 이므로 예외
        given(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("잘못된 비밀번호입니다.");

        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("changePassword 성공 - 기존 비밀번호 확인 후 새 비밀번호로 변경한다")
    void changePassword_success() {
        // given
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedOldPassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);
        given(request.getNewPassword()).willReturn("newPassword");
        given(request.getOldPassword()).willReturn("oldPassword");

        // 첫 번째 if: 새 비밀번호 == 기존 비밀번호 ? -> false
        given(passwordEncoder.matches("newPassword", "encodedOldPassword")).willReturn(false);

        // 두 번째 if: 기존 비밀번호 일치 ? -> true
        given(passwordEncoder.matches("oldPassword", "encodedOldPassword")).willReturn(true);

        // encode 결과
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

        // when
        userService.changePassword(1L, request);

        // then
        then(user).should().changePassword("encodedNewPassword");
    }
}
