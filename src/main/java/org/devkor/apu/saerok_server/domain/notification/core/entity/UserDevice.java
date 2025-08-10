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
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"})
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

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    public static UserDevice create(User user, String deviceId, String token) {
        UserDevice userDevice = new UserDevice();
        userDevice.user = user;
        userDevice.deviceId = deviceId;
        userDevice.token = token;
        return userDevice;
    }

    public void updateToken(String newToken) { this.token = newToken; }
}
