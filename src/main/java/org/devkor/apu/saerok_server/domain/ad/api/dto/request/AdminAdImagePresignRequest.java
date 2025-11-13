package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminAdImagePresignRequest {

    @Schema(
            description = "업로드할 광고 이미지의 Content-Type입니다. 예: image/png, image/jpeg",
            example = "image/png",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String contentType;
}
