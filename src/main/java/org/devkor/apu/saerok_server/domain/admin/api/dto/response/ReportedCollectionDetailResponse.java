package org.devkor.apu.saerok_server.domain.admin.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;

@Schema(description = "신고된 새록 상세(새록 + 댓글 목록)")
public record ReportedCollectionDetailResponse(
        Long reportId,
        GetCollectionDetailResponse collection,
        GetCollectionCommentsResponse comments
) {}
