package org.devkor.apu.saerok_server.domain.ad.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateAdPlacementRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateAdRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminCreateSlotRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateAdPlacementRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateAdRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.request.AdminUpdateSlotRequest;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminAdListResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminAdPlacementListResponse;
import org.devkor.apu.saerok_server.domain.ad.api.dto.response.AdminSlotListResponse;
import org.devkor.apu.saerok_server.domain.ad.application.AdminAdPlacementService;
import org.devkor.apu.saerok_server.domain.ad.application.AdminAdService;
import org.devkor.apu.saerok_server.domain.ad.application.AdminSlotService;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Ad API", description = "배너 광고 관리용 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/ad")
public class AdminAdController {

    private final AdminAdService adminAdService;
    private final AdminSlotService adminSlotService;
    private final AdminAdPlacementService adminAdPlacementService;

    // --- Ad 관리 ---

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "광고 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdListResponse.class)))
            }
    )
    public AdminAdListResponse listAds() {
        List<Ad> ads = adminAdService.listAds();

        List<AdminAdListResponse.Item> items = ads.stream()
                .map(ad -> new AdminAdListResponse.Item(
                        ad.getId(),
                        ad.getName(),
                        ad.getMemo(),
                        ad.getObjectKey(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
            }
    )
    public AdminAdListResponse.Item createAd(
            @Valid @RequestBody AdminCreateAdRequest request
    ) {
        Ad ad = adminAdService.createAd(
                request.name(),
                request.memo(),
                request.objectKey(),
                request.contentType(),
                request.targetUrl()
        );

        return new AdminAdListResponse.Item(
                ad.getId(),
                ad.getName(),
                ad.getMemo(),
                ad.getObjectKey(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "광고를 찾을 수 없음", content = @Content)
            }
    )
    public AdminAdListResponse.Item updateAd(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAdRequest request
    ) {
        Ad ad = adminAdService.updateAd(
                id,
                request.name(),
                request.memo(),
                request.objectKey(),
                request.contentType(),
                request.targetUrl()
        );

        return new AdminAdListResponse.Item(
                ad.getId(),
                ad.getName(),
                ad.getMemo(),
                ad.getObjectKey(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "광고를 찾을 수 없음", content = @Content)
            }
    )
    public void deleteAd(
            @PathVariable Long id
    ) {
        adminAdService.deleteAd(id);
    }

    // --- Slot 관리 ---

    @GetMapping("/slot")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "슬롯 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminSlotListResponse.class)))
            }
    )
    public AdminSlotListResponse listSlots() {
        List<Slot> slots = adminSlotService.listSlots();

        List<AdminSlotListResponse.Item> items = slots.stream()
                .map(slot -> new AdminSlotListResponse.Item(
                        slot.getId(),
                        slot.getName(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = AdminSlotListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
            }
    )
    public AdminSlotListResponse.Item createSlot(
            @Valid @RequestBody AdminCreateSlotRequest request
    ) {
        Slot slot = adminSlotService.createSlot(
                request.name(),
                request.fallbackRatio(),
                request.ttlSeconds()
        );

        return new AdminSlotListResponse.Item(
                slot.getId(),
                slot.getName(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = AdminSlotListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "슬롯을 찾을 수 없음", content = @Content)
            }
    )
    public AdminSlotListResponse.Item updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateSlotRequest request
    ) {
        Slot slot = adminSlotService.updateSlot(
                id,
                request.fallbackRatio(),
                request.ttlSeconds()
        );

        return new AdminSlotListResponse.Item(
                slot.getId(),
                slot.getName(),
                slot.getFallbackRatio(),
                slot.getTtlSeconds(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
    }

    // --- AdPlacement 관리 ---

    @GetMapping("/placement")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "광고 배치 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdPlacementListResponse.class)))
            }
    )
    public AdminAdPlacementListResponse listPlacements() {
        List<AdPlacement> placements = adminAdPlacementService.listPlacements();

        List<AdminAdPlacementListResponse.Item> items = placements.stream()
                .map(p -> new AdminAdPlacementListResponse.Item(
                        p.getId(),
                        p.getAd().getId(),
                        p.getAd().getName(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdPlacementListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "광고/슬롯을 찾을 수 없음", content = @Content)
            }
    )
    public AdminAdPlacementListResponse.Item createPlacement(
            @Valid @RequestBody AdminCreateAdPlacementRequest request
    ) {
        AdPlacement placement = adminAdPlacementService.createPlacement(
                request.adId(),
                request.slotId(),
                request.startDate(),
                request.endDate(),
                request.weight(),
                request.enabled()
        );

        return new AdminAdPlacementListResponse.Item(
                placement.getId(),
                placement.getAd().getId(),
                placement.getAd().getName(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = AdminAdPlacementListResponse.Item.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "배치를 찾을 수 없음", content = @Content)
            }
    )
    public AdminAdPlacementListResponse.Item updatePlacement(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAdPlacementRequest request
    ) {
        AdPlacement placement = adminAdPlacementService.updatePlacement(
                id,
                request.slotId(),
                request.startDate(),
                request.endDate(),
                request.weight(),
                request.enabled()
        );

        return new AdminAdPlacementListResponse.Item(
                placement.getId(),
                placement.getAd().getId(),
                placement.getAd().getName(),
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "배치를 찾을 수 없음", content = @Content)
            }
    )
    public void deletePlacement(
            @PathVariable Long id
    ) {
        adminAdPlacementService.deletePlacement(id);
    }
}
