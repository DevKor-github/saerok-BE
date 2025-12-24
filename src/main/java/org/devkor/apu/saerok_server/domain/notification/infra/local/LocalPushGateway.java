package org.devkor.apu.saerok_server.domain.notification.infra.local;

import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushTarget;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class LocalPushGateway implements PushGateway {

    @Override
    public void sendToUser(Long userId, NotificationType type, PushMessageCommand cmd) {

        log.info("""
                
                ┌───────────────── LOCAL PUSH (SIMULATED) ─────────────────┐
                │ userId          : {}
                │ type            : {}
                │ title           : {}
                │ body            : {}
                │ relatedId       : {}
                │ notificationId  : {}
                │ unreadCount     : {}
                └──────────────────────────────────────────────────────────┘
                """,
                userId,
                type,
                safe(cmd.title()),
                safe(cmd.body()),
                cmd.relatedId(),
                cmd.notificationId(),
                cmd.unreadCount()
        );
    }

    @Override
    public void sendToUsersDeduplicated(java.util.List<PushTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return;
        }

        for (PushTarget target : targets) {
            if (target == null) {
                continue;
            }
            sendToUser(target.userId(), target.type(), target.command());
        }
    }

    @Override
    public void sendSilentBadgeUpdate(Long userId, int unreadCount) {
        log.info("""
                
                ┌─────────────── LOCAL SILENT BADGE UPDATE (SIMULATED) ────────────┐
                │ userId       : {}
                │ unreadCount  : {}
                │ purpose      : Badge Update Only
                └──────────────────────────────────────────────────────────┘
                """,
                userId,
                unreadCount
        );
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\n", "\\n");
    }
}
