package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CollectionCommandService;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.exception.ErrorResponse;
import org.devkor.apu.saerok_server.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collections API", description = "컬렉션 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections/")
public class CollectionController {

    private final UserRepository userRepository;
    private final CollectionWebMapper collectionWebMapper;
    private final CollectionCommandService collectionCommandService;

    @PostMapping
    @Operation(
            summary = "컬렉션 등록 (종추)",
            description = "새 컬렉션(관찰 기록)을 생성합니다. (이미지 제외한 메타데이터 전송)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            컬렉션 생성 요청 DTO.
                            
                            - `birdId`와 `tempBirdName` 둘 중 하나는 null이고, 다른 하나는 null이 아니어야 합니다. 위반 시 Bad Request
                            <br> ex) 도감에 등록된 새라면 `birdId = 42, tempBirdName = null`. 그렇지 않으면 `birdId = null, tempBirdName = "이상한 새"`
                            """,
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCollectionRequest.class),
                            mediaType = "application/json"
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "컬렉션 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CreateCollectionResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public CreateCollectionResponse createCollection(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateCollectionRequest request
    ) {
        Long userId = userPrincipal.getId();
        Long collectionId = collectionCommandService.createCollection(collectionWebMapper.toCreateCollectionCommand(request, userId));
        return collectionWebMapper.toCreateCollectionResponse(collectionId);
    }

    @PostMapping("/{collectionId}/images/presign")
    @Operation(
            summary = "[미구현] 컬렉션 이미지 Presign 발급",
            description = "클라이언트가 S3로 이미지를 직접 업로드할 수 있도록 Presigned URL을 발급합니다."
    )
    public void generatePresignedUrls(
            @PathVariable Long collectionId
            /* , @RequestBody CollectionImagePresignRequest request */
    ) {
        // TODO: 구현
    }

    @PostMapping("/{collectionId}/images")
    @Operation(
            summary = "[미구현] 컬렉션 이미지 업로드 완료 통보",
            description = "클라이언트가 S3 이미지 업로드를 끝낸 뒤, 이미지 메타데이터(Object Key, orderIndex 등)를 서버에 등록합니다."
    )
    public void notifyImageUpload(
            @PathVariable Long collectionId
            /* , @RequestBody CollectionImageUploadCompleteRequest images */
    ) {
        // TODO: 구현
    }
}
