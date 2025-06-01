package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"})
)
@Getter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRoleType role;

    public static UserRole createUserRole(User user, UserRoleType role) {
        UserRole userRole = new UserRole();
        userRole.user = user;
        userRole.role = role;
        return userRole;
    }
}
