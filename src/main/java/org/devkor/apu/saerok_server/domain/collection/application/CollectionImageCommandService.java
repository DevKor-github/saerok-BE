package org.devkor.apu.saerok_server.domain.collection.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionImageResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.PresignResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionImageCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageMetadataService;
import org.devkor.apu.saerok_server.global.shared.util.dto.ExtractedImageMetadata;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionImageCommandService {

    private final CollectionRepository collectionRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final ImageDomainService imageDomainService;
    private final ImageService imageService;
    private final ImageMetadataService imageMetadataService;

    public PresignResponse generatePresignedUploadUrl(Long userId, Long collectionId, String contentType) {

        UserBirdCollection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!userId.equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        String objectKey = "collection-images/" + collectionId + "/" + UUID.randomUUID();

        return new PresignResponse(
                imageService.generateUploadUrl(objectKey, contentType, 10),
                objectKey
        );
    }

    public CreateCollectionImageResponse saveImageMetadata(Long userId, Long collectionId, CreateCollectionImageCommand command) {

        UserBirdCollection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!userId.equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        UserBirdCollectionImage image = UserBirdCollectionImage.builder()
                .collection(collection)
                .objectKey(command.objectKey())
                .contentType(command.contentType())
                .build();

        // 이미지에서 메타데이터 추출
        ExtractedImageMetadata extractedMetadata = imageMetadataService.extractMetadataFromS3Image(command.objectKey());
        
        return new CreateCollectionImageResponse(
                collectionImageRepository.save(image),
                imageDomainService.toUploadImageUrl(image.getObjectKey()),
                extractedMetadata
        );
    }

    public void deleteCollectionImage(Long userId, Long collectionId, Long imageId) {

        UserBirdCollectionImage image = collectionImageRepository.findById(imageId).orElseThrow(() -> new NotFoundException("해당 id의 이미지가 존재하지 않아요"));
        if (!image.getCollection().getId().equals(collectionId)) {
            throw new NotFoundException("해당 컬렉션의 이미지가 아니예요");
        }
        if (!image.getCollection().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        String objectKey = image.getObjectKey();

        collectionImageRepository.remove(image);
        runAfterCommitOrNow(() -> imageService.delete(objectKey));
    }
}
