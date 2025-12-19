package org.devkor.apu.saerok_server.domain.admin.announcement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request.AdminAnnouncementImagePresignRequest;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request.AdminCreateAnnouncementRequest;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request.AdminUpdateAnnouncementRequest;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response.AdminAnnouncementDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response.AdminAnnouncementListResponse;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response.AnnouncementImagePresignResponse;
import org.devkor.apu.saerok_server.domain.admin.announcement.application.AdminAnnouncementService;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Announcement API", description = "공지사항 관리용 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/announcement")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    @PostMapping
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "공지사항 생성",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공", content = @Content(schema = @Schema(implementation = AdminAnnouncementDetailResponse.class)))
            }
    )
    public AdminAnnouncementDetailResponse createAnnouncement(
            @Valid @RequestBody AdminCreateAnnouncementRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Announcement announcement = adminAnnouncementService.createAnnouncement(
                admin.getId(),
                request.title(),
                request.content(),
                request.scheduledAt(),
                request.publishNow(),
                request.sendNotification(),
                request.pushTitle(),
                request.pushBody(),
                request.inAppBody(),
                request.images()
        );

        return toDetailResponse(announcement);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "공지사항 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = AdminAnnouncementDetailResponse.class)))
            }
    )
    public AdminAnnouncementDetailResponse updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAnnouncementRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        Announcement announcement = adminAnnouncementService.updateScheduledAnnouncement(
                admin.getId(),
                id,
                request.title(),
                request.content(),
                request.scheduledAt(),
                request.publishNow(),
                request.sendNotification(),
                request.pushTitle(),
                request.pushBody(),
                request.inAppBody(),
                request.images()
        );

        return toDetailResponse(announcement);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "공지사항 삭제",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        adminAnnouncementService.deleteAnnouncement(admin.getId(), id);
    }

    @GetMapping
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_READ')")
    @Operation(
            summary = "공지사항 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AdminAnnouncementListResponse.class)))
            }
    )
    public AdminAnnouncementListResponse listAnnouncements() {
        List<Announcement> announcements = adminAnnouncementService.listAnnouncements();

        List<AdminAnnouncementListResponse.Item> items = announcements.stream()
                .map(a -> new AdminAnnouncementListResponse.Item(
                        a.getId(),
                        a.getTitle(),
                        a.getStatus(),
                        a.getScheduledAt(),
                        a.getPublishedAt(),
                        a.getAdmin().getNickname()
                ))
                .toList();

        return new AdminAnnouncementListResponse(items);
    }

    @PostMapping("/image/presign")
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "공지사항 이미지 Presigned URL 발급",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "공지사항 본문 이미지 업로드용 Presigned URL을 발급합니다."
    )
    public AnnouncementImagePresignResponse generateImagePresignUrl(
            @Valid @RequestBody AdminAnnouncementImagePresignRequest request
    ) {
        return adminAnnouncementService.generateImagePresignUrl(request.contentType());
    }

    private AdminAnnouncementDetailResponse toDetailResponse(Announcement announcement) {
        List<AdminAnnouncementDetailResponse.Image> images = announcement.getImages().stream()
                .map(img -> new AdminAnnouncementDetailResponse.Image(img.getObjectKey(), img.getContentType()))
                .toList();

        return new AdminAnnouncementDetailResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getStatus(),
                announcement.getScheduledAt(),
                announcement.getPublishedAt(),
                announcement.getSendNotification(),
                announcement.getPushTitle(),
                announcement.getPushBody(),
                announcement.getInAppBody(),
                announcement.getAdmin().getNickname(),
                images
        );
    }
}
