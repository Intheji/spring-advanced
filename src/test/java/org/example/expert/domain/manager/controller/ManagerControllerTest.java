package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class ManagerControllerTest {

    // 1. 대역 배우 캐스팅
    private final ManagerService managerService = mock(ManagerService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);

    // 2. 주연 배우에게 대역 배우 주입
    private final ManagerController managerController = new ManagerController(managerService, jwtUtil);

    @Test
    @DisplayName("saveManager 요청이 들어오면 로그인한 유저 정보와 todoId, 요청 객체를 서비스에 넘기고 응답을 반환한다")
    void saveManager_success() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long todoId = 10L;
        ManagerSaveRequest managerSaveRequest = mock(ManagerSaveRequest.class);

        ManagerSaveResponse managerSaveResponse = mock(ManagerSaveResponse.class);
        given(managerService.saveManager(authUser, todoId, managerSaveRequest)).willReturn(managerSaveResponse);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<ManagerSaveResponse> response =
                managerController.saveManager(authUser, todoId, managerSaveRequest);

        // then: 주연 배우인 컨트롤러가 대역 배우인 서비스에게
        // authUser, todoId, 요청 객체를 잘 넘기고 응답을 정상 반환하는지 확인한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(managerSaveResponse);

        then(managerService).should().saveManager(authUser, todoId, managerSaveRequest);
    }

    @Test
    @DisplayName("getMembers 요청이 들어오면 todoId를 서비스에 넘기고 담당자 목록 응답을 반환한다")
    void getMembers_success() {
        // given: 상황 세팅
        long todoId = 10L;
        List<ManagerResponse> managerResponses = List.of(mock(ManagerResponse.class));

        given(managerService.getManagers(todoId)).willReturn(managerResponses);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<List<ManagerResponse>> response = managerController.getMembers(todoId);

        // then: 주연 배우인 컨트롤러가 대역 배우인 서비스에게
        // todoId를 잘 넘기고 목록 응답을 정상 반환하는지 확인한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(managerResponses);

        then(managerService).should().getManagers(todoId);
    }

    @Test
    @DisplayName("deleteManager 요청이 들어오면 Authorization 헤더에서 userId를 추출해 서비스에 넘긴다")
    void deleteManager_success() {
        // given: 상황 세팅
        String bearerToken = "Bearer access.token";
        long todoId = 10L;
        long managerId = 20L;

        Claims claims = mock(Claims.class);

        // 대역 배우 훈련(Stubbing)시키기
        // 컨트롤러는 Bearer 접두사를 잘라낸 뒤 jwtUtil.extractClaims(...) 를 호출한다.
        given(jwtUtil.extractClaims("access.token")).willReturn(claims);
        given(claims.getSubject()).willReturn("1");

        // when: 컨트롤러 메서드를 직접 호출한다
        managerController.deleteManager(bearerToken, todoId, managerId);

        // then: 토큰에서 추출한 userId 와 함께 서비스에 삭제 요청을 잘 넘겼는지 확인한다.
        then(jwtUtil).should().extractClaims("access.token");
        then(managerService).should().deleteManager(1L, todoId, managerId);
    }
}
