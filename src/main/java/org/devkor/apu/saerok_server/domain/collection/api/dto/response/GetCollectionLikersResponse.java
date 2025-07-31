package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "컬렉션을 좋아요한 사용자 목록 조회 응답 DTO")
public record GetCollectionLikersResponse(
        @Schema(description = "좋아요한 사용자 목록")
        List<Item> items
) {
    @Schema(name = "GetCollectionLikersResponse.Item")
    public record Item(
            @Schema(description = "사용자 ID", example = "10")
            Long userId,

            @Schema(description = "사용자 닉네임", example = "안암동새록마스터")
            String nickname,

            @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/profile-images/10.jpg")
            String profileImageUrl
    ) {}
}
