package org.devkor.apu.saerok_server.global.core.config.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

/**
 * 애플리케이션 시작 시 기본 프로필 이미지들을 S3에 자동 업로드하는 컴포넌트
 * 
 * 동작 방식:
 * 1. 애플리케이션 시작 완료 후 자동 실행
 * 2. S3에 이미 존재하는 이미지는 스킵하여 중복 업로드 방지
 * 3. 리소스 파일에서 이미지를 읽어 S3에 업로드
 * 
 * 필요한 리소스 구조:
 * src/main/resources/static/images/profile/default/
 * ├── default-1.png
 * ├── default-2.png
 * ├── default-3.png
 * ├── default-4.png
 * ├── default-5.png
 * └── default-6.png
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultProfileImageInitializer {

    private static final String[] DEFAULT_PROFILE_IMAGES = {
            "default-1.png",
            "default-2.png", 
            "default-3.png",
            "default-4.png",
            "default-5.png",
            "default-6.png"
    };
    
    private static final String RESOURCE_PATH_PREFIX = "static/images/profile/default/";
    private static final String S3_KEY_PREFIX = "profile-images/default/";
    private static final String CONTENT_TYPE = "image/png";

    private final S3Client s3Client;

    @Value("${aws.s3.upload-image-bucket}")
    private String bucket;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultProfileImages() {
        log.info("=== 기본 프로필 이미지 초기화 시작 ===");
        
        int uploadedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        for (String fileName : DEFAULT_PROFILE_IMAGES) {
            String s3Key = S3_KEY_PREFIX + fileName;
            
            try {
                // 1. S3에 이미 존재하는지 확인
                if (isImageExistsInS3(s3Key)) {
                    log.debug("✓ 이미 존재하는 기본 프로필 이미지 스킵: {}", s3Key);
                    skippedCount++;
                    continue;
                }
                
                // 2. 리소스 파일 존재 여부 확인
                if (!isResourceExists(fileName)) {
                    log.error("✗ 리소스 파일을 찾을 수 없습니다: {}{}", RESOURCE_PATH_PREFIX, fileName);
                    errorCount++;
                    continue;
                }
                
                // 3. S3에 업로드
                uploadImageFromResources(fileName, s3Key);
                uploadedCount++;
                log.info("✓ 기본 프로필 이미지 업로드 완료: {}", s3Key);
                
            } catch (Exception e) {
                log.error("✗ 기본 프로필 이미지 업로드 실패: {} - {}", s3Key, e.getMessage());
                errorCount++;
            }
        }
        
        log.info("=== 기본 프로필 이미지 초기화 완료 ===");
        log.info("결과 - 업로드: {}개, 스킵: {}개, 오류: {}개", uploadedCount, skippedCount, errorCount);
        
        if (errorCount > 0) {
            log.warn("일부 기본 프로필 이미지 초기화에 실패했습니다. 위의 오류 로그를 확인해주세요.");
        }
    }

    /**
     * S3에 해당 키의 객체가 존재하는지 확인
     * HeadObject API를 사용하여 객체 메타데이터만 조회 (실제 데이터는 다운로드하지 않음)
     */
    private boolean isImageExistsInS3(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            // 객체가 존재하지 않는 경우
            return false;
        } catch (S3Exception e) {
            log.warn("S3 객체 존재 여부 확인 중 오류 발생: {} - {}", s3Key, e.getMessage());
            // 확실하지 않은 경우 존재하지 않는 것으로 처리하여 업로드 시도
            return false;
        }
    }

    /**
     * 클래스패스에 리소스 파일이 존재하는지 확인
     */
    private boolean isResourceExists(String fileName) {
        String resourcePath = RESOURCE_PATH_PREFIX + fileName;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return resource.exists();
    }

    /**
     * 리소스에서 이미지를 읽어 S3에 업로드
     */
    private void uploadImageFromResources(String fileName, String s3Key) throws IOException {
        String resourcePath = RESOURCE_PATH_PREFIX + fileName;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        
        try (InputStream inputStream = resource.getInputStream()) {
            long contentLength = resource.contentLength();
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(CONTENT_TYPE)
                    .contentLength(contentLength)
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
        } catch (IOException e) {
            throw new IOException("리소스 파일 읽기 실패: " + resourcePath, e);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + s3Key, e);
        }
    }
}
