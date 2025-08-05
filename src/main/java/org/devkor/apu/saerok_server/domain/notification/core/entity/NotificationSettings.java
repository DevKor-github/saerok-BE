package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uq_notification_settings_user_device", columnNames = {"user_id", "device_id"})
)
@NoArgsConstructor
@Getter
public class NotificationSettings extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 256)
    private String deviceId;

    @Column(name = "like_enabled", nullable = false)
    private Boolean likeEnabled;

    @Column(name = "comment_enabled", nullable = false)
    private Boolean commentEnabled;

    @Column(name = "bird_id_suggestion_enabled", nullable = false)
    private Boolean birdIdSuggestionEnabled;

    @Column(name = "system_enabled", nullable = false)
    private Boolean systemEnabled;

    @Builder
    public NotificationSettings(User user, String deviceId, Boolean likeEnabled, Boolean commentEnabled, Boolean birdIdSuggestionEnabled, Boolean systemEnabled) {
        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (deviceId == null || deviceId.trim().isEmpty()) throw new IllegalArgumentException("deviceId는 비어있을 수 없습니다.");
        
        this.user = user;
        this.deviceId = deviceId;
        this.likeEnabled = likeEnabled != null ? likeEnabled : true;
        this.commentEnabled = commentEnabled != null ? commentEnabled : true;
        this.birdIdSuggestionEnabled = birdIdSuggestionEnabled != null ? birdIdSuggestionEnabled : true;
        this.systemEnabled = systemEnabled != null ? systemEnabled : true;
    }

    /**
     * 기본 설정으로 알림 설정을 생성합니다 (모든 알림 활성화)
     */
    public static NotificationSettings createDefault(User user, String deviceId) {
        return NotificationSettings.builder()
                .user(user)
                .deviceId(deviceId)
                .likeEnabled(true)
                .commentEnabled(true)
                .birdIdSuggestionEnabled(true)
                .systemEnabled(true)
                .build();
    }

    // 특정 알림 유형의 활성화 상태를 토글합니다
    public void toggleNotification(NotificationType type) {
        switch (type) {
            case LIKE -> this.likeEnabled = !this.likeEnabled;
            case COMMENT -> this.commentEnabled = !this.commentEnabled;
            case BIRD_ID_SUGGESTION -> this.birdIdSuggestionEnabled = !this.birdIdSuggestionEnabled;
            case SYSTEM -> this.systemEnabled = !this.systemEnabled;
        }
    }

    // 특정 알림 유형의 활성화 상태를 조회합니다
    public boolean isNotificationEnabled(NotificationType type) {
        return switch (type) {
            case LIKE -> this.likeEnabled;
            case COMMENT -> this.commentEnabled;
            case BIRD_ID_SUGGESTION -> this.birdIdSuggestionEnabled;
            case SYSTEM -> this.systemEnabled;
        };
    }
}
