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

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false, length = 50)
    private NotificationSubject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private NotificationAction action;

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
    public Notification(User user,
                        String body,
                        NotificationSubject subject,
                        NotificationAction action,
                        Long relatedId,
                        String deepLink,
                        User sender,
                        Boolean isRead) {
        if (user == null) { throw new IllegalArgumentException("user는 null일 수 없습니다."); }
        if (body == null || body.trim().isEmpty()) { throw new IllegalArgumentException("body는 비어있을 수 없습니다."); }
        if (subject == null) { throw new IllegalArgumentException("subject는 null일 수 없습니다."); }
        if (action == null) { throw new IllegalArgumentException("action은 null일 수 없습니다."); }

        this.user = user;
        this.body = body;
        this.subject = subject;
        this.action = action;
        this.relatedId = relatedId;
        this.deepLink = deepLink;
        this.sender = sender;
        this.isRead = isRead != null ? isRead : false;
    }

    public void markAsRead() { this.isRead = true; }

    public boolean isUnread() { return !this.isRead; }
}
