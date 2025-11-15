package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        name = "permission",
        uniqueConstraints = @UniqueConstraint(columnNames = "key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "key", nullable = false, length = 100)
    private PermissionKey key;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    private Permission(PermissionKey key, String description) {
        this.key = key;
        this.description = description;
    }

    public static Permission of(PermissionKey key, String description) {
        return new Permission(key, description);
    }
}
