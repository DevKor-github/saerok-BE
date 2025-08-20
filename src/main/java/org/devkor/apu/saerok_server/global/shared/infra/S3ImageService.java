package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * S3의 업로드 이미지 버킷과 통신해 이미지 업로드/삭제/조회 기능을 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.upload-image-bucket}")
    private String bucket;

    @Override
    public String generateUploadUrl(String key, String contentType, long expireMinutes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(expireMinutes))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 이미지 삭제 실패: key=" + key, e);
        }
    }

    @Override
    public void deleteAll(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        final int BATCH_SIZE = 1000;
        List<String> hardFailed = new ArrayList<>();

        for (int i = 0; i < objectKeys.size(); i += BATCH_SIZE) {
            List<String> batch = objectKeys.subList(i, Math.min(i + BATCH_SIZE, objectKeys.size()));
            List<ObjectIdentifier> toDelete = batch.stream()
                    .map(k -> ObjectIdentifier.builder().key(k).build())
                    .toList();

            try {
                DeleteObjectsRequest req = DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build();

                DeleteObjectsResponse resp = s3Client.deleteObjects(req);

                // 200이어도 partial failure가 있을 수 있음
                if (resp != null && resp.hasErrors() && !resp.errors().isEmpty()) {
                    for (S3Error err : resp.errors()) {
                        String key = err.key();
                        log.warn("S3 다중 삭제 실패: key={}, code={}, message={}", key, err.code(), err.message());
                        // 개별 삭제로 재시도
                        try {
                            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                        } catch (S3Exception ex) {
                            hardFailed.add(key);
                            log.error("S3 개별 삭제 재시도 실패: key={}, 상태코드={}, 메시지={}", key, ex.statusCode(), ex.getMessage());
                        }
                    }
                }

            } catch (S3Exception e) {
                // 배치 호출 자체가 실패하면 각 키에 대해 개별 삭제 fallback
                log.warn("S3 다중 삭제 요청 자체 실패 (상태코드={}): 해당 배치({}개)에 대해 개별 삭제 시도", e.statusCode(), batch.size());
                for (String key : batch) {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                    } catch (S3Exception ex) {
                        hardFailed.add(key);
                        log.error("S3 개별 삭제 실패: key={}, 상태코드={}, 메시지={}", key, ex.statusCode(), ex.getMessage());
                    }
                }
            }
        }

        if (!hardFailed.isEmpty()) {
            throw new RuntimeException("일부 S3 이미지 삭제에 실패했습니다. 실패한 키 목록: " + hardFailed);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            // 404 Not Found 일 때는 false, 그 외는 예외 재던짐
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
