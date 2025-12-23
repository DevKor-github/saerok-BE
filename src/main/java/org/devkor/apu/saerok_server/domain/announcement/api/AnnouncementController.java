package org.devkor.apu.saerok_server.domain.announcement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.announcement.api.dto.response.AnnouncementDetailResponse;
import org.devkor.apu.saerok_server.domain.announcement.api.dto.response.AnnouncementListResponse;
import org.devkor.apu.saerok_server.domain.announcement.application.AnnouncementQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Announcement API", description = "서비스 공지사항 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/announcements")
public class AnnouncementController {

    private final AnnouncementQueryService announcementQueryService;

    @GetMapping
    @Operation(
            summary = "게시된 공지사항 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AnnouncementListResponse.class)))
            }
    )
    public AnnouncementListResponse listAnnouncements() {
        return announcementQueryService.listPublished();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "게시된 공지사항 상세 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AnnouncementDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공지사항", content = @Content)
            }
    )
    public AnnouncementDetailResponse getAnnouncement(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id
    ) {
        return announcementQueryService.getPublishedAnnouncement(id);
    }
}
