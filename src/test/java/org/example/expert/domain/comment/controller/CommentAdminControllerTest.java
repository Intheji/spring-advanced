package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class CommentAdminControllerTest {

    private final CommentAdminService commentAdminService = mock(CommentAdminService.class);

    private final CommentAdminController commentAdminController = new CommentAdminController(commentAdminService);

    @Test
    @DisplayName("deleteComment 요청이 들어오면 commentId를 서비스에 넘겨 삭제를 위임한다")
    void deleteComment_success() {
        // given: 상황 세팅
        long commentId = 10L;

        // when: 컨트롤러 메서드를 직접 호출한다
        commentAdminController.deleteComment(commentId);

        // then: 주연 배우인 컨트롤러가 대역 배우인 서비스에게 commentId를 잘 넘겨 삭제를 위임했는지 확인한다.
        then(commentAdminService).should().deleteComment(commentId);
    }
}
