package org.devkor.apu.saerok_server.domain.notification.application;

import org.devkor.apu.saerok_server.domain.notification.application.dto.RegisterUserDeviceCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeSchema;
import org.devkor.apu.saerok_server.domain.notification.mapper.UserDeviceWebMapperImpl;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 디바이스 토큰 생애주기 시나리오를 실제 서비스 구동으로 검증한다.
 *
 * <p>로그아웃의 디바이스 측 효과는 {@code LogoutService}가 호출하는
 * {@link UserDeviceCommandService#deactivateDeviceIfPresent}로 그대로 재현한다.
 */
@DataJpaTest
@Import({
        UserDeviceCommandService.class,
        UserDeviceRepository.class,
        UserRepository.class,
        UserDeviceWebMapperImpl.class,
        NotificationSettingBackfillService.class,
        NotificationTypeSchema.class,
        NotificationSettingRepository.class
})
@ActiveProfiles("test")
class UserDeviceLifecycleScenarioTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserDeviceCommandService service;
    @Autowired UserDeviceRepository userDeviceRepository;
    @Autowired NotificationSettingRepository settingRepository;

    private static final DevicePlatform IOS = DevicePlatform.IOS;

    /* ----------------------------- helpers ----------------------------- */

    private User newUser() {
        User u = new UserBuilder(em).build();
        em.flush();
        return u;
    }

    /** 로그인 후 앱이 호출하는 디바이스 등록. 등록된 row id 반환. */
    private Long register(Long userId, String deviceId, String token) {
        service.registerUserDevice(new RegisterUserDeviceCommand(userId, deviceId, token, IOS));
        em.flush(); em.clear();
        return row(userId, deviceId).orElseThrow().getId();
    }

    /** 로그아웃의 디바이스 측 효과(토큰 비활성화). */
    private void logout(Long userId, String deviceId) {
        service.deactivateDeviceIfPresent(userId, deviceId, IOS);
        em.flush(); em.clear();
    }

    private Optional<UserDevice> row(Long userId, String deviceId) {
        return userDeviceRepository.findByUserIdAndDeviceIdAndPlatform(userId, deviceId, IOS);
    }

    private String token(Long userId, String deviceId) {
        return row(userId, deviceId).orElseThrow().getToken();
    }

    /* ----------------------------- scenarios ----------------------------- */

    @Test
    @DisplayName("① A 로그아웃 → 같은 기기에 B 로그인: A는 휴면(token=null), B만 활성")
    void scenario1_bTakesOverAfterLogout() {
        User a = newUser();
        User b = newUser();

        register(a.getId(), "D", "T");
        logout(a.getId(), "D");
        register(b.getId(), "D", "T"); // 같은 deviceId + 같은 FCM 토큰

        assertThat(row(a.getId(), "D")).isPresent();          // 행은 보존
        assertThat(token(a.getId(), "D")).isNull();            // 비활성
        assertThat(token(b.getId(), "D")).isEqualTo("T");      // B 활성
        assertThat(userDeviceRepository.findAllActiveByUserId(a.getId())).isEmpty();
        assertThat(userDeviceRepository.findAllActiveByUserId(b.getId())).hasSize(1);
    }

    @Test
    @DisplayName("② A 로그아웃 → A 재로그인(같은 기기): 같은 행 재활성 + 알림 설정 보존")
    void scenario2_sameUserReloginPreservesSettings() {
        User a = newUser();
        Long deviceRowId = register(a.getId(), "D", "T");

        // A가 특정 알림을 끈다
        NotificationSetting s = settingRepository
                .findByUserDeviceIdAndType(deviceRowId, NotificationType.LIKED_ON_COLLECTION)
                .orElseThrow();
        if (Boolean.TRUE.equals(s.getEnabled())) s.toggle();
        em.flush(); em.clear();
        assertThat(settingRepository
                .findByUserDeviceIdAndType(deviceRowId, NotificationType.LIKED_ON_COLLECTION)
                .orElseThrow().getEnabled()).isFalse();

        logout(a.getId(), "D");
        assertThat(token(a.getId(), "D")).isNull();

        Long reRowId = register(a.getId(), "D", "T");

        assertThat(reRowId).isEqualTo(deviceRowId);            // 새 행 생성 아님, 같은 행 재사용
        assertThat(token(a.getId(), "D")).isEqualTo("T");      // 재활성화
        assertThat(settingRepository                            // 끈 설정 그대로 보존
                .findByUserDeviceIdAndType(deviceRowId, NotificationType.LIKED_ON_COLLECTION)
                .orElseThrow().getEnabled()).isFalse();
    }

    @Test
    @DisplayName("③ A 로그아웃 → A가 다른 기기로 로그인: 옛 기기는 휴면(행/설정 보존), 새 기기 활성")
    void scenario3_sameUserDifferentDevice() {
        User a = newUser();
        Long dRow = register(a.getId(), "D", "T1");
        logout(a.getId(), "D");
        Long eRow = register(a.getId(), "E", "T2");

        assertThat(eRow).isNotEqualTo(dRow);
        assertThat(token(a.getId(), "D")).isNull();            // 옛 기기 휴면
        assertThat(settingRepository.findByUserDeviceId(dRow)).isNotEmpty(); // 옛 기기 설정 보존
        assertThat(token(a.getId(), "E")).isEqualTo("T2");     // 새 기기 활성
        assertThat(userDeviceRepository.findAllActiveByUserId(a.getId()))
                .extracting(UserDevice::getId)
                .containsExactly(eRow);
    }

    @Test
    @DisplayName("④ 두 기기 로그인 중 한 기기만 로그아웃: 나머지 기기는 계속 활성")
    void scenario4_multiDeviceLogoutOne() {
        User a = newUser();
        register(a.getId(), "D", "T1");
        Long eRow = register(a.getId(), "E", "T2");

        logout(a.getId(), "D");

        assertThat(token(a.getId(), "D")).isNull();
        assertThat(token(a.getId(), "E")).isEqualTo("T2");
        assertThat(userDeviceRepository.findAllActiveByUserId(a.getId()))
                .extracting(UserDevice::getId)
                .containsExactly(eRow);
    }

    @Test
    @DisplayName("⑤ A가 로그아웃 안 하고 같은 기기에 B 로그인: 등록 충돌정리가 A 토큰을 비활성화")
    void scenario5_bTakesOverWithoutLogout() {
        User a = newUser();
        User b = newUser();

        register(a.getId(), "D", "T");
        // A는 로그아웃하지 않음
        register(b.getId(), "D", "T");

        assertThat(token(a.getId(), "D")).isNull();            // 충돌정리(token=T 일치)로 비활성화
        assertThat(token(b.getId(), "D")).isEqualTo("T");
        assertThat(userDeviceRepository.findAllActiveByUserId(a.getId())).isEmpty();
        assertThat(userDeviceRepository.findAllActiveByUserId(b.getId())).hasSize(1);
    }
}
