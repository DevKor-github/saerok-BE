package org.devkor.apu.saerok_server.domain.community.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityUserInfo;

import java.util.List;

@Schema(description = "커뮤니티 검색 결과 응답")
public record GetCommunitySearchResponse(
        @Schema(description = "새록 검색 결과 총 개수", example = "543")
        Long collectionsCount,
        
        @Schema(description = "새록 검색 결과 (최대 3개)")
        List<CommunityCollectionInfo> collections,
        
        @Schema(description = "사용자 검색 결과 총 개수", example = "5")
        Long usersCount,
        
        @Schema(description = "사용자 검색 결과 (최대 3개)")
        List<CommunityUserInfo> users
) {}
