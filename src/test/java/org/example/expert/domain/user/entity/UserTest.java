package org.example.expert.domain.user.entity;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class UserTest {

    @Test
    @DisplayName("User 생성자 - 이메일/비밀번호/권한이 정상 세팅된다")
    void user_constructor_sets_fields() {
        // given
        String email = "test@test.com";
        String password = "pw";
        UserRole role = UserRole.USER;

        // when
        User user = new User(email, password, role);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getUserRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("fromAuthUser - AuthUser 정보를 기반으로 User를 생성한다")
    void fromAuthUser_creates_user() {
        // given
        AuthUser authUser = mock(AuthUser.class);
        given(authUser.getId()).willReturn(1L);
        given(authUser.getEmail()).willReturn("auth@test.com");
        given(authUser.getUserRole()).willReturn(UserRole.USER);

        // when
        User user = User.fromAuthUser(authUser);

        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("auth@test.com");
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("changePassword - 비밀번호가 변경된다")
    void changePassword_updates_password() {
        // given
        User user = new User("test@test.com", "oldPw", UserRole.USER);

        // when
        user.changePassword("newPw");

        // then
        assertThat(user.getPassword()).isEqualTo("newPw");
    }

    @Test
    @DisplayName("updateRole - 권한이 변경된다")
    void updateRole_updates_userRole() {
        // given
        User user = new User("test@test.com", "pw", UserRole.USER);

        // when
        user.updateRole(UserRole.ADMIN);

        // then
        assertThat(user.getUserRole()).isEqualTo(UserRole.ADMIN);
    }
}