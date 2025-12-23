package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.BatchedNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationMessagesConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 배치 처리된 알림 렌더러.
 * 액터 수에 따라 단일/집계 메시지를 렌더링한다.
 */
@Component
@RequiredArgsConstructor
public class BatchedNotificationRenderer implements NotificationRenderer {

    private final NotificationMessagesConfig messages;

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof BatchedNotificationPayload b)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        NotificationMessagesConfig.Template t = messages.forType(b.type());

        // 단일 액터인 경우 기존 메시지 형식 사용
        if (b.isSingleActor()) {
            return renderSingle(t, b);
        }

        return renderBatched(t, b);
    }

    private RenderedMessage renderSingle(NotificationMessagesConfig.Template t, BatchedNotificationPayload b) {
        var vars = NotificationTemplateRenderer.toVars(b.extras());
        vars.put("actorName", NotificationTemplateRenderer.nullToEmpty(b.getFirstActor().name()));

        return NotificationTemplateRenderer.render(t, vars);
    }

    private RenderedMessage renderBatched(NotificationMessagesConfig.Template t, BatchedNotificationPayload b) {
        var vars = NotificationTemplateRenderer.toVars(b.extras());
        vars.put("actorName", NotificationTemplateRenderer.nullToEmpty(b.getFirstActor().name()));
        vars.put("count", String.valueOf(b.actorCount()));
        vars.put("othersCount", String.valueOf(b.actorCount() - 1));

        return NotificationTemplateRenderer.renderBatch(t, vars);
    }

}
