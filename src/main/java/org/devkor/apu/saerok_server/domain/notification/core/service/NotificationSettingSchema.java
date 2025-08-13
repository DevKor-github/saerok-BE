package org.devkor.apu.saerok_server.domain.notification.core.service;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/** 각 유저디바이스에 "반드시 존재해야 하는" NotificationSetting (subject/action) 조합을 정의 */
@Component
public class NotificationSettingSchema {

    /** 현재 스펙: COLLECTION 그룹(=action null) + LIKE/COMMENT/SUGGEST_BIRD_ID 3종 */
    public Set<SubjectActionKey> requiredKeys() {
        Set<SubjectActionKey> keys = new LinkedHashSet<>();
        keys.add(new SubjectActionKey(NotificationSubject.COLLECTION, null)); // 그룹 토글

        keys.add(new SubjectActionKey(NotificationSubject.COLLECTION, NotificationAction.LIKE));
        keys.add(new SubjectActionKey(NotificationSubject.COLLECTION, NotificationAction.COMMENT));
        keys.add(new SubjectActionKey(NotificationSubject.COLLECTION, NotificationAction.SUGGEST_BIRD_ID));
        return keys;
    }

    public record SubjectActionKey(NotificationSubject subject, NotificationAction action) {}
}
