package org.devkor.apu.saerok_server.domain.admin.audit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "admin_audit_log")
@Getter
@NoArgsConstructor
public class AdminAuditLog extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private AdminAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 64)
    private AdminAuditTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "report_id")
    private Long reportId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    private AdminAuditLog(User admin,
                          AdminAuditAction action,
                          AdminAuditTargetType targetType,
                          Long targetId,
                          Long reportId,
                          Map<String, Object> metadata) {
        this.admin = admin;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportId = reportId;
        this.metadata = metadata;
    }

    public static AdminAuditLog of(User admin,
                                   AdminAuditAction action,
                                   AdminAuditTargetType targetType,
                                   Long targetId,
                                   Long reportId,
                                   Map<String, Object> metadata) {
        return new AdminAuditLog(admin, action, targetType, targetId, reportId, metadata);
    }
}
