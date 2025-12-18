package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationMessagesConfig;

import java.util.HashMap;
import java.util.Map;

final class NotificationTemplateRenderer {

    private NotificationTemplateRenderer() {
    }

    static RenderedMessage render(NotificationMessagesConfig.Template template, Map<String, String> vars) {
        String title = renderTemplate(template.getPushTitle(), vars);
        String body = renderTemplate(template.getPushBody(), vars);
        return new RenderedMessage(title, body);
    }

    static Map<String, String> toVars(Map<String, Object> extras) {
        Map<String, String> vars = new HashMap<>();
        extras.forEach((k, v) -> vars.put(k, v == null ? "" : String.valueOf(v)));
        return vars;
    }

    static String nullToEmpty(String s) {
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
    private static String renderTemplate(String template, Map<String, String> vars) {
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
