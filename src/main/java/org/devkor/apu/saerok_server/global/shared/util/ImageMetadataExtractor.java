package org.devkor.apu.saerok_server.global.shared.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.shared.util.dto.ExtractedImageMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 이미지 파일에서 EXIF 메타데이터를 추출하는 유틸리티 클래스
 */
@Slf4j
@Component
public class ImageMetadataExtractor {

    /**
     * 이미지 InputStream에서 EXIF 메타데이터를 추출합니다.
     * 
     * @param imageStream 이미지 파일의 InputStream
     * @return 추출된 메타데이터 (날짜, GPS 좌표)
     */
    public ExtractedImageMetadata extractMetadata(InputStream imageStream) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageStream);

            LocalDate extractedDate = extractDate(metadata);
            Double latitude = null;
            Double longitude = null;
            
            // GPS 정보 추출
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && !gpsDirectory.hasErrors()) {
                try {
                    if (gpsDirectory.getGeoLocation() != null) {
                        latitude = gpsDirectory.getGeoLocation().getLatitude();
                        longitude = gpsDirectory.getGeoLocation().getLongitude();
                    }
                } catch (Exception e) {
                    log.warn("GPS 좌표 추출 중 오류 발생", e);
                }
            }
            return new ExtractedImageMetadata(extractedDate, latitude, longitude);
            
        } catch (ImageProcessingException | IOException e) {
            log.error("이미지 메타데이터 추출 실패", e);
            return new ExtractedImageMetadata(null, null, null);
        }
    }
    
    private LocalDate extractDate(Metadata metadata) {
        // DateTimeOriginal을 우선적으로 사용 (사진 촬영 시간)
        ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        
        if (exifDirectory != null) {
            try {
                Date dateTimeOriginal = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (dateTimeOriginal != null) {
                    LocalDateTime localDateTime = dateTimeOriginal.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();

                    return localDateTime.toLocalDate();
                }
            } catch (Exception e) {
                log.warn("날짜 추출 중 오류 발생", e);
            }
        }
        
        return null;
    }
}
