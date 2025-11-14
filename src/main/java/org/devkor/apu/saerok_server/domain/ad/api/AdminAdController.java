package org.devkor.apu.saerok_server.domain.ad.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminAdImagePresignRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateAdPlacementRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateAdRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateSlotRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateAdPlacementRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateAdRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateSlotRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminAdListResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminAdPlacementListResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminSlotListResponse;
import org.devkor.apu.saerok_server.domain.ad.application.AdminAdPlacementService;
import org.devkor.apu.saerok_server.domain.ad.application.AdminAdService;
import org.devkor.apu.saerok_server.domain.ad.application.AdminSlotService;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Ad API", description = "광고 관리용 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/ad")
public class AdminAdController {

    private final AdminAdService adminAdService;
    private final AdminSlotService adminSlotService;
    private final AdminAdPlacementService adminAdPlacementService;
    private final ImageDomainService imageDomainService;

    /* ───────────── 광고(Ad) 관리 ───────────── */

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "광고 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdListResponse.class))
                    )
            }
    )
    public AdminAdListResponse listAds() {
        List<Ad> ads = adminAdService.listAds();

        List<AdminAdListResponse.Item> items = ads.stream()
                .map(ad -> new AdminAdListResponse.Item(
                        ad.getId(),
                        ad.getName(),
                        ad.getMemo(),
                        ad.getObjectKey() != null ? imageDomainService.toUploadImageUrl(ad.getObjectKey()) : null,
                        ad.getContentType(),
                        ad.getTargetUrl(),
                        ad.getCreatedAt(),
                        ad.getUpdatedAt()
                ))
                .toList();

        return new AdminAdListResponse(items);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 생성",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminAdListResponse.Item createAd(
            @Valid @RequestBody AdminCreateAdRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Ad ad = adminAdService.createAd(
                admin.getId(),
                request.name(),
                request.memo(),
                request.objectKey(),
                request.contentType(),
                request.targetUrl()
        );

        String imageUrl = ad.getObjectKey() != null ? imageDomainService.toUploadImageUrl(ad.getObjectKey()) : null;

        return new AdminAdListResponse.Item(
                ad.getId(),
                ad.getName(),
                ad.getMemo(),
                imageUrl,
                ad.getContentType(),
                ad.getTargetUrl(),
                ad.getCreatedAt(),
                ad.getUpdatedAt()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 수정",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminAdListResponse.Item updateAd(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAdRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Ad ad = adminAdService.updateAd(
                admin.getId(),
                id,
                request.name(),
                request.memo(),
                request.objectKey(),
                request.contentType(),
                request.targetUrl()
        );

        String imageUrl = ad.getObjectKey() != null ? imageDomainService.toUploadImageUrl(ad.getObjectKey()) : null;

        return new AdminAdListResponse.Item(
                ad.getId(),
                ad.getName(),
                ad.getMemo(),
                imageUrl,
                ad.getContentType(),
                ad.getTargetUrl(),
                ad.getCreatedAt(),
                ad.getUpdatedAt()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 삭제",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteAd(@PathVariable Long id,
                         @AuthenticationPrincipal UserPrincipal admin) {
        adminAdService.deleteAd(admin.getId(), id);
    }

    /* ───────────── 광고 이미지 Presigned URL ───────────── */

    @PostMapping("/image/presign")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 이미지 Presigned URL 발급",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    광고 배너 이미지를 업로드할 수 있도록 S3 Presigned URL을 발급합니다.
                    
                    1. 이 API를 호출해 presignedUrl, objectKey를 발급받는다.
                    2. 클라이언트가 presignedUrl로 이미지를 PUT 업로드한다.
                    3. 이후 광고 생성/수정 API에 objectKey, contentType 등을 포함해 호출한다.
                    """
    )
    public AdImagePresignResponse generateAdImagePresignUrl(
            @Valid @RequestBody AdminAdImagePresignRequest request
    ) {
        return adminAdService.generateAdImagePresignUrl(request.getContentType());
    }

    /* ───────────── 슬롯(Slot) 관리 ───────────── */

    @GetMapping("/slot")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "슬롯 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminSlotListResponse listSlots() {
        List<Slot> slots = adminSlotService.listSlots();

        List<AdminSlotListResponse.Item> items = slots.stream()
                .map(slot -> new AdminSlotListResponse.Item(
                        slot.getId(),
                        slot.getName(),
                        slot.getMemo(),
                        slot.getFallbackRatio(),
                        slot.getTtlSeconds(),
                        slot.getCreatedAt(),
                        slot.getUpdatedAt()
                ))
                .toList();

        return new AdminSlotListResponse(items);
    }

    @PostMapping("/slot")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "슬롯 생성",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminSlotListResponse.Item createSlot(
            @Valid @RequestBody AdminCreateSlotRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Slot slot = adminSlotService.createSlot(
                admin.getId(),
                request.getName(),
                request.getMemo(),
                request.getFallbackRatio(),
                request.getTtlSeconds()
        );

        return new AdminSlotListResponse.Item(
                slot.getId(),
                slot.getName(),
                slot.getMemo(),
                slot.getFallbackRatio(),
                slot.getTtlSeconds(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
    }

    @PutMapping("/slot/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "슬롯 수정",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminSlotListResponse.Item updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateSlotRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Slot slot = adminSlotService.updateSlot(
                admin.getId(),
                id,
                request.getMemo(),
                request.getFallbackRatio(),
                request.getTtlSeconds()
        );

        return new AdminSlotListResponse.Item(
                slot.getId(),
                slot.getName(),
                slot.getMemo(),
                slot.getFallbackRatio(),
                slot.getTtlSeconds(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
    }

    @DeleteMapping("/slot/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "슬롯 삭제",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteSlot(@PathVariable Long id,
                           @AuthenticationPrincipal UserPrincipal admin) {
        adminSlotService.deleteSlot(admin.getId(), id);
    }

    /* ───────────── 광고 배치(AdPlacement) 관리 ───────────── */

    @GetMapping("/placement")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "광고 배치 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminAdPlacementListResponse listPlacements() {
        List<AdPlacement> placements = adminAdPlacementService.listPlacements();

        List<AdminAdPlacementListResponse.Item> items = placements.stream()
                .map(p -> new AdminAdPlacementListResponse.Item(
                        p.getId(),
                        p.getAd().getId(),
                        p.getAd().getName(),
                        p.getAd().getObjectKey() != null ? imageDomainService.toUploadImageUrl(p.getAd().getObjectKey()) : null,
                        p.getSlot().getId(),
                        p.getSlot().getName(),
                        p.getStartDate(),
                        p.getEndDate(),
                        p.getWeight(),
                        p.getEnabled(),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();

        return new AdminAdPlacementListResponse(items);
    }

    @PostMapping("/placement")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 배치 생성",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminAdPlacementListResponse.Item createPlacement(
            @Valid @RequestBody AdminCreateAdPlacementRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        AdPlacement placement = adminAdPlacementService.createPlacement(
                admin.getId(),
                request.adId(),
                request.slotId(),
                request.startDate(),
                request.endDate(),
                request.weight(),
                request.enabled()
        );

        String adImageUrl = placement.getAd().getObjectKey() != null
                ? imageDomainService.toUploadImageUrl(placement.getAd().getObjectKey())
                : null;

        return new AdminAdPlacementListResponse.Item(
                placement.getId(),
                placement.getAd().getId(),
                placement.getAd().getName(),
                adImageUrl,
                placement.getSlot().getId(),
                placement.getSlot().getName(),
                placement.getStartDate(),
                placement.getEndDate(),
                placement.getWeight(),
                placement.getEnabled(),
                placement.getCreatedAt(),
                placement.getUpdatedAt()
        );
    }

    @PutMapping("/placement/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 배치 수정",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminAdPlacementListResponse.Item updatePlacement(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAdPlacementRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        AdPlacement placement = adminAdPlacementService.updatePlacement(
                admin.getId(),
                id,
                request.slotId(),
                request.startDate(),
                request.endDate(),
                request.weight(),
                request.enabled()
        );

        String adImageUrl = placement.getAd().getObjectKey() != null
                ? imageDomainService.toUploadImageUrl(placement.getAd().getObjectKey())
                : null;

        return new AdminAdPlacementListResponse.Item(
                placement.getId(),
                placement.getAd().getId(),
                placement.getAd().getName(),
                adImageUrl,
                placement.getSlot().getId(),
                placement.getSlot().getName(),
                placement.getStartDate(),
                placement.getEndDate(),
                placement.getWeight(),
                placement.getEnabled(),
                placement.getCreatedAt(),
                placement.getUpdatedAt()
        );
    }

    @DeleteMapping("/placement/{id}")
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "광고 배치 삭제",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deletePlacement(@PathVariable Long id,
                                @AuthenticationPrincipal UserPrincipal admin) {
        adminAdPlacementService.deletePlacement(admin.getId(), id);
    }
}
