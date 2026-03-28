package org.devkor.apu.saerok_server.domain.freeboard.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeBoardPostComment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_board_post_id", nullable = false)
    private FreeBoardPost post;

    @Setter
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FreeBoardCommentStatus status = FreeBoardCommentStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FreeBoardPostComment parent;

    public static FreeBoardPostComment of(User user, FreeBoardPost post, String content) {
        FreeBoardPostComment comment = new FreeBoardPostComment();
        comment.user = user;
        comment.post = post;
        comment.content = content;
        comment.status = FreeBoardCommentStatus.ACTIVE;
        return comment;
    }

    public static FreeBoardPostComment of(User user, FreeBoardPost post, String content, FreeBoardPostComment parent) {
        FreeBoardPostComment comment = of(user, post, content);
        comment.parent = parent;
        return comment;
    }

    public void softDelete() {
        this.status = FreeBoardCommentStatus.DELETED;
    }

    public void ban() {
        this.status = FreeBoardCommentStatus.BANNED;
    }

    public boolean isActive() {
        return this.status == FreeBoardCommentStatus.ACTIVE;
    }

    public boolean isReply() {
        return this.parent != null;
    }
}
