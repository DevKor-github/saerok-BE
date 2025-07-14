package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Getter
@NoArgsConstructor
public class UserBirdCollectionCommentReport extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private UserBirdCollectionComment comment;

    @Column(name = "comment_content", nullable = false)
    private String commentContent;

    public static UserBirdCollectionCommentReport of(User reporter, User reportedUser, UserBirdCollectionComment comment) {
        UserBirdCollectionCommentReport commentReport = new UserBirdCollectionCommentReport();
        commentReport.reporter = reporter;
        commentReport.reportedUser = reportedUser;
        commentReport.comment = comment;
        commentReport.commentContent = comment.getContent();
        
        return commentReport;
    }
}
