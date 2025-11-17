package org.devkor.apu.saerok_server.domain.admin.audit.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.audit.api.dto.response.AdminAuditLogListResponse;
import org.devkor.apu.saerok_server.domain.admin.audit.application.dto.AdminAuditQueryCommand;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminAuditQueryService {

    private final AdminAuditLogRepository repository;

    public AdminAuditLogListResponse list(AdminAuditQueryCommand cmd) {
        List<AdminAuditLog> logs = (cmd.page() != null && cmd.size() != null)
                ? repository.findPageOrderByCreatedAtDesc(cmd.page(), cmd.size())
                : repository.findAllOrderByCreatedAtDesc();

        var items = logs.stream().map(l ->
                new AdminAuditLogListResponse.Item(
                        l.getId(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(l.getCreatedAt()),
                        new AdminAuditLogListResponse.UserMini(l.getAdmin().getId(), l.getAdmin().getNickname()),
                        l.getAction().name(),
                        l.getTargetType().name(),
                        l.getTargetId(),
                        l.getReportId(),
                        l.getMetadata()
                )
        ).toList();

        return new AdminAuditLogListResponse(items);
    }
}
