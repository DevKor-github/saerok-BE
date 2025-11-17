package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Table(
        name = "role",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_role_code", columnNames = "code"),
                @UniqueConstraint(name = "uq_role_display_name", columnNames = "display_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * 시스템 식별자 (예: USER, TEAM_MEMBER, ADMIN_EDITOR, MODERATOR 등)
     */
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    /**
     * 관리자 UI 등에 노출될 사람이 읽기 좋은 이름
     */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * Role의 의미/용도를 설명하는 텍스트
     */
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    /**
     * 시스템에 내장된 Role 인지 여부
     * - true: 코드에서 관리하는 기본 Role (삭제/수정에 제한을 둘 수 있음)
     * - false: 운영자가 UI를 통해 추가한 커스텀 Role
     */
    @Column(name = "is_builtin", nullable = false)
    private boolean builtin;

    private Role(String code, String displayName, String description, boolean builtin) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.builtin = builtin;
    }

    public static Role builtin(String code, String displayName, String description) {
        return new Role(code, displayName, description, true);
    }

    public static Role custom(String code, String displayName, String description) {
        return new Role(code, displayName, description, false);
    }
}
