package org.devkor.apu.saerok_server.domain.admin.notification.application.event;

import java.util.List;

public sealed interface AdminNotificationEvent {

    record AdminMessageSent(
            List<Long> recipientIds,
            String title,
            String body
    ) implements AdminNotificationEvent {}

    record ContentDeletedByReport(
            Long contentOwnerId,
            String reason
    ) implements AdminNotificationEvent {}
}
