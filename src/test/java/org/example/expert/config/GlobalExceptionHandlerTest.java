package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("InvalidRequestException 이 발생하면 400 응답을 반환한다")
    void invalidRequestExceptionException_returns_bad_request() {
        // given: 잘못된 요청 예외를 준비한다.
        InvalidRequestException exception = new InvalidRequestException("잘못된 요청입니다.");

        // when: 예외 핸들러를 직접 호출한다.
        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.invalidRequestExceptionException(exception);

        // then: 400 상태코드와 에러 응답 body 가 정확해야 한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().get("code")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("잘못된 요청입니다.");
    }

    @Test
    @DisplayName("AuthException 이 발생하면 401 응답을 반환한다")
    void handleAuthException_returns_unauthorized() {
        // given: 인증 예외를 준비한다.
        AuthException exception = new AuthException("인증이 필요합니다.");

        // when: 예외 핸들러를 직접 호출한다.
        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleAuthException(exception);

        // then: 401 상태코드와 에러 응답 body 가 정확해야 한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UNAUTHORIZED");
        assertThat(response.getBody().get("code")).isEqualTo(401);
        assertThat(response.getBody().get("message")).isEqualTo("인증이 필요합니다.");
    }

    @Test
    @DisplayName("ServerException 이 발생하면 500 응답을 반환한다")
    void handleServerException_returns_internal_server_error() {
        // given: 서버 예외를 준비한다.
        ServerException exception = new ServerException("서버 오류입니다.");

        // when: 예외 핸들러를 직접 호출한다.
        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleServerException(exception);

        // then: 500 상태코드와 에러 응답 body 가 정확해야 한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().get("code")).isEqualTo(500);
        assertThat(response.getBody().get("message")).isEqualTo("서버 오류입니다.");
    }

    @Test
    @DisplayName("getErrorResponse 는 전달받은 상태코드와 메시지로 에러 응답을 생성한다")
    void getErrorResponse_returns_expected_response() {
        // given: 공통 에러 응답 생성을 직접 검증하기 위한 상태코드와 메시지를 준비한다.
        // when: 공통 응답 생성 메서드를 직접 호출한다.
        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.getErrorResponse(org.springframework.http.HttpStatus.BAD_REQUEST, "공통 에러");

        // then: 상태코드와 body 값이 정확해야 한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().get("code")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("공통 에러");
    }
}