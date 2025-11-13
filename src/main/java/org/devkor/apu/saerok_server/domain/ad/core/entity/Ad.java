package org.devkor.apu.saerok_server.domain.ad.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(name = "ad")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Ad extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "memo")
    private String memo;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    private Ad(String name,
               String memo,
               String objectKey,
               String contentType,
               String targetUrl) {
        this.name = name;
        this.memo = memo;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.targetUrl = targetUrl;
    }

    public static Ad create(String name,
                            String memo,
                            String objectKey,
                            String contentType,
                            String targetUrl) {
        return new Ad(name, memo, objectKey, contentType, targetUrl);
    }

    public void update(String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {
        if (name != null) {
            this.name = name;
        }
        this.memo = memo;
        if (objectKey != null) {
            this.objectKey = objectKey;
        }
        if (contentType != null) {
            this.contentType = contentType;
        }
        if (targetUrl != null) {
            this.targetUrl = targetUrl;
        }
    }
}
