package org.devkor.apu.saerok_server.domain.freeboard.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.ReportFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostReportRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FreeBoardPostReportCommandService {

    private final FreeBoardPostReportRepository freeBoardPostReportRepository;
    private final FreeBoardPostRepository freeBoardPostRepository;
    private final UserRepository userRepository;

    public ReportFreeBoardPostResponse reportPost(Long reporterId, Long postId) {

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        FreeBoardPost post = freeBoardPostRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        if (post.getUser().getId().equals(reporterId)) {
            throw new BadRequestException("자신의 게시글은 신고할 수 없어요");
        }

        boolean alreadyReported = freeBoardPostReportRepository.existsByReporterIdAndPostId(reporterId, postId);
        if (alreadyReported) {
            throw new BadRequestException("이미 신고한 게시글이에요");
        }

        FreeBoardPostReport report = FreeBoardPostReport.of(reporter, post.getUser(), post);

        freeBoardPostReportRepository.save(report);
        return new ReportFreeBoardPostResponse(report.getId());
    }
}
