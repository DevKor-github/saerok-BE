package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@NoArgsConstructor
public class UserProfileImage extends Auditable {

    private static final String DEFAULT_PROFILE_IMAGE_KEY = "profile-images/default/default.png";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Builder
    public UserProfileImage(User user, String objectKey, String contentType) {
        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (objectKey == null || objectKey.isEmpty()) throw new IllegalArgumentException("objectKey는 null이거나 빈 문자열일 수 없습니다.");
        if (contentType == null || contentType.isEmpty()) throw new IllegalArgumentException("contentType은 null이거나 빈 문자열일 수 없습니다.");

        this.user = user;
        this.objectKey = objectKey;
        this.contentType = contentType;
    }

    public String updateToDefault(String defaultContentType) {
        String oldObjectKey = this.objectKey;
        this.objectKey = DEFAULT_PROFILE_IMAGE_KEY;
        this.contentType = defaultContentType;
        return oldObjectKey;
    }

    public String updateToCustom(String newObjectKey, String newContentType) {
        if (newObjectKey == null || newObjectKey.isEmpty()) {
            throw new IllegalArgumentException("objectKey는 null이거나 빈 문자열일 수 없습니다.");
        }
        if (newContentType == null || newContentType.isEmpty()) {
            throw new IllegalArgumentException("contentType은 null이거나 빈 문자열일 수 없습니다.");
        }
        
        String oldObjectKey = this.objectKey;
        this.objectKey = newObjectKey;
        this.contentType = newContentType;
        return oldObjectKey;
    }

    /**
     * 현재 기본 이미지인지 확인
     */
    public boolean isDefaultImage() {
        return DEFAULT_PROFILE_IMAGE_KEY.equals(this.objectKey);
    }
}
