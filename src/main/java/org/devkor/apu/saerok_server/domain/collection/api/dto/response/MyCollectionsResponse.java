package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Schema(description = "내 컬렉션 목록 조회 응답")
public class MyCollectionsResponse {
    private Long collectionId;
    private String imageUrl;
    private String birdName;
}
