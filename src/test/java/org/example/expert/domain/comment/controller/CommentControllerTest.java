package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.request.CommentUpdateRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class CommentControllerTest {

    private final CommentService commentService = mock(CommentService.class);

    private final CommentController commentController = new CommentController(commentService);

    @Test
    @DisplayName("saveComment 요청이 들어오면 로그인한 유저 정보와 todoId, 요청 객체를 서비스에 넘기고 응답을 반환한다")
    void saveComment_success() {
        // given: 상황 세팅
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long todoId = 10L;
        CommentSaveRequest commentSaveRequest = mock(CommentSaveRequest.class);

        CommentSaveResponse commentSaveResponse = mock(CommentSaveResponse.class);
        given(commentService.saveComment(authUser, todoId, commentSaveRequest)).willReturn(commentSaveResponse);

        // when: 컨트롤러 메서드를 직접 호출한다
        ResponseEntity<CommentSaveResponse> response =
                commentController.saveComment(authUser, todoId, commentSaveRequest);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(commentSaveResponse);

        then(commentService).should().saveComment(authUser, todoId, commentSaveRequest);
    }

    @Test
    @DisplayName("getComments 요청이 들어오면 todoId를 서비스에 넘기고 댓글 목록 응답을 반환한다")
    void getComments_success() {
        // given
        long todoId = 10L;
        List<CommentResponse> commentResponses = List.of(mock(CommentResponse.class));

        given(commentService.getComments(todoId)).willReturn(commentResponses);

        // when
        ResponseEntity<List<CommentResponse>> response = commentController.getComments(todoId);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(commentResponses);

        then(commentService).should().getComments(todoId);
    }

    @Test
    @DisplayName("updateComment 요청이 들어오면 로그인한 유저 정보와 commentId, 요청 객체를 서비스에 넘긴다")
    void updateComment_success() {
        // given
        AuthUser authUser = new AuthUser(1L, "hyunji@email.com", UserRole.USER);
        long commentId = 10L;
        CommentUpdateRequest commentUpdateRequest = mock(CommentUpdateRequest.class);

        // when
        commentController.updateComment(authUser, commentId, commentUpdateRequest);

        // then
        then(commentService).should().updateComment(authUser, commentId, commentUpdateRequest);
    }
}
