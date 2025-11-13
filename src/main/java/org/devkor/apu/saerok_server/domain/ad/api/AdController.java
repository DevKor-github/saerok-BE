package org.devkor.apu.saerok_server.domain.ad.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdEventRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdEventStatusResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.GetAdSlotResponse;
import org.devkor.apu.saerok_server.domain.ad.application.AdEventService;
import org.devkor.apu.saerok_server.domain.ad.application.AdSelectionResult;
import org.devkor.apu.saerok_server.domain.ad.application.AdSelectorService;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdEventType;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ad API", description = "클라이언트 광고 노출 및 이벤트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/ad")
public class AdController {

    private final AdSelectorService adSelectorService;
    private final AdEventService adEventService;
    private final ImageDomainService imageDomainService;

    @GetMapping("/slots/{slotName}")
    @Operation(
            summary = "슬롯 단위 광고 요청",
            description = """
                    주어진 슬롯 이름에 대해 광고를 하나 선택해서 반환합니다.<br>
                    - 슬롯의 fallbackRatio에 따라 FALLBACK 또는 AD를 응답합니다.<br>
                    - AD인 경우 이미지 URL과 클릭 시 이동할 URL을 함께 내려줍니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "응답 성공",
                            content = @Content(schema = @Schema(implementation = GetAdSlotResponse.class))),
                    @ApiResponse(responseCode = "404", description = "슬롯을 찾을 수 없음", content = @Content)
            }
    )
    public GetAdSlotResponse getSlotAd(
            @PathVariable String slotName
    ) {
        AdSelectionResult result = adSelectorService.selectAdForSlot(slotName);

        if (result.isFallback()) {
            return new GetAdSlotResponse("FALLBACK", result.ttlSeconds(), null);
        }

        Ad ad = result.ad();
        String imageUrl = imageDomainService.toUploadImageUrl(ad.getObjectKey());

        GetAdSlotResponse.AdPayload payload = new GetAdSlotResponse.AdPayload(
                ad.getId(),
                imageUrl,
                ad.getTargetUrl()
        );

        return new GetAdSlotResponse("AD", result.ttlSeconds(), payload);
    }

    @PostMapping("/event/impression")
    @Operation(
            summary = "광고 노출 이벤트 기록",
            description = """
                    광고 이미지가 실제로 렌더링되었을 때 호출해 주세요.<br>
                    deviceId는 서버에서 SHA-256 해싱 후 deviceHash로 저장합니다.<br>
                    동일 기기에서 10초 내에 같은 슬롯의 같은 광고가 노출된 기록은 중복 기록되지 않습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "기록 성공",
                            content = @Content(schema = @Schema(implementation = AdEventStatusResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "광고를 찾을 수 없음", content = @Content)
            }
    )
    public AdEventStatusResponse logImpression(
            @Valid @RequestBody AdEventRequest request
    ) {
        adEventService.logEvent(
                AdEventType.IMPRESSION,
                request.adId(),
                request.slotName(),
                request.deviceId()
        );
        return new AdEventStatusResponse("ok");
    }

    @PostMapping("/event/click")
    @Operation(
            summary = "광고 클릭 이벤트 기록",
            description = """
                    광고 클릭 시 호출해 주세요.<br>
                    deviceId는 서버에서 SHA-256 해싱 후 deviceHash로 저장합니다.<br>
                    동일 기기에서 10초 내에 같은 슬롯의 같은 광고를 클릭한 기록은 중복 기록되지 않습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "기록 성공",
                            content = @Content(schema = @Schema(implementation = AdEventStatusResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "광고를 찾을 수 없음", content = @Content)
            }
    )
    public AdEventStatusResponse logClick(
            @Valid @RequestBody AdEventRequest request
    ) {
        adEventService.logEvent(
                AdEventType.CLICK,
                request.adId(),
                request.slotName(),
                request.deviceId()
        );
        return new AdEventStatusResponse("ok");
    }
}
