package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;

import java.util.List;

@Schema(description = "커뮤니티 컬렉션 목록 조회 응답")
public record GetCommunityCollectionsResponse(
        @Schema(description = "컬렉션 목록")
        List<CommunityCollectionInfo> items
) {}
