package org.devkor.apu.saerok_server.domain.dex.bookmark.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkIdResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.application.BookmarkService;
import org.devkor.apu.saerok_server.global.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/${api_prefix}/birds/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    @Operation(
            summary = "[미구현] 내 북마크 목록 조회",
            description = "사용자가 북마크한 조류 목록을 조회합니다."
    )
    public void getBookmarks() {
        // 미구현
    }

    @GetMapping("/{birdId}")
    @Operation(
            summary = "[미구현] 특정 조류 북마크 여부 조회",
            description = "사용자가 해당 조류를 북마크했는지 여부를 조회합니다."
    )
    public void isBookmarked() {
        // 미구현
    }

    @PostMapping("/{birdId}")
    @Operation(
            summary = "[미구현] 북마크 추가",
            description = "사용자가 특정 조류를 북마크합니다."
    )
    public void addBookmark() {
        // 미구현
    }

    @DeleteMapping("/{birdId}")
    @Operation(
            summary = "북마크 해제",
            description = "사용자가 특정 조류의 북마크를 해제합니다."
    )
    public void removeBookmark() {
        //미구현
    }
}
