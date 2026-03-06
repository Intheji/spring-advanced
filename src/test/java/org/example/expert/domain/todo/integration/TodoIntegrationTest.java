package org.example.expert.domain.todo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.client.WeatherClient;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class TodoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private WeatherClient weatherClient;

    private String user1Token;
    private String user2Token;

    private User user1;
    private User user2;

    private Todo savedTodo;

    @BeforeEach
    void setup() {
        user1 = userRepository.save(new User("hyunji@email.com", "1234", UserRole.USER));
        user2 = userRepository.save(new User("jiwon@email.com", "1004", UserRole.USER));

        savedTodo = todoRepository.save(new Todo("title", "contents", "SUNNY", user1));

        user1Token = jwtUtil.createToken(user1.getId(), user1.getEmail(), user1.getUserRole());
        user2Token = jwtUtil.createToken(user2.getId(), user2.getEmail(), user2.getUserRole());

        given(weatherClient.getTodayWeather()).willReturn("SUNNY");
    }

    @Test
    @DisplayName("성공 - todo 저장")
    void todo_save_success() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "집 가고 싶은 사람 손");
        request.put("contents", "집에 있는데 집에 가고 싶다");

        mockMvc.perform(post("/todos")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("집 가고 싶은 사람 손"))
                .andExpect(jsonPath("$.contents").value("집에 있는데 집에 가고 싶다"))
                .andExpect(jsonPath("$.weather").value("SUNNY"))
                .andExpect(jsonPath("$.user.id").value(user1.getId()))
                .andExpect(jsonPath("$.user.email").value(user1.getEmail()));
    }

    @Test
    @DisplayName("실패 - 인증 없이 todo 저장")
    void todo_save_failed_by_authUser_is_null() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "집 가고 싶은 사람 손");
        request.put("contents", "집에 있는데 집에 가고 싶다");

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - validation 오류")
    void todo_save_failed_by_validation() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("contents", "");

        mockMvc.perform(post("/todos")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - todo 목록 조회")
    void getTodos_succeed() throws Exception {
        todoRepository.save(new Todo("첫 번째 제목", "첫 번째 내용", "SUNNY", user1));
        todoRepository.save(new Todo("두 번째 제목", "두 번째 내용", "RAINY", user1));

        mockMvc.perform(get("/todos")
                        .header("Authorization", user1Token)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("성공 - todo 단건 조회")
    void getTodo_succeed() throws Exception {
        mockMvc.perform(get("/todos/{todoId}", savedTodo.getId())
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTodo.getId()))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.contents").value("contents"))
                .andExpect(jsonPath("$.weather").value("SUNNY"))
                .andExpect(jsonPath("$.user.id").value(user1.getId()))
                .andExpect(jsonPath("$.user.email").value(user1.getEmail()));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 todo 조회")
    void getTodo_failed_by_not_found() throws Exception {
        mockMvc.perform(get("/todos/{todoId}", 9999L)
                        .header("Authorization", user1Token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - todo 수정")
    void updateTodo_succeed() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "수정 제목");
        request.put("contents", "수정 내용");

        mockMvc.perform(put("/todos/{todoId}", savedTodo.getId())
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패 - 작성자가 아닌 유저는 수정할 수 없음")
    void updateTodo_failed_by_unauthorized() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "수정 제목");
        request.put("contents", "수정 내용");

        mockMvc.perform(put("/todos/{todoId}", savedTodo.getId())
                        .header("Authorization", user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 todo 수정")
    void updateTodo_failed_by_not_found() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", "수정 제목");
        request.put("contents", "수정 내용");

        mockMvc.perform(put("/todos/{todoId}", 9999L)
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
