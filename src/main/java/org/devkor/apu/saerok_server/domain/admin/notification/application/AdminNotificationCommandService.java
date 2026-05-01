package org.devkor.apu.saerok_server.domain.admin.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.admin.notification.application.event.AdminNotificationEvent;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminNotificationCommandService {

    private final ApplicationEventPublisher eventPublisher;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserRepository userRepository;

    public void sendMessageToUsers(Long adminUserId, List<Long> userIds, String title, String body) {
        eventPublisher.publishEvent(
                new AdminNotificationEvent.AdminMessageSent(userIds, title, body)
        );

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 계정이 존재하지 않아요"));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("recipientCount", userIds.size());
        metadata.put("recipientIds", userIds);
        metadata.put("title", title);
        metadata.put("body", body);

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                AdminAuditAction.ADMIN_MESSAGE_SENT,
                AdminAuditTargetType.ADMIN_MESSAGE,
                null,
                null,
                metadata
        ));
    }
}
