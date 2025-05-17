package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CollectionImagePresignRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionImageRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionImageResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.PresignResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommandService;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionImageCommandService;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.global.exception.ErrorResponse;
import org.devkor.apu.saerok_server.global.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collections API", description = "컬렉션 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections/")
public class CollectionController {

    private final CollectionWebMapper collectionWebMapper;
    private final CollectionCommandService collectionCommandService;
    private final CollectionImageCommandService collectionImageCommandService;

    @PostMapping
    @Operation(
            summary = "컬렉션 등록 (종추)",
            description = """
        새 컬렉션(관찰 기록)을 생성합니다. 이 단계에서는 **이미지를 제외한 메타데이터만 전송**합니다.

        ⚠️ 유효성 제약:
        - birdId와 tempBirdName은 반드시 하나만 있어야 함
        - note는 50자 이하

        📌 이 API를 먼저 호출하여 컬렉션을 생성한 후,
        응답으로 받은 `collectionId`를 기준으로 이미지를 업로드해야 합니다.
        
        ---
        ✅ 전체 등록 흐름 요약:

        1. `POST /collections` \s
           → 컬렉션 메타데이터 생성, 응답으로 `collectionId` 확보

        2. `POST /collections/{collectionId}/images/presign` \s
           → Presigned URL 발급, 응답으로 `url`과 `objectKey` 확보

        3. 클라이언트에서 해당 `url`로 컬렉션 이미지를 PUT 업로드 \s
           → 헤더에 `Content-Type` 포함

        4. `POST /collections/{collectionId}/images` \s
           → 업로드 완료 후 서버에 컬렉션 이미지 메타데이터 등록
        """,
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
    @ResponseStatus(HttpStatus.CREATED)
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
            summary = "컬렉션 이미지 Presigned URL 발급",
            description = """
        특정 컬렉션에 이미지 파일을 업로드할 수 있도록 **S3 Presigned URL을 발급**합니다.

        🪪 이 URL은 AWS S3로 직접 **`PUT` 요청**할 수 있는 임시 주소이며,
        클라이언트는 해당 주소로 직접 이미지 파일을 업로드해야 합니다.

        ---
        📤 **업로드 방식 (중요)**  
        Presigned URL이 발급되면, 클라이언트는 아래 조건에 맞춰 업로드 요청을 보내야 합니다:

        - HTTP 메서드: `PUT`  
        - 요청 URL: 이 API에서 발급받은 `url`  
        - 요청 본문: 업로드할 이미지 바이너리  
        - 요청 헤더:
            - `Content-Type`: Presigned URL 발급 시 서버에 전달했던 `contentType`과 정확히 동일하게 설정해야 합니다  
              (예: `image/jpeg`, `image/png` 등)

        ---
        🧾 요청 시 필요한 정보:
        - 이미지의 Content-Type (예: `image/jpeg`, `image/png`)

        📨 반환값:
        - `url`: presigned PUT 주소
        - `objectKey`: 이후 이미지 메타데이터 등록 API에 전달해야 하는 키

        💡 이 주소는 10분 이내에 만료되므로, 발급 즉시 업로드가 이루어져야 합니다.
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            컬렉션 이미지 업로드 주소 발급 요청 DTO.
                            """,
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CollectionImagePresignRequest.class),
                            mediaType = "application/json"
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "컬렉션 이미지 업로드 주소 발급 성공",
                            content = @Content(
                                    schema = @Schema(implementation = PresignResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "해당 컬렉션에 대한 권한 없음 (다른 사용자의 컬렉션 id로 요청한 경우)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 컬렉션 (유효하지 않은 컬렉션 id)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public PresignResponse generatePresignedUrls(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @RequestBody CollectionImagePresignRequest request
    ) {
        return collectionImageCommandService.generatePresignedUploadUrl(userPrincipal.getId(), collectionId, request.getContentType());
    }

    @PostMapping("/{collectionId}/images")
    @Operation(
            summary = "컬렉션 이미지 메타데이터 등록",
            description = """
        클라이언트가 S3에 이미지 업로드를 완료한 후, 
        해당 이미지의 **메타데이터(objectKey, contentType)** 를 서버에 등록합니다.

        ⚙️ 서버는 해당 정보를 기반으로 DB에 이미지 정보를 저장하고,
        이후 이 정보를 사용해 컬렉션 조회를 지원합니다.

        요청 시 필요한 정보:
        - objectKey: presigned URL 발급 API로부터 받은 objectKey 값 그대로
        - contentType: 클라이언트가 업로드한 파일의 Content-Type (예: image/jpeg, image/png)

        📌 반드시 `컬렉션 생성 API → presigned URL 발급 API → 해당 URL로 이미지 업로드 → 이미지 메타데이터 등록 API`의 순서를 따라야 합니다.
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "컬렉션 이미지 메타데이터 등록 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CreateCollectionImageResponse.class),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "해당 컬렉션에 대한 권한 없음 (다른 사용자의 컬렉션 id로 요청한 경우)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 컬렉션 (유효하지 않은 컬렉션 id)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public CreateCollectionImageResponse notifyImageUpload(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @RequestBody CreateCollectionImageRequest request
    ) {
        return collectionImageCommandService.saveImageMetadata(
                userPrincipal.getId(),
                collectionId,
                collectionWebMapper.toCreateCollectionImageCommand(request)
        );
    }
}
