package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@NoArgsConstructor
public class UserProfileImage extends CreatedAtOnly {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    public static UserProfileImage of(User user, String objectKey, String contentType) {
        UserProfileImage img = new UserProfileImage();
        img.user = user;
        img.objectKey = objectKey;
        img.contentType = contentType;

        return img;
    }
}
