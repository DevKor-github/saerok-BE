package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collections Like API", description = "컬렉션 좋아요 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collections")
public class CollectionLikeController {

    @PostMapping("/{collectionId}/like")
    @Operation(summary = "컬렉션 좋아요 토글", description = "컬렉션에 좋아요를 추가하거나 제거합니다.")
    public void toggleLike(
            @PathVariable Long collectionId,
            @RequestParam Long userId
    ) {return;}

    @GetMapping("/{collectionId}/like/status")
    @Operation(summary = "컬렉션 좋아요 상태 조회", description = "사용자의 특정 컬렉션 좋아요 상태를 조회합니다.")
    public void getLikeStatus(
            @PathVariable Long collectionId,
            @RequestParam Long userId
    ) {return;}

    @GetMapping("/me/liked")
    @Operation(summary = "내가 좋아요한 컬렉션 목록", description = "사용자가 좋아요한 컬렉션 목록을 조회합니다.")
    public void getLikedCollections(
            @RequestParam Long userId
    ) {return;}

    @GetMapping("/{collectionId}/likers")
    @Operation(summary = "좋아요 누른 유저 목록", description = "특정 컬렉션을 좋아요한 유저 목록을 조회합니다.")
    public List<Long> getLikers(
            @PathVariable Long collectionId
    ){return null;}
}
