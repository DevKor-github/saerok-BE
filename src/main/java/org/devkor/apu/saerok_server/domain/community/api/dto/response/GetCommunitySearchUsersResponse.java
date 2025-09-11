package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityUserInfo;

import java.util.List;

@Schema(description = "커뮤니티 사용자 검색 결과 응답")
public record GetCommunitySearchUsersResponse(
        @Schema(description = "사용자 검색 결과")
        List<CommunityUserInfo> items
) {}
