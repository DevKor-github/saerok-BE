package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notification_setting_user_device_subject_action",
                columnNames = {"user_device_id", "subject", "action"}
        )
)
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
    @Column(name = "subject", nullable = false, length = 50)
    private NotificationSubject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 50)
    private NotificationAction action; // nullable: subject 그룹 토글 용

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Builder
    public NotificationSetting(UserDevice userDevice, NotificationSubject subject, NotificationAction action, Boolean enabled) {
        if (userDevice == null) throw new IllegalArgumentException("userDevice는 null일 수 없습니다.");
        if (subject == null) throw new IllegalArgumentException("subject는 null일 수 없습니다.");
        this.userDevice = userDevice;
        this.subject = subject;
        this.action = action;
        this.enabled = (enabled != null) ? enabled : true;
    }

    public void toggle() { this.enabled = !this.enabled; }

    public boolean enabled() { return Boolean.TRUE.equals(this.enabled); }
}
