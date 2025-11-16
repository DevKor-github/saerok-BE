package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "컬렉션 상세 조회 응답 DTO")
public class GetCollectionDetailResponse {

    @Schema(description = "컬렉션 ID", example = "1")
    private Long collectionId;

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/collection-images/1.jpg")
    private String imageUrl;

    @Schema(description = "관찰 날짜", example = "2024-05-21")
    private LocalDate discoveredDate;

    @Schema(description = "생성 날짜", example = "2024-05-21T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "관찰 위치 위도", example = "37.987654")
    private Double latitude;

    @Schema(description = "관찰 위치 경도", example = "127.123456")
    private Double longitude;

    @Schema(description = "관찰 위치 별칭", example = "서울숲")
    private String locationAlias;

    @Schema(description = "관찰 지점의 주소", example = "서울시 성동구 성수동")
    private String address;

    @Schema(description = "한 줄 평", example = "까치가 무리를 지어 날아다님")
    private String note;

    @Schema(description = "컬렉션 공개 범위 (공개/비공개)")
    private AccessLevelType accessLevel;

    @Schema(description = "좋아요 수", example = "15")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "7")
    private Long commentCount;

    @Schema(description = "내가 좋아요 눌렀는지 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "내 컬렉션인지 여부", example = "false")
    private Boolean isMine;

    @Schema(description = "새 정보")
    private BirdInfo bird;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Data
    @Schema(description = "새 정보")
    public static class BirdInfo {
        @Schema(description = "새 ID", example = "101")
        private Long birdId;

        @Schema(description = "새의 한글 이름", example = "까치")
        private String koreanName;

        @Schema(description = "새의 학명", example = "Pica pica")
        private String scientificName;
    }

    @Data
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "10")
        private Long userId;

        @Schema(description = "사용자 닉네임", example = "junhee")
        private String nickname;

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/10.jpg")
        private String profileImageUrl;

        @Schema(description = "썸네일 프로필 이미지 URL (320px 너비)", example = "https://cdn.example.com/thumbnails/user-profile-images/10.webp")
        private String thumbnailProfileImageUrl;
    }
}