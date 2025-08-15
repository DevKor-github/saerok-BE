package org.devkor.apu.saerok_server.domain.notification.core.service;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class NotificationTypeSchema {
    public Set<NotificationType> requiredTypes() {
        return EnumSet.allOf(NotificationType.class);
    }
}
