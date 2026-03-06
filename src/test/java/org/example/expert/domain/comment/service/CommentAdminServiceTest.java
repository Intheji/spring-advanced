package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class CommentAdminServiceTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final CommentAdminService commentAdminService = new CommentAdminService(commentRepository);

    @Test
    @DisplayName("deleteComment 요청이 들어오면 commentId로 댓글 삭제를 위임한다")
    void deleteComment_success() {
        // given
        long commentId = 10L;

        // when
        commentAdminService.deleteComment(commentId);

        // then
        then(commentRepository).should().deleteById(commentId);
    }
}