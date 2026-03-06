package org.example.expert.domain.user.controller;

import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAdminControllerTest {

    private final UserAdminService userAdminService = mock(UserAdminService.class);
    private final UserAdminController userAdminController = new UserAdminController(userAdminService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userAdminController).build();

    @Test
    @DisplayName("PATCH /admin/users/{userId} 요청이 들어오면 유저 권한 변경을 위임한다")
    void changeUserRole_success() throws Exception {

        // given
        String requestBody = """
                {
                  "userRole": "ADMIN"
                }
                """;

        // when
        mockMvc.perform(patch("/admin/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // then
        then(userAdminService).should().changeUserRole(
                org.mockito.ArgumentMatchers.eq(1L),
                any(UserRoleChangeRequest.class)
        );
    }
}