package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoUpdateRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class TodoControllerTest {

    // 1. 대역 배우 캐스팅!
    private final TodoService todoService = mock(TodoService.class);

    // 2. 주연 배우에게 대역 배우 주입!
    private final TodoController todoController = new TodoController(todoService);

    @Test
    @DisplayName("saveTodo 요청이 들어오면 로그인한 유저 정보와 요청 객체를 서비스에 넘기고 응답을 반환한다")
    void saveTodo_success() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = mock(TodoSaveRequest.class);

        TodoSaveResponse todoSaveResponse = mock(TodoSaveResponse.class);
        given(todoService.saveTodo(authUser, todoSaveRequest)).willReturn(todoSaveResponse);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<TodoSaveResponse> response = todoController.saveTodo(authUser, todoSaveRequest);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(todoSaveResponse);

        then(todoService).should().saveTodo(authUser, todoSaveRequest);
    }

    @Test
    @DisplayName("getTodos 요청이 들어오면 page 와 size 를 서비스에 넘기고 페이지 응답을 반환한다")
    void getTodos_success() {
        // given: 상황 세팅
        Page<TodoResponse> todoResponsePage = new PageImpl<>(List.of());
        given(todoService.getTodos(1, 10)).willReturn(todoResponsePage);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(1, 10);

        // then: page 와 size 가 서비스에 잘 전달되고 응답이 정상 반환되는지 확인한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(todoResponsePage);

        then(todoService).should().getTodos(1, 10);
    }

    @Test
    @DisplayName("getTodo 요청이 들어오면 todoId 를 서비스에 넘기고 상세 응답을 반환한다")
    void getTodo_success() {
        // given: 상황 세팅
        TodoResponse todoResponse = mock(TodoResponse.class);
        given(todoService.getTodo(10L)).willReturn(todoResponse);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<TodoResponse> response = todoController.getTodo(10L);

        // then: todoId 가 서비스에 잘 전달되고 응답이 정상 반환되는지 확인한다.
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(todoResponse);

        then(todoService).should().getTodo(10L);
    }

    @Test
    @DisplayName("updateTodo 요청이 들어오면 로그인한 유저 정보와 todoId, 요청 객체를 서비스에 넘긴다")
    void updateTodo_success() {
        // given
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long todoId = 10L;
        TodoUpdateRequest todoUpdateRequest = mock(TodoUpdateRequest.class);

        // when
        todoController.updateTodo(authUser, todoId, todoUpdateRequest);

        // then
        then(todoService).should().updateTodo(authUser, todoId, todoUpdateRequest);
    }
}
