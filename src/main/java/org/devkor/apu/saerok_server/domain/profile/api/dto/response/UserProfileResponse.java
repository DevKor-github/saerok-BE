package org.devkor.apu.saerok_server.domain.profile.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "사용자 프로필 조회 응답")
public record UserProfileResponse(

        @Schema(description = "닉네임", example = "새덕후99", requiredMode = Schema.RequiredMode.REQUIRED)
        String nickname,

        @Schema(description = "가입일", example = "2024-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate joinedDate,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.saerok.dev/user-profile-images/42.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        String profileImageUrl,

        @Schema(description = "컬렉션(새록) 개수", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long collectionCount,

        @Schema(description = "컬렉션(새록) 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CollectionItem> collections
) {
    @Schema(name = "UserProfileResponse.CollectionItem", description = "사용자의 새록(컬렉션) 항목")
    public record CollectionItem(

            @Schema(description = "컬렉션 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
            Long collectionId,

            @Schema(description = "조류 ID (없을 수 있음)", example = "33", nullable = true)
            Long birdId,

            @Schema(description = "조류의 한국어 이름", example = "노랑턱멧새", nullable = true)
            String birdKoreanName,

            @Schema(description = "조류의 학명", example = "Emberiza sulphurata", nullable = true)
            String birdScientificName,

            @Schema(description = "대표 이미지 URL (컬렉션의 첫 번째 이미지)", example = "https://cdn.saerok.dev/images/collection/101/main.jpg", nullable = true)
            String imageUrl,

            @Schema(description = "사용자가 작성한 한줄평", example = "처음 본 새인데 정말 귀여웠다!", nullable = true)
            String note,

            @Schema(description = "이 새를 실제로 발견한 날짜", example = "2025-05-16", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate discoveredDate,

            @Schema(description = "컬렉션을 업로드한 날짜", example = "2025-05-17", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate uploadedDate
    ) {}
}
