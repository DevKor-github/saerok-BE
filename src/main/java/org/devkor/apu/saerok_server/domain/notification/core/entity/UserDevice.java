package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        name = "user_device",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id", "platform"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserDevice extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 256)
    private String deviceId;

    @Column(name = "token", length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 16)
    private DevicePlatform platform;

    public static UserDevice create(User user, String deviceId, String token, DevicePlatform platform) {
        UserDevice userDevice = new UserDevice();
        userDevice.user = user;
        userDevice.deviceId = deviceId;
        userDevice.platform = platform;
        userDevice.activateToken(token);
        return userDevice;
    }

    public void activateToken(String newToken) {
        if (newToken == null || newToken.isBlank()) {
            throw new IllegalArgumentException("FCM token은 비어 있을 수 없습니다");
        }
        this.token = newToken;
    }

    public void deactivateToken() {
        this.token = null;
    }
}
