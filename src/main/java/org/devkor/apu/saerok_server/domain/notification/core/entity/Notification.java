package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notification")
@NoArgsConstructor
@Getter
public class Notification extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private NotificationType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Builder
    public Notification(User user,
                        NotificationType type,
                        User actor,
                        Boolean isRead,
                        Map<String, Object> payload) {
        if (user == null) { throw new IllegalArgumentException("user는 null일 수 없습니다."); }
        if (type == null) { throw new IllegalArgumentException("type은 null일 수 없습니다."); }

        this.user = user;
        this.type = type;
        this.actor = actor;
        this.isRead = isRead != null ? isRead : false;
        if (payload != null) {
            this.payload = new HashMap<>(payload);
        } else {
            this.payload = new HashMap<>();
        }
    }

    public void markAsRead() { this.isRead = true; }

}
