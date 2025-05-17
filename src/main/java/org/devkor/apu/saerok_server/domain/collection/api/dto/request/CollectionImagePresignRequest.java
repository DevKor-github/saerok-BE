package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionImagePresignRequest {

    @Schema(
            description = "업로드할 이미지의 Content-Type입니다. 예: image/jpeg, image/png 등. 이 값은 이후 실제 PUT 업로드 시 Content-Type 헤더와 반드시 일치시켜야 합니다.",
            example = "image/png",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String contentType;
}
