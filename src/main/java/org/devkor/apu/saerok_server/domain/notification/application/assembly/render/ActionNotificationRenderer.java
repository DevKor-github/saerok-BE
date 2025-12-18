package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationMessagesConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActionNotificationRenderer implements NotificationRenderer {

    private final NotificationMessagesConfig messages;

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        NotificationType type = a.type();
        NotificationMessagesConfig.Template t = messages.forType(type);

        Map<String, String> vars = new HashMap<>();
        vars.put("actorName", nullToEmpty(a.actorName()));
        a.extras().forEach((k, v) -> vars.put(k, v == null ? "" : String.valueOf(v)));

        String title = renderTemplate(t.getPushTitle(), vars);
        String body  = renderTemplate(t.getPushBody(), vars);

        return new RenderedMessage(title, body);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * <h2>간단한 템플릿 렌더링</h2>
     *
     * <ul>
     *   <li>치환 규칙: <code>{key}</code> → vars.get(key)</li>
     *   <li>이스케이프: <code>{{</code> → <code>{</code>, <code>}}</code> → <code>}</code></li>
     * </ul>
     */
    private String renderTemplate(String template, Map<String, String> vars) {
        if (template == null) return "";
        String out = template.replace("{{", "\u0000").replace("}}", "\u0001"); // 임시 마커
        for (Map.Entry<String, String> e : vars.entrySet()) {
            String key = "{" + e.getKey() + "}";
            out = out.replace(key, e.getValue() == null ? "" : e.getValue());
        }
        // 이스케이프 복원
        return out.replace("\u0000", "{").replace("\u0001", "}");
    }
}
