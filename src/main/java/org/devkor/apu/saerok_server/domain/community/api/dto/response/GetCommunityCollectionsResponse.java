package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "커뮤니티 컬렉션 목록 조회 응답")
public class GetCommunityCollectionsResponse {

    @Schema(description = "컬렉션 목록")
    private List<CollectionItem> items;

    @Data
    @Schema(name = "GetCommunityCollectionsResponse.CollectionItem")
    public static class CollectionItem {
        @Schema(description = "컬렉션 ID", example = "1")
        private Long collectionId;

        @Schema(description = "이미지 URL", example = "https://cdn.example.com/collection-images/1.jpg")
        private String imageUrl;

        @Schema(description = "발견 날짜", example = "2024-03-15")
        private LocalDate discoveredDate;

        @Schema(description = "위도", example = "37.987654")
        private Double latitude;

        @Schema(description = "경도", example = "127.123456")
        private Double longitude;

        @Schema(description = "위치 별칭", example = "서울숲")
        private String locationAlias;

        @Schema(description = "주소", example = "서울시 성동구 성수동")
        private String address;

        @Schema(description = "한 줄 평", example = "광화문에서 까치가 날아다녔어요")
        private String note;

        @Schema(description = "좋아요 수", example = "15")
        private Long likeCount;

        @Schema(description = "댓글 수", example = "7")
        private Long commentCount;

        @Schema(description = "내가 좋아요 눌렀는지 여부", example = "true")
        private Boolean isLiked;

        @Schema(description = "새 정보")
        private BirdInfo bird;

        @Schema(description = "사용자 정보")
        private UserInfo user;
    }

    @Data
    @Schema(name = "GetCommunityCollectionsResponse.BirdInfo")
    public static class BirdInfo {
        @Schema(description = "새 ID", example = "1")
        private Long birdId;

        @Schema(description = "새 한국어 이름", example = "까치")
        private String koreanName;
    }

    @Data
    @Schema(name = "GetCommunityCollectionsResponse.UserInfo")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "10")
        private Long userId;

        @Schema(description = "닉네임", example = "안암동새록마스터")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/10.jpg")
        private String profileImageUrl;
    }
}
