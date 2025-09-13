package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityPendingCollectionInfo;

import java.util.List;

@Schema(description = "커뮤니티 동정 요청 컬렉션 조회 응답")
public record GetCommunityPendingCollectionsResponse(
        @Schema(description = "동정 요청 컬렉션 목록")
        List<CommunityPendingCollectionInfo> items
) {}
