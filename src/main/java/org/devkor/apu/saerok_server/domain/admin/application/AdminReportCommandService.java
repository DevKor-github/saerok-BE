package org.devkor.apu.saerok_server.domain.admin.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.TransactionUtils;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportCommandService {

    private final CollectionReportRepository collectionReportRepository;
    private final CollectionCommentReportRepository commentReportRepository;

    private final CollectionRepository collectionRepository;
    private final CollectionCommentRepository commentRepository;
    private final CollectionImageRepository collectionImageRepository;

    private final ImageService imageService;

    /* ───────────── 신고 무시 ───────────── */

    public void ignoreCollectionReport(Long reportId) {
        if (!collectionReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 새록 신고가 없어요");
        }
    }

    public void ignoreCommentReport(Long reportId) {
        if (!commentReportRepository.deleteById(reportId)) {
            throw new NotFoundException("해당 ID의 댓글 신고가 없어요");
        }
    }

    /* ───────────── 신고 대상 삭제 ───────────── */

    public void deleteCollectionByReport(Long reportId) {
        UserBirdCollectionReport report = collectionReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 새록 신고가 없어요"));

        Long collectionId = report.getCollection().getId();

        // 1) 해당 컬렉션에 대한 신고들 정리 (컬렉션 신고 + 해당 컬렉션의 모든 댓글 신고)
        collectionReportRepository.deleteByCollectionId(collectionId);
        commentReportRepository.deleteByCollectionId(collectionId);

        // 2) 컬렉션 이미지 S3 삭제 준비
        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collectionId);

        // 3) 컬렉션 삭제
        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 컬렉션이 존재하지 않아요"));
        collectionRepository.remove(collection);

        // 4) 트랜잭션 커밋 후 S3 이미지 삭제
        if (!objectKeys.isEmpty()) {
            TransactionUtils.runAfterCommitOrNow(() -> imageService.deleteAll(objectKeys));
        }
    }

    public void deleteCommentByReport(Long reportId) {
        UserBirdCollectionCommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 댓글 신고가 없어요"));

        Long commentId = report.getComment().getId();

        // 1) 해당 댓글에 대한 신고 정리
        commentReportRepository.deleteByCommentId(commentId);

        // 2) 댓글 삭제
        UserBirdCollectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("해당 댓글이 존재하지 않아요"));
        commentRepository.remove(comment);
    }
}
