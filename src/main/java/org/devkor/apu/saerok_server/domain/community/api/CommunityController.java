package org.devkor.apu.saerok_server.domain.community.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityCollectionsResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityMainResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunitySearchResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunitySearchUsersResponse;
import org.devkor.apu.saerok_server.domain.community.application.CommunityQueryService;
import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ErrorResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community API", description = "커뮤니티 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/community")
public class CommunityController {

    private final CommunityQueryService communityQueryService;

    // 1) 메인 화면
    @GetMapping("/main")
    @PermitAll
    @Operation(
            summary = "커뮤니티 메인 화면 조회",
            description = """
            커뮤니티 메인 화면에 표시할 데이터를 조회합니다.
            로그인 여부와 관계없이 비공개 컬렉션은 보이지 않습니다.
            - 최근에 올라온 새록
            - 요즘 인기 있는 새록
            - 동정 요청 새록
            위 세 탭의 아이템을 각각 3개씩 조회합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunityMainResponse.class)))
            }
    )
    public GetCommunityMainResponse getCommunityMain(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.getCommunityMain(userId);
    }

    // 2) 최근 올라온 새록 (사실상 전체 보기)
    @GetMapping("/recent")
    @PermitAll
    @Operation(
            summary = "최근 올라온 새록 조회",
            description = """
            '최근 올라온 새록' 탭 목록을 조회합니다.
            
            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunityCollectionsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public GetCommunityCollectionsResponse getRecentCollections(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        CommunityQueryCommand command = new CommunityQueryCommand(page, size, null);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.getRecentCollections(userId, command);
    }

    // 3) 요즘 인기 있는 새록
    @GetMapping("/popular")
    @PermitAll
    @Operation(
            summary = "요즘 인기 있는 새록 조회",
            description = """
            '요즘 인기 있는 새록' 탭 목록을 조회합니다.
            좋아요 10개 이상인 새록에 대해 최신순으로 정렬합니다.
            
            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunityCollectionsResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public GetCommunityCollectionsResponse getPopularCollections(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        CommunityQueryCommand command = new CommunityQueryCommand(page, size, null);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.getPopularCollections(userId, command);
    }

    // 4) 이 새 이름이 뭔가요?
    @GetMapping("/pending-bird-id")
    @PermitAll
    @Operation(
            summary = "동정 요청 새록 조회",
            description = """
            '이 새 이름이 뭔가요?' 탭 목록을 조회합니다.
            
            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunityCollectionsResponse.class)))
            }
    )
    public GetCommunityCollectionsResponse getPendingCollections(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        CommunityQueryCommand command = new CommunityQueryCommand(page, size, null);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.getPendingBirdIdCollections(userId, command);
    }

    // 5) 검색 - 전체
    @GetMapping("/search")
    @PermitAll
    @Operation(
            summary = "커뮤니티 검색 - 전체",
            description = """
            검색어를 기준으로 새록/사용자 결과를 각각 최대 3개까지 조회합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunitySearchResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public GetCommunitySearchResponse searchAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "검색어", example = "까치") @RequestParam String q
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.searchAll(q, userId);
    }

    // 6) 검색 - 새록
    @GetMapping("/search/collections")
    @PermitAll
    @Operation(
            summary = "커뮤니티 검색 - 새록",
            description = """
            검색어에 해당하는 새 이름을 가진 컬렉션들을 조회합니다.
            
            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunityCollectionsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public GetCommunityCollectionsResponse searchCollections(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "검색어", example = "까치") @RequestParam String q,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        CommunityQueryCommand command = new CommunityQueryCommand(page, size, q);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return communityQueryService.searchCollections(userId, command);
    }

    // 7) 검색 - 사용자
    @GetMapping("/search/users")
    @PermitAll
    @Operation(
            summary = "커뮤니티 검색 - 사용자",
            description = """
            검색어에 해당하는 닉네임을 가진 사용자들을 조회합니다.
            
            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = GetCommunitySearchUsersResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public GetCommunitySearchUsersResponse searchUsers(
            @Parameter(description = "검색어", example = "새록마스터") @RequestParam String q,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        CommunityQueryCommand command = new CommunityQueryCommand(page, size, q);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        
        return communityQueryService.searchUsers(command);
    }
}
