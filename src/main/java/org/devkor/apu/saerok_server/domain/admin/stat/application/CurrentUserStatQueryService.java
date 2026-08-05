package org.devkor.apu.saerok_server.domain.admin.stat.application;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response.CurrentUserStatResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupSourceType;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurrentUserStatQueryService {

    private static final String UNKNOWN_SIGNUP_SOURCE = "UNKNOWN";

    private final EntityManager em;

    public CurrentUserStatResponse getCurrentUserStats() {
        return new CurrentUserStatResponse(
                countCompletedUsers(),
                countCompletedUsersBySignupSource(),
                countActivePushUsersByPlatform()
        );
    }

    private long countCompletedUsers() {
        return em.createQuery("""
                SELECT COUNT(u) FROM User u
                WHERE u.signupStatus = :completed
                  AND u.deletedAt IS NULL
                """, Long.class)
                .setParameter("completed", SignupStatusType.COMPLETED)
                .getSingleResult();
    }

    private Map<String, Long> countCompletedUsersBySignupSource() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (SignupSourceType source : SignupSourceType.values()) {
            counts.put(source.name(), 0L);
        }
        counts.put(UNKNOWN_SIGNUP_SOURCE, 0L);

        List<Object[]> rows = em.createQuery("""
                SELECT u.signupSource, COUNT(u) FROM User u
                WHERE u.signupStatus = :completed
                  AND u.deletedAt IS NULL
                GROUP BY u.signupSource
                """, Object[].class)
                .setParameter("completed", SignupStatusType.COMPLETED)
                .getResultList();

        for (Object[] row : rows) {
            String key = row[0] == null ? UNKNOWN_SIGNUP_SOURCE : row[0].toString();
            counts.put(key, ((Number) row[1]).longValue());
        }
        return counts;
    }

    private Map<String, Long> countActivePushUsersByPlatform() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (DevicePlatform platform : DevicePlatform.values()) {
            counts.put(platform.name(), 0L);
        }

        List<Object[]> rows = em.createQuery("""
                SELECT ud.platform, COUNT(DISTINCT u.id) FROM UserDevice ud
                JOIN ud.user u
                WHERE ud.token IS NOT NULL
                  AND u.signupStatus = :completed
                  AND u.deletedAt IS NULL
                GROUP BY ud.platform
                """, Object[].class)
                .setParameter("completed", SignupStatusType.COMPLETED)
                .getResultList();

        for (Object[] row : rows) {
            counts.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return counts;
    }
}
