package org.devkor.apu.saerok_server.domain.community.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "커뮤니티 컬렉션 공통 정보")
public record CommunityCollectionInfo(
        @Schema(description = "컬렉션 ID", example = "1")
        Long collectionId,
        
        @Schema(description = "이미지 URL", example = "https://cdn.example.com/collection-images/1.jpg")
        String imageUrl,
        
        @Schema(description = "발견 날짜", example = "2024-03-15")
        LocalDate discoveredDate,

        @Schema(description = "업로드 날짜", example = "2024-03-15T10:30:00")
        LocalDateTime uploadedDate,
        
        @Schema(description = "위도", example = "37.987654")
        Double latitude,
        
        @Schema(description = "경도", example = "127.123456")
        Double longitude,
        
        @Schema(description = "위치 별칭", example = "서울숲")
        String locationAlias,
        
        @Schema(description = "주소", example = "서울시 성동구 성수동")
        String address,
        
        @Schema(description = "한 줄 평", example = "광화문에서 까치가 날아다녔어요")
        String note,
        
        @Schema(description = "좋아요 수", example = "15")
        Long likeCount,
        
        @Schema(description = "댓글 수", example = "7")
        Long commentCount,
        
        @Schema(description = "내가 좋아요 눌렀는지 여부", example = "true")
        Boolean isLiked,
        
        @Schema(description = "인기 컬렉션 여부", example = "true")
        Boolean isPopular,
        
        @Schema(description = "동정 돕기에 참여한 유저 수 (동정 요청 컬렉션인 경우에만)", example = "5", nullable = true)
        Long suggestionUserCount,
        
        @Schema(description = "새 정보")
        BirdInfo bird,
        
        @Schema(description = "사용자 정보")
        UserInfo user
) {
    
    @Schema(name = "CommunityCollectionInfo.BirdInfo")
    public record BirdInfo(
            @Schema(description = "새 ID", example = "1")
            Long birdId,
            
            @Schema(description = "새 한국어 이름", example = "까치")
            String koreanName
    ) {}

    @Schema(name = "CommunityCollectionInfo.UserInfo")
    public record UserInfo(
            @Schema(description = "사용자 ID", example = "10")
            Long userId,
            
            @Schema(description = "닉네임", example = "안암동새록마스터")
            String nickname,
            
            @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/10.jpg")
            String profileImageUrl
    ) {}
}
