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

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "deep_link", length = 500)
    private String deepLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Builder
    public Notification(User user, String title, String body, NotificationType type, 
                       Long relatedId, String deepLink, User sender, Boolean isRead) {
        if (user == null) {throw new IllegalArgumentException("user는 null일 수 없습니다.");}
        if (title == null || title.trim().isEmpty()) {throw new IllegalArgumentException("title은 비어있을 수 없습니다.");}
        if (body == null || body.trim().isEmpty()) {throw new IllegalArgumentException("body는 비어있을 수 없습니다.");}
        if (type == null) {throw new IllegalArgumentException("type은 null일 수 없습니다.");}

        this.user = user;
        this.title = title;
        this.body = body;
        this.type = type;
        this.relatedId = relatedId;
        this.deepLink = deepLink;
        this.sender = sender;
        this.isRead = isRead != null ? isRead : false;
    }

    // 알림을 읽음 처리합니다.
    public void markAsRead() {
        this.isRead = true;
    }

    // 알림이 읽지 않은 상태인지 확인합니다.
    public boolean isUnread() {
        return !this.isRead;
    }
}
