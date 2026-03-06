package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    // 1. 대역 배우 캐스팅
    @Mock
    private UserRepository userRepository;

    // 2. 주연 배우에게 대역 배우 주입
    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("changeUserRole 실패 - 유저가 존재하지 않으면 예외가 발생한다")
    void changeUserRole_fail_when_user_not_found() {
        // given: 상황 세팅
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = mock(UserRoleChangeRequest.class);

        // 대역 배우 훈련(Stubbing)시키기
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then: 유저가 없으면 예외가 터져야 한다
        assertThatThrownBy(() -> userAdminService.changeUserRole(userId, userRoleChangeRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("changeUserRole 성공 - 유저가 존재하면 요청한 권한으로 변경한다")
    void changeUserRole_success() {
        // given: 상황 세팅
        long userId = 1L;

        User user = mock(User.class);
        UserRoleChangeRequest userRoleChangeRequest = mock(UserRoleChangeRequest.class);

        // 대역 배우 훈련(Stubbing)시키기
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRoleChangeRequest.getRole()).willReturn("ADMIN");

        // when: 권한 변경을 요청한다
        userAdminService.changeUserRole(userId, userRoleChangeRequest);

        // then: 주연 배우인 서비스가 유저를 찾고 요청한 권한 문자열을 기반으로 user.updateRole(...)을 호출하는지 확인한다.
        then(userRepository).should().findById(userId);
        then(user).should().updateRole(UserRole.ADMIN);
    }
}
