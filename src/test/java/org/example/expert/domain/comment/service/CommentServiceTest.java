package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.request.CommentUpdateRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    public void comment_목록을_정상적으로_조회한다() {
        // given
        long todoId = 1L;

        User user = new User("user1@example.com", "password", UserRole.USER);
        Comment comment = new Comment("댓글 내용", user, null);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(java.util.List.of(comment));

        // when
        java.util.List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("댓글 내용", result.get(0).getContents());
        assertEquals("user1@example.com", result.get(0).getUser().getEmail());
    }

    @Test
    void comment_수정_중_댓글을_찾지_못해_에러가_발생한다() {
        // given
        long commentId = 1L;
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        CommentUpdateRequest request = mock(CommentUpdateRequest.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.updateComment(authUser, commentId, request);
        });

        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    void comment_수정_중_작성자가_아니면_에러가_발생한다() {
        // given
        long commentId = 1L;
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        User anotherUser = new User("other@email.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(anotherUser, "id", 999L);

        Comment comment = new Comment("원래 댓글", anotherUser, null);
        CommentUpdateRequest request = mock(CommentUpdateRequest.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.updateComment(authUser, commentId, request);
        });

        // then
        assertEquals("댓글을 수정할 권한이 없습니다.", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_수정한다() {
        // given
        long commentId = 1L;
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        User user = User.fromAuthUser(authUser);
        Comment comment = new Comment("원래 댓글", user, null);
        CommentUpdateRequest request = mock(CommentUpdateRequest.class);
        given(request.getContents()).willReturn("수정된 댓글");

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.updateComment(authUser, commentId, request);

        // then
        assertEquals("수정된 댓글", comment.getContents());
    }
}
