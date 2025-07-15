package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.ReportCollectionCommentResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentReportRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollectionCommentReportCommandService {

    private final CollectionCommentReportRepository commentReportRepository;
    private final CollectionCommentRepository commentRepository;
    private final UserRepository userRepository;

    public ReportCollectionCommentResponse reportComment(Long reporterId, Long collectionId, Long commentId) {

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getCollection().getId().equals(collectionId)) {
            throw new NotFoundException("해당 컬렉션에 속한 댓글이 아니에요");
        }

        if (comment.getUser().getId().equals(reporterId)) {
            throw new BadRequestException("자신의 댓글은 신고할 수 없어요");
        }

        // 중복 신고 방지
        boolean alreadyReported = commentReportRepository.existsByReporterIdAndCommentId(reporterId, commentId);
        if (alreadyReported) {
            throw new BadRequestException("이미 신고한 댓글이에요");
        }

        UserBirdCollectionCommentReport report = UserBirdCollectionCommentReport.of(reporter, comment.getUser(), comment);

        commentReportRepository.save(report);
        return new ReportCollectionCommentResponse(report.getId());
    }
}
