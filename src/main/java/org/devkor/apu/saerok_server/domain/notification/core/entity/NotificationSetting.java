package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
            name = "uq_notification_setting_user_device_type", columnNames = {"user_id", "device_id", "type"}
        )
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Builder
    public NotificationSettings(User user, String deviceId, NotificationType type, Boolean enabled) {
        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (deviceId == null || deviceId.trim().isEmpty()) throw new IllegalArgumentException("deviceId는 비어있을 수 없습니다.");
        if (type == null) throw new IllegalArgumentException("type은 null일 수 없습니다.");
        
        this.user = user;
        this.deviceId = deviceId;
        this.type = type;
        this.enabled = enabled != null ? enabled : true;
    }

    // 기본 설정으로 모든 알림 유형을 활성화하여 생성합니다
    public static List<NotificationSettings> createDefaultSettings(User user, String deviceId) {
        return Arrays.stream(NotificationType.values())
                .map(type -> NotificationSettings.builder()
                        .user(user)
                        .deviceId(deviceId)
                        .type(type)
                        .enabled(true)
                        .build())
                .toList();
    }

    // 알림 활성화 상태를 토글합니다
    public void toggleNotificationSetting() {this.enabled = !this.enabled;}

    // 알림 활성화 상태를 조회합니다
    public boolean isNotificationEnabled() {return this.enabled;}
}
