package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.ReportCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionReportCommandService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collection Report API", description = "컬렉션 신고 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections")
public class CollectionReportController {

    private final CollectionReportCommandService collectionReportCommandService;

    @PostMapping("/{collectionId}/report")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "컬렉션 신고",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "신고 접수 성공",
                            content = @Content(schema = @Schema(implementation = ReportCollectionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "컬렉션이 존재하지 않음 (또는 이미 신고한 컬렉션)", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ReportCollectionResponse reportCollection(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId
    ) {
        return collectionReportCommandService.reportCollection(userPrincipal.getId(), collectionId);
    }
}
