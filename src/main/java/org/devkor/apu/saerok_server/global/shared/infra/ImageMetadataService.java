package org.devkor.apu.saerok_server.global.shared.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.shared.util.ImageMetadataExtractor;
import org.devkor.apu.saerok_server.global.shared.util.dto.ExtractedImageMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3에 업로드된 이미지에서 메타데이터를 추출하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageMetadataService {

    private final S3Client s3Client;
    private final ImageMetadataExtractor imageMetadataExtractor;
    
    @Value("${aws.s3.upload-image-bucket}")
    private String bucket;

    /**
     * S3에 업로드된 이미지에서 메타데이터를 추출합니다.
     * 
     * @param objectKey S3 객체 키
     * @return 추출된 메타데이터
     */
    public ExtractedImageMetadata extractMetadataFromS3Image(String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            
            try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
                return imageMetadataExtractor.extractMetadata(s3Object);
            }
            
        } catch (S3Exception e) {
            log.error("S3에서 이미지를 가져오는 중 오류 발생: objectKey={}", objectKey, e);
            return new ExtractedImageMetadata(null, null, null);
        } catch (Exception e) {
            log.error("이미지 메타데이터 추출 중 예외 발생: objectKey={}", objectKey, e);
            return new ExtractedImageMetadata(null, null, null);
        }
    }
}
