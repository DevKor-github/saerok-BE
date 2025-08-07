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
import org.devkor.apu.saerok_server.global.shared.exception.S3DeleteException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionImageCommandService {

    private static final Logger log = LoggerFactory.getLogger(CollectionImageCommandService.class);

    private final S3Presigner s3Presigner;
    private final CollectionRepository collectionRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final ImageDomainService imageDomainService;
    private final S3Client s3Client;

    @Value("${aws.s3.upload-image-bucket}")
    private String bucket;

    public PresignResponse generatePresignedUploadUrl(Long userId, Long collectionId, String contentType) {

        UserBirdCollection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!userId.equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        String objectKey = "collection-images/" + collectionId + "/" + UUID.randomUUID();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignResponse(
                presignedRequest.url().toString(),
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

        return new CreateCollectionImageResponse(
                collectionImageRepository.save(image),
                imageDomainService.toUploadImageUrl(image.getObjectKey())
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

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            log.error("S3 이미지 삭제 실패: objectKey={}, bucket={}, error={}", objectKey, bucket, e.getMessage());
            throw new S3DeleteException("S3 이미지 삭제에 실패했습니다.");
        }

        collectionImageRepository.remove(image);
    }
}
