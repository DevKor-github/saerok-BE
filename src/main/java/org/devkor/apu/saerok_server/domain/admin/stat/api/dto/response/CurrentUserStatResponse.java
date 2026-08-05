package org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "관리자용 현재 사용자 현황 응답 DTO")
public record CurrentUserStatResponse(
        @Schema(description = "현재 가입 완료 사용자 수", example = "1250")
        long completedUserCount,
        @Schema(description = "가입 경로별 현재 가입 완료 사용자 수. 가입 경로가 없는 사용자는 UNKNOWN 키로 반환")
        Map<String, Long> signupSourceCounts,
        @Schema(description = "플랫폼별 활성 푸시 토큰 보유 사용자 수. 한 사용자가 여러 플랫폼에 중복 포함될 수 있음")
        Map<String, Long> activePushUserCountsByPlatform
) {
}
