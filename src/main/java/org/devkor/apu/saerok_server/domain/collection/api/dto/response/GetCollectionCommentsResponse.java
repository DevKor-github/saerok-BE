package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record GetCollectionCommentsResponse(
    @Schema(description = "댓글 목록 (원댓글만 포함, 대댓글은 각 원댓글의 replies에 포함)")
    List<Item> items,
    @Schema(description = "내 컬렉션인지 여부", example = "true")
    Boolean isMyCollection,
    @Schema(description = "다음 페이지 존재 여부 (페이징 요청 시에만 유효, 미사용 시 null)", example = "true", nullable = true)
    Boolean hasNext
) {

    public record Item(
            @Schema(description = "댓글 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
            Long commentId,
            @Schema(description = "작성자 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
            Long userId,
            @Schema(description = "작성자 닉네임", example = "안암동새록마스터", requiredMode = Schema.RequiredMode.REQUIRED)
            String nickname,
            @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/3.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
            String profileImageUrl,
            @Schema(description = "작성자 썸네일 프로필 이미지 URL (320px 너비)", example = "https://cdn.example.com/thumbnails/user-profile-images/3.webp", requiredMode = Schema.RequiredMode.REQUIRED)
            String thumbnailProfileImageUrl,
            @Schema(description = "댓글 내용", example = "멋진 관찰 기록이네요!", requiredMode = Schema.RequiredMode.REQUIRED)
            String content,
            @Schema(description = "댓글 상태 (ACTIVE, DELETED, BANNED)", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
            String status,
            @Schema(description = "부모 댓글 ID (원댓글이면 null, 대댓글이면 부모 댓글 ID)", example = "null", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            Long parentId,
            @Schema(description = "좋아요 수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
            int likeCount,
            @Schema(description = "좋아요 눌렀는지 여부 (비로그인 시 false)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
            Boolean isLiked,
            @Schema(description = "내 댓글인지 여부 (비로그인 시 false)", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
            Boolean isMine,
            @Schema(description = "작성 시각", example = "2025-07-05T03:10:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime createdAt,
            @Schema(description = "수정 시각", example = "2025-07-05T04:21:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime updatedAt,
            @Schema(description = "대댓글 목록 (대댓글의 replies는 항상 빈 배열)", requiredMode = Schema.RequiredMode.REQUIRED)
            List<Item> replies
    ) {

    }
}
