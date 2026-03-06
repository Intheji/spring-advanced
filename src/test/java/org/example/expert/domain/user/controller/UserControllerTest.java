package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class UserControllerTest {

    // 1. 대역 배우 캐스팅!!!
    private final UserService userService = mock(UserService.class);

    // 2. 주연 배우에게 대역 배우 주입!!!
    private final UserController userController = new UserController(userService);

    @Test
    @DisplayName("GET /users/{userId} 요청이 들어오면 유저 정보를 반환한다")
    void getUser_success() {
        // given: 상황 세팅
        UserResponse userResponse = new UserResponse(1L, "hyunji@email.com");
        given(userService.getUser(1L)).willReturn(userResponse);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<UserResponse> response = userController.getUser(1L);

        // then: 서비스가 반환한 유저 정보를 ResponseEntity.ok로 잘 감싸서 반환하는지 확인
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getEmail()).isEqualTo("hyunji@email.com");

        then(userService).should().getUser(1L);
    }

    @Test
    @DisplayName("PUT /users 요청이 들어오면 로그인한 유저 id로 비밀번호 변경을 위임한다")
    void changePassword_success() {
        // given: 상황 세팅
        // @Auth 로 주입받는 로그인 사용자 정보를 직접 만들어 준다
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);

        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);

        // when: 컨트롤러 메서드를 직접 호출한다
        userController.changePassword(authUser, request);

        // then: 컨트롤러가 authUser 에서 id 를 꺼내 서비스에 잘 넘겼는지 확인한다
        then(userService).should().changePassword(1L, request);
    }
}
