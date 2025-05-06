package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Collections API", description = "컬렉션 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections/")
public class CollectionController {

    @PostMapping
    @Operation(
            summary = "[미구현] 컬렉션 등록",
            description = "새 컬렉션(관찰 기록)을 생성합니다. (이미지 제외한 메타데이터 전송)"
    )
    public void createCollection(/* @RequestBody CreateCollectionRequest request */) {
        // TODO: 구현
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
