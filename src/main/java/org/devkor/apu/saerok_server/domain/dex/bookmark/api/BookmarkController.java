package org.devkor.apu.saerok_server.domain.dex.bookmark.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.application.BookmarkCommandService;
import org.devkor.apu.saerok_server.domain.dex.bookmark.application.BookmarkQueryService;
import org.devkor.apu.saerok_server.global.shared.exception.ErrorResponse;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookmarks API", description = "북마크 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/birds/bookmarks")
public class BookmarkController {

    private final BookmarkCommandService bookmarkCommandService;
    private final BookmarkQueryService bookmarkQueryService;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "내 북마크 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "사용자가 북마크한 조류 목록을 조회합니다. 북마크 엔티티의 정보만 포함합니다.",
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = BookmarkResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
            }
    )
    public ResponseEntity<BookmarkResponse> getBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();

        System.out.println(userId);
        
        BookmarkResponse response = bookmarkQueryService.getBookmarksResponse(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "북마크한 조류 상세 정보 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "사용자가 북마크한 조류들의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = BookmarkedBirdDetailResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
            }
    )
    public ResponseEntity<List<BookmarkedBirdDetailResponse>> getBookmarkedBirdDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        
        List<BookmarkedBirdDetailResponse> birdDetails = bookmarkQueryService.getBookmarkedBirdDetailsResponse(userId);
        return ResponseEntity.ok(birdDetails);
    }

    @PostMapping("/{birdId}/toggle")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "조류 북마크 토글",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "특정 조류에 대한 북마크를 추가하거나 제거합니다.",
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "토글 성공",
                        content = @Content(schema = @Schema(implementation = BookmarkStatusResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "조류를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<BookmarkStatusResponse> toggleBookmark(
            @PathVariable Long birdId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        
        BookmarkStatusResponse response = bookmarkCommandService.toggleBookmarkResponse(userId, birdId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{birdId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "조류 북마크 상태 확인",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "특정 조류에 대한 북마크 상태를 확인합니다.",
            responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = BookmarkStatusResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
            }
    )
    public ResponseEntity<BookmarkStatusResponse> getBookmarkStatus(
            @PathVariable Long birdId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        
        BookmarkStatusResponse statusResponse = bookmarkQueryService.getBookmarkStatusResponse(userId, birdId);
        return ResponseEntity.ok(statusResponse);
    }
}