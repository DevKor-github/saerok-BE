package org.devkor.apu.saerok_server.domain.admin.stat.application;

import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.BirdIdRequestHistoryRepository;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.DailyStatRepository;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupSourceType;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        StatAggregationService.class,
        DailyStatRepository.class,
        BirdIdRequestHistoryRepository.class
})
@ActiveProfiles("test")
class PlatformSignupTrendAggregationServiceTest extends AbstractPostgresContainerTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired TestEntityManager em;
    @Autowired StatAggregationService sut;
    @Autowired DailyStatRepository dailyRepo;

    @Test
    @DisplayName("플랫폼 가입 증분은 첫 기기 등록과 가입 완료가 모두 충족된 날에 사용자당 한 번만 저장한다")
    void aggregatePlatformSignupDaily_countsFirstPlatformAdoptionOnly() {
        User iosOnly = completedUser();
        device(iosOnly, "ios-device-1", DevicePlatform.IOS);
        device(iosOnly, "ios-device-2", DevicePlatform.IOS);

        User bothPlatforms = completedUser();
        device(bothPlatforms, "ios-device-3", DevicePlatform.IOS);
        device(bothPlatforms, "android-device-1", DevicePlatform.ANDROID);

        User incomplete = new UserBuilder(em).build();
        device(incomplete, "android-device-2", DevicePlatform.ANDROID);

        User withdrawn = completedUser();
        device(withdrawn, "android-device-3", DevicePlatform.ANDROID);
        withdrawn.anonymizeForWithdrawal();

        em.flush();
        em.clear();

        LocalDate date = LocalDate.now(KST);
        sut.aggregateFor(date, EnumSet.of(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE));

        Map<String, Object> payload = dailyRepo
                .findByMetricAndDate(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, date)
                .orElseThrow()
                .getPayload();

        assertThat(((Number) payload.get("IOS")).longValue()).isEqualTo(2L);
        assertThat(((Number) payload.get("ANDROID")).longValue()).isEqualTo(2L);
    }

    @Test
    @DisplayName("기기 등록이 가입 완료보다 빠르면 가입 완료일에 플랫폼 가입 증분을 저장한다")
    void aggregatePlatformSignupDaily_usesTheLaterOfDeviceRegistrationAndSignupCompletion() {
        LocalDate signupDate = LocalDate.now(KST);
        User user = new UserBuilder(em).build();
        UserDevice device = UserDevice.create(user, "ios-device-before-signup", "token-before-signup", DevicePlatform.IOS);
        em.persist(device);
        em.flush();

        updateDeviceCreatedAt(device.getId(), signupDate.minusDays(1).atTime(10, 0).atZone(KST).toOffsetDateTime());
        completeOn(user.getId(), signupDate.atTime(10, 0).atZone(KST).toOffsetDateTime());
        em.clear();

        sut.aggregateFor(signupDate, EnumSet.of(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE));

        Map<String, Object> payload = dailyRepo
                .findByMetricAndDate(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, signupDate)
                .orElseThrow()
                .getPayload();

        assertThat(((Number) payload.get("IOS")).longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("첫 집계는 기존 플랫폼 가입자를 기준값으로 저장하고 이후 집계에서 다시 더하지 않는다")
    void aggregatePlatformSignupDaily_seedsExistingUsersOnlyOnTheFirstAggregation() {
        LocalDate firstAggregationDate = LocalDate.now(KST);
        User user = new UserBuilder(em).build();
        UserDevice device = UserDevice.create(user, "existing-ios-device", "existing-token", DevicePlatform.IOS);
        em.persist(device);
        em.flush();

        completeOn(user.getId(), firstAggregationDate.minusDays(5).atTime(10, 0).atZone(KST).toOffsetDateTime());
        updateDeviceCreatedAt(device.getId(), firstAggregationDate.minusDays(4).atTime(10, 0).atZone(KST).toOffsetDateTime());
        em.clear();

        sut.aggregateFor(firstAggregationDate, EnumSet.of(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE));
        sut.aggregateFor(firstAggregationDate.plusDays(1), EnumSet.of(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE));

        Map<String, Object> firstPayload = dailyRepo
                .findByMetricAndDate(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, firstAggregationDate)
                .orElseThrow()
                .getPayload();
        Map<String, Object> nextPayload = dailyRepo
                .findByMetricAndDate(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, firstAggregationDate.plusDays(1))
                .orElseThrow()
                .getPayload();

        assertThat(((Number) firstPayload.get("IOS")).longValue()).isEqualTo(1L);
        assertThat(nextPayload).doesNotContainKey("IOS");
    }

    private User completedUser() {
        User user = new UserBuilder(em).build();
        user.setSignupSource(SignupSourceType.INSTAGRAM);
        user.setSignupStatus(SignupStatusType.COMPLETED);
        return user;
    }

    private void device(User user, String deviceId, DevicePlatform platform) {
        em.persist(UserDevice.create(user, deviceId, "token-" + deviceId, platform));
    }

    private void updateDeviceCreatedAt(Long deviceId, OffsetDateTime createdAt) {
        em.getEntityManager()
                .createNativeQuery("UPDATE user_device SET created_at = ?1 WHERE id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, deviceId)
                .executeUpdate();
    }

    private void completeOn(Long userId, OffsetDateTime completedAt) {
        em.getEntityManager()
                .createNativeQuery("""
                        UPDATE users
                           SET signup_status = 'COMPLETED',
                               signup_completed_at = ?1
                         WHERE id = ?2
                        """)
                .setParameter(1, completedAt)
                .setParameter(2, userId)
                .executeUpdate();
    }
}
