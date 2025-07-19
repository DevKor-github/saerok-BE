package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.ReportCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionReportRepository;
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
public class CollectionReportCommandService {

    private final CollectionReportRepository collectionReportRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    public ReportCollectionResponse reportCollection(Long reporterId, Long collectionId) {

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 컬렉션 id예요"));

        if (collection.getUser().getId().equals(reporterId)) {
            throw new BadRequestException("자신의 컬렉션은 신고할 수 없어요");
        }

        // 중복 신고 방지
        boolean alreadyReported = collectionReportRepository.existsByReporterIdAndCollectionId(reporterId, collectionId);
        if (alreadyReported) {
            throw new BadRequestException("이미 신고한 컬렉션이에요");
        }

        UserBirdCollectionReport report = UserBirdCollectionReport.of(reporter, collection.getUser(), collection);

        collectionReportRepository.save(report);
        return new ReportCollectionResponse(report.getId());
    }
}
