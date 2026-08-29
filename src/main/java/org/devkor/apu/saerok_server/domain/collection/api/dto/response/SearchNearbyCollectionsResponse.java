package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "새 이름으로 검색한 주위 컬렉션 조회 응답 DTO")
public class SearchNearbyCollectionsResponse {

    @Schema(description = "검색 결과 컬렉션 목록")
    private List<GetNearbyCollectionsResponse.Item> items;

    @Schema(
            description = "최대 검색 반경까지 확장했지만 검색 결과가 없는지 여부",
            example = "false"
    )
    private boolean noResults;
}
