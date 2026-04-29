package org.devkor.apu.saerok_server.domain.admin.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자 사용자 목록 응답")
public record AdminUserListResponse(
        @Schema(description = "사용자 목록")
        List<Item> users,

        @Schema(description = "현재 페이지", example = "1")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 사용자 수", example = "120")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "6")
        int totalPages
) {

    public record Item(
            @Schema(description = "사용자 ID", example = "501")
            Long id,

            @Schema(description = "닉네임", example = "솔바람")
            String nickname
    ) {
    }
}
