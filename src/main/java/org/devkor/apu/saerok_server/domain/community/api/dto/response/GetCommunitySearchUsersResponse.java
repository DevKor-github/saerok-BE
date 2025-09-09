package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "커뮤니티 사용자 검색 결과 응답")
public class GetCommunitySearchUsersResponse {

    @Schema(description = "사용자 검색 결과")
    private List<UserSearchResult> items;

    @Data
    @Schema(name = "GetCommunitySearchUsersResponse.UserSearchResult")
    public static class UserSearchResult {
        @Schema(description = "사용자 ID", example = "10")
        private Long userId;

        @Schema(description = "닉네임", example = "안암동새록마스터")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/10.jpg")
        private String profileImageUrl;
    }
}
