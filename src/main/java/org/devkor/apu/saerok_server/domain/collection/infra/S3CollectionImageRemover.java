package org.devkor.apu.saerok_server.domain.collection.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.List;

@Service
@RequiredArgsConstructor
public class S3CollectionImageRemover {

    private final CollectionImageRepository collectionImageRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public void removeFromS3(Long collectionId) {
        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collectionId);

        if (!objectKeys.isEmpty()) {
            List<ObjectIdentifier> objectsToDelete = objectKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();
            s3Client.deleteObjects(deleteRequest);

            // TODO: 추후 운영/고도화 단계에서 S3 이미지 삭제 실패 감지 및 후처리
        }
    }
}
