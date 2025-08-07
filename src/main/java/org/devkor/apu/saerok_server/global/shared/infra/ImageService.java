package org.devkor.apu.saerok_server.global.shared.infra;

import java.util.List;

/**
 * 이미지 업로드/삭제/조회 기능을 제공하는 서비스
 */
public interface ImageService {
    /**
     * 업로드용 Presigned URL을 생성하고, S3에 실제 업로드할 키를 반환
     * @param objectKey 저장할 경로
     * @param contentType 파일의 Content-Type
     * @param expireMinutes URL 만료 시간
     * @return Presigned URL
     */
    String generateUploadUrl(String objectKey, String contentType, long expireMinutes);

    /**
     * 주어진 objectKey를 S3에서 삭제
     * @param objectKey 삭제할 경로
     */
    void delete(String objectKey);

    /**
     * 주어진 objectKey List를 S3에서 삭제
     * @param objectKeys 삭제할 경로들
     */
    void deleteAll(List<String> objectKeys);

    /**
     * 객체가 S3에 존재하는지 확인
     * @param objectKey 확인할 경로
     * @return true면 존재
     */
    boolean exists(String objectKey);
}