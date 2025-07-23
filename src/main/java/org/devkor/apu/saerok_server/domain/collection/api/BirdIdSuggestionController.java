package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.SuggestOrAgreeRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.application.BirdIdSuggestionCommandService;
import org.devkor.apu.saerok_server.domain.collection.application.BirdIdSuggestionQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bird‑ID Suggestion API", description = "조류 ID 동정 의견 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections")
public class BirdIdSuggestionController {

    private final BirdIdSuggestionCommandService commandService;
    private final BirdIdSuggestionQueryService   queryService;

    @GetMapping("/pending-bird-id")
    @PermitAll
    @Operation(
            summary  = "동정 의견을 기다리는 컬렉션 목록 조회",
            description = "bird_id 미확정 PUBLIC 컬렉션 목록을 조회",
            responses = @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content      = @Content(schema = @Schema(implementation = GetPendingCollectionsResponse.class))
            )
    )
    public GetPendingCollectionsResponse listPendingCollections() {
        return queryService.getPendingCollections();
    }

    @GetMapping("/{collectionId}/bird-id-suggestions")
    @PermitAll
    @Operation(
            summary  = "컬렉션 동정 의견 목록 조회 (인증: optional)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content      = @Content(schema = @Schema(implementation = GetBirdIdSuggestionsResponse.class))
            )
    )
    public GetBirdIdSuggestionsResponse listSuggestions(
            @PathVariable Long collectionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal == null ? null : userPrincipal.getId();
        return queryService.getSuggestions(userId, collectionId);
    }

    @PostMapping("/{collectionId}/bird-id-suggestions")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary  = "동정 의견 제안/동의",
            description = "400 error: 나 자신의 컬렉션에 동정 의견 제안/동의하는 경우, bird_id 확정된 컬렉션에 동정 의견 제안/동의하는 경우",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content  = @Content(schema = @Schema(implementation = SuggestOrAgreeRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "제안/동의 성공",
                            content = @Content(schema = @Schema(implementation = SuggestOrAgreeResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "컬렉션/조류 없음", content = @Content)
            }
    )
    public SuggestOrAgreeResponse suggestOrAgree(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @RequestBody SuggestOrAgreeRequest request
    ) {
        return commandService.suggestOrAgree(userPrincipal.getId(), collectionId, request.birdId());
    }

    @DeleteMapping("/{collectionId}/bird-id-suggestions/{birdId}/agree")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary  = "동정 의견 동의 취소",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "동의 취소 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "동의 기록 없음", content = @Content)
            }
    )
    public void cancelAgree(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @PathVariable Long birdId
    ) {
        commandService.cancelAgree(userPrincipal.getId(), collectionId, birdId);
    }

    @PostMapping("/{collectionId}/bird-id-suggestions/{birdId}/adopt")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary  = "동정 의견 채택",
            description = "해당 컬렉션 소유자만 동정 의견 채택 가능",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "채택 성공",
                            content = @Content(schema = @Schema(implementation = AdoptSuggestionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "이미 확정된 컬렉션", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "컬렉션/조류 없음", content = @Content)
            }
    )
    public AdoptSuggestionResponse adopt(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @PathVariable Long birdId
    ) {
        return commandService.adopt(userPrincipal.getId(), collectionId, birdId);
    }

    @DeleteMapping("/{collectionId}/bird-id-suggestions/all")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary  = "컬렉션의 모든 동정 의견 삭제",
            description = "해당 컬렉션 소유자만 모든 동정 의견 삭제 가능",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "컬렉션 없음", content = @Content)
            }
    )
    public void deleteAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId
    ) {
        commandService.deleteAll(userPrincipal.getId(), collectionId);
    }
}
