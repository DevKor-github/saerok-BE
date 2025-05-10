package org.devkor.apu.saerok_server.domain.dex.bookmark.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bookmark.application.BookmarkService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/${api_prefix}/birds/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    @Operation(
            summary = "[미구현] 내 북마크 목록 조회",
            description = "사용자가 북마크한 조류 목록을 조회합니다. " +
                    "북마크 엔티티의 정보만 포함합니다."
    )
    public void getBookmarks() {
        // 미구현
    }

    @GetMapping("/items")
    @Operation(
            summary = "[미구현] 북마크한 조류 상세 정보 조회",
            description = "사용자가 북마크한 조류들의 상세 정보를 조회합니다."
    )
    public void getBookmarkedBirdDetails() {
        // 미구현
    }

    @PostMapping("/{birdId}/toggle")
    @Operation(
            summary = "[미구현] 조류 북마크 토글",
            description = "특정 조류에 대한 북마크를 추가하거나 제거합니다."
    )
    public void postBookmarkToggle() {
        // 미구현
    }

    @GetMapping("/{birdId}/status")
    @Operation(
            summary = "[미구현] 조류 북마크 상태 확인",
            description = "특정 조류에 대한 북마크 상태를 확인합니다."
    )
    public void getBookmarkStatus() {
        // 미구현
    }
}