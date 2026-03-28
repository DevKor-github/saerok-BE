package org.devkor.apu.saerok_server.domain.freeboard.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.ReportFreeBoardPostCommentResponse;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FreeBoardPostCommentReportCommandService {

    private final FreeBoardPostCommentReportRepository commentReportRepository;
    private final FreeBoardPostCommentRepository commentRepository;
    private final UserRepository userRepository;

    public ReportFreeBoardPostCommentResponse reportComment(Long reporterId, Long postId, Long commentId) {

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        FreeBoardPostComment comment = commentRepository.findByIdWithUserAndPost(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("해당 게시글에 속한 댓글이 아니에요");
        }

        if (comment.getUser().getId().equals(reporterId)) {
            throw new BadRequestException("자신의 댓글은 신고할 수 없어요");
        }

        boolean alreadyReported = commentReportRepository.existsByReporterIdAndCommentId(reporterId, commentId);
        if (alreadyReported) {
            throw new BadRequestException("이미 신고한 댓글이에요");
        }

        FreeBoardPostCommentReport report = FreeBoardPostCommentReport.of(reporter, comment.getUser(), comment);

        commentReportRepository.save(report);
        return new ReportFreeBoardPostCommentResponse(report.getId());
    }
}
