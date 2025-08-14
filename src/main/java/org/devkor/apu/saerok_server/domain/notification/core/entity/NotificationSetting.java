package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(name = "notification_setting",
        uniqueConstraints = @UniqueConstraint(name = "uq_notification_setting_user_device_type",
                columnNames = {"user_device_id", "type"}))
@NoArgsConstructor
@Getter
public class NotificationSetting extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_device_id", nullable = false)
    private UserDevice userDevice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private NotificationType type;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    private NotificationSetting(UserDevice userDevice, NotificationType type, boolean enabled) {
        this.userDevice = userDevice;
        this.type = type;
        this.enabled = enabled;
    }

    public static NotificationSetting of(UserDevice device, NotificationType type, boolean enabled) {
        return new NotificationSetting(device, type, enabled);
    }

    public void toggle() { this.enabled = !this.enabled; }
}
