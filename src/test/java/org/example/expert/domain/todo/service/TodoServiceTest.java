package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoUpdateRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    // 1. 대역 배우 캐스팅
    @Mock
    TodoRepository todoRepository;

    @Mock
    WeatherClient weatherClient;

    // 2. 주연 배우에게 대역 배우 주입
    @InjectMocks
    TodoService todoService;

    @Test
    @DisplayName("saveTodo 성공 - 로그인한 유저와 오늘 날씨를 기반으로 할 일을 저장하고 응답을 반환한다")
    void saveTodo_success() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);

        TodoSaveRequest todoSaveRequest = mock(TodoSaveRequest.class);
        given(todoSaveRequest.getTitle()).willReturn("스프링 공부");
        given(todoSaveRequest.getContents()).willReturn("테스트 코드 작성하기");

        // 대역 배우 훈련(Stubbing)시키기
        given(weatherClient.getTodayWeather()).willReturn("맑음");

        Todo savedTodo = mock(Todo.class);
        given(savedTodo.getId()).willReturn(100L);
        given(savedTodo.getTitle()).willReturn("스프링 공부");
        given(savedTodo.getContents()).willReturn("테스트 코드 작성하기");

        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("스프링 공부");
        assertThat(response.getContents()).isEqualTo("테스트 코드 작성하기");
        assertThat(response.getWeather()).isEqualTo("맑음");
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("hyunji@email.com");

        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        then(todoRepository).should().save(todoCaptor.capture());

        Todo newTodo = todoCaptor.getValue();
        assertThat(newTodo.getTitle()).isEqualTo("스프링 공부");
        assertThat(newTodo.getContents()).isEqualTo("테스트 코드 작성하기");
        assertThat(newTodo.getWeather()).isEqualTo("맑음");
        assertThat(newTodo.getUser().getId()).isEqualTo(1L);
        assertThat(newTodo.getUser().getEmail()).isEqualTo("hyunji@email.com");
    }

    @Test
    @DisplayName("getTodos 성공 - 페이지 조회 결과를 TodoResponse 페이지로 변환해서 반환한다")
    void getTodos_success() {
        // given: 상황 세팅
        User user = new User("hyunji@email.com", "encodedPassword", UserRole.USER);

        Todo todo = mock(Todo.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 6, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 3, 6, 11, 0);

        given(todo.getId()).willReturn(1L);
        given(todo.getTitle()).willReturn("할 일 제목");
        given(todo.getContents()).willReturn("할 일 내용");
        given(todo.getWeather()).willReturn("비");
        given(todo.getUser()).willReturn(user);
        given(todo.getCreatedAt()).willReturn(createdAt);
        given(todo.getModifiedAt()).willReturn(modifiedAt);

        Page<Todo> todoPage = new PageImpl<>(List.of(todo));

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todoPage);

        // when
        Page<TodoResponse> response = todoService.getTodos(1, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);

        TodoResponse todoResponse = response.getContent().get(0);
        assertThat(todoResponse.getId()).isEqualTo(1L);
        assertThat(todoResponse.getTitle()).isEqualTo("할 일 제목");
        assertThat(todoResponse.getContents()).isEqualTo("할 일 내용");
        assertThat(todoResponse.getWeather()).isEqualTo("비");
        assertThat(todoResponse.getUser().getEmail()).isEqualTo("hyunji@email.com");

        // page - 1
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(todoRepository).should().findAllByOrderByModifiedAtDesc(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("getTodo 실패 - 해당 할 일이 존재하지 않으면 예외가 발생한다")
    void getTodo_fail_when_todo_not_found() {
        // given: 상황 세팅
        given(todoRepository.findByIdWithUser(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");
    }

    @Test
    @DisplayName("getTodo 성공 - 할 일 상세 정보를 반환한다")
    void getTodo_success() {
        // given
        User user = new User("hyunji@email.com", "encodedPassword", UserRole.USER);

        Todo todo = mock(Todo.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 6, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 3, 6, 11, 0);

        given(todo.getId()).willReturn(10L);
        given(todo.getTitle()).willReturn("상세 제목");
        given(todo.getContents()).willReturn("상세 내용");
        given(todo.getWeather()).willReturn("흐림");
        given(todo.getUser()).willReturn(user);
        given(todo.getCreatedAt()).willReturn(createdAt);
        given(todo.getModifiedAt()).willReturn(modifiedAt);

        given(todoRepository.findByIdWithUser(10L)).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(10L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("상세 제목");
        assertThat(response.getContents()).isEqualTo("상세 내용");
        assertThat(response.getWeather()).isEqualTo("흐림");
        assertThat(response.getUser().getEmail()).isEqualTo("hyunji@email.com");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    @DisplayName("updateTodo 실패 - 해당 할 일이 존재하지 않으면 예외가 발생한다")
    void updateTodo_fail_when_todo_not_found() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long todoId = 1L;

        TodoUpdateRequest request = mock(TodoUpdateRequest.class);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(authUser, todoId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");
    }

    @Test
    @DisplayName("updateTodo 실패 - 일정 작성자가 아니면 예외가 발생한다")
    void updateTodo_fail_when_user_is_not_owner() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long todoId = 1L;

        TodoUpdateRequest request = mock(TodoUpdateRequest.class);

        User anotherUser = new User("other@email.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(anotherUser, "id", 999L);

        Todo todo = new Todo("기존 제목", "기존 내용", "맑음", anotherUser);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(authUser, todoId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("해당 일정을 수정할 권한이 없습니다.");
    }
    @Test
    @DisplayName("updateTodo 성공 - 일정 작성자이면 제목과 내용을 수정한다")
    void updateTodo_success() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("기존 제목", "기존 내용", "맑음", user);

        TodoUpdateRequest request = mock(TodoUpdateRequest.class);
        given(request.getTitle()).willReturn("수정 제목");
        given(request.getContents()).willReturn("수정 내용");

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        todoService.updateTodo(authUser, todoId, request);

        // then
        assertThat(todo.getTitle()).isEqualTo("수정 제목");
        assertThat(todo.getContents()).isEqualTo("수정 내용");
    }
}