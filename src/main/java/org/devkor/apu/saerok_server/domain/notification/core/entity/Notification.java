package org.devkor.apu.saerok_server.domain.notification.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

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

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "deep_link", length = 500)
    private String deepLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Builder
    public Notification(User user,
                        NotificationType type,
                        Long relatedId,
                        String deepLink,
                        User actor,
                        Boolean isRead) {
        if (user == null) { throw new IllegalArgumentException("user는 null일 수 없습니다."); }
        if (type == null) { throw new IllegalArgumentException("type은 null일 수 없습니다."); }

        this.user = user;
        this.type = type;
        this.relatedId = relatedId;
        this.deepLink = deepLink;
        this.actor = actor;
        this.isRead = isRead != null ? isRead : false;
    }

    public void markAsRead() { this.isRead = true; }

}
