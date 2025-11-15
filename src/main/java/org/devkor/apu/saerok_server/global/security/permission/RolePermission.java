package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        name = "role_permission",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"role", "permission_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 100)
    private UserRoleType role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    private RolePermission(UserRoleType role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public static RolePermission of(UserRoleType role, Permission permission) {
        return new RolePermission(role, permission);
    }
}
