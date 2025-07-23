package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Getter
@NoArgsConstructor
public class UserBirdCollectionReport extends CreatedAtOnly {

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
    @JoinColumn(name = "collection_id", nullable = false)
    private UserBirdCollection collection;

    public static UserBirdCollectionReport of(User reporter, User reportedUser, UserBirdCollection collection) {
        UserBirdCollectionReport collectionReport = new UserBirdCollectionReport();
        collectionReport.reporter = reporter;
        collectionReport.reportedUser = reportedUser;
        collectionReport.collection = collection;
        
        return collectionReport;
    }
}
