package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("signup 요청이 들어오면 회원가입 응답을 반환한다")
    void signup_request_returns_signup_response() throws Exception {

        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "hyunji@email.com");
        ReflectionTestUtils.setField(request, "password", "password");
        ReflectionTestUtils.setField(request, "userRole", "USER");

        SignupResponse signupResponse = new SignupResponse("bearer.token");

        given(authService.signup(any(SignupRequest.class))).willReturn(signupResponse);

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("bearer.token"));

        then(authService).should().signup(any(SignupRequest.class));
    }

    @Test
    @DisplayName("signin 요청이 들어오면 로그인 응답을 반환한다")
    void signin_request_returns_signin_response() throws Exception {

        // given
        SigninRequest request = new SigninRequest();
        ReflectionTestUtils.setField(request, "email", "hyunji@email.com");
        ReflectionTestUtils.setField(request, "password", "12345678");

        SigninResponse signinResponse = new SigninResponse("bearer.token");

        given(authService.signin(any(SigninRequest.class))).willReturn(signinResponse);

        // when & then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("bearer.token"));

        then(authService).should().signin(any(SigninRequest.class));
    }



}
