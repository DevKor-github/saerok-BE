package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;

import java.util.List;

@Schema(description = "커뮤니티 메인 화면 조회 응답")
public record GetCommunityMainResponse(
        @Schema(description = "최근에 올라온 새록 목록 (최대 3개)")
        List<CommunityCollectionInfo> recentCollections,
        
        @Schema(description = "요즘 인기 있는 새록 목록 (최대 3개)")
        List<CommunityCollectionInfo> popularCollections,
        
        @Schema(description = "동정 요청 새록 목록 (최대 3개)")
        List<CommunityCollectionInfo> pendingCollections
) {}
