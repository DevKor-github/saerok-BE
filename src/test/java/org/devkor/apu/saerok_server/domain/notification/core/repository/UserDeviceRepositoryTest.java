package org.devkor.apu.saerok_server.domain.notification.core.repository;

import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(UserDeviceRepository.class)
@ActiveProfiles("test")
class UserDeviceRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserDeviceRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserDevice device(User user, String deviceId, String token) {
        UserDevice userDevice = UserDevice.create(user, deviceId, token, DevicePlatform.IOS);
        repo.save(userDevice);
        return userDevice;
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save and findById")
    void save_and_findById() {
        User user = user();
        UserDevice device = device(user, "device-1", "token-1");
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findById(device.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDeviceId()).isEqualTo("device-1");
        assertThat(found.get().getToken()).isEqualTo("token-1");
    }

    @Test @DisplayName("findByUserIdAndDeviceIdAndPlatform")
    void findByUserIdAndDeviceIdAndPlatform_returnsMatch() {
        User user = user();
        device(user, "device-1", "token-1");
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findByUserIdAndDeviceIdAndPlatform(user.getId(), "device-1", DevicePlatform.IOS);
        Optional<UserDevice> missingDevice = repo.findByUserIdAndDeviceIdAndPlatform(user.getId(), "device-2", DevicePlatform.IOS);
        Optional<UserDevice> missingPlatform = repo.findByUserIdAndDeviceIdAndPlatform(user.getId(), "device-1", DevicePlatform.ANDROID);

        assertThat(found).isPresent();
        assertThat(missingDevice).isEmpty();
        assertThat(missingPlatform).isEmpty();
    }

    @Test @DisplayName("deactivateByTokens - token만 비활성화하고 디바이스와 설정은 유지")
    void deactivateByTokens_preservesDeviceAndSettings() {
        User user = user();
        UserDevice device = device(user, "device-1", "token-1");
        NotificationSetting setting = NotificationSetting.of(device, NotificationType.LIKED_ON_COLLECTION, false);
        em.persist(setting);
        repo.flush(); em.clear();

        int deactivated = repo.deactivateByTokens(List.of("token-1"));
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findByUserIdAndDeviceIdAndPlatform(user.getId(), "device-1", DevicePlatform.IOS);
        NotificationSetting preservedSetting = em.find(NotificationSetting.class, setting.getId());

        assertThat(deactivated).isEqualTo(1);
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isNull();
        assertThat(preservedSetting).isNotNull();
        assertThat(preservedSetting.getEnabled()).isFalse();
        assertThat(repo.findTokensByUserId(user.getId())).isEmpty();
        assertThat(repo.findTokensByUserDeviceIds(List.of(found.get().getId()))).isEmpty();

        found.get().activateToken("token-new");
        repo.flush(); em.clear();

        UserDevice reactivated = repo.findById(found.get().getId()).orElseThrow();
        NotificationSetting settingAfterReactivation = em.find(NotificationSetting.class, setting.getId());
        assertThat(reactivated.getToken()).isEqualTo("token-new");
        assertThat(settingAfterReactivation.getEnabled()).isFalse();
    }

    @Test @DisplayName("deactivateConflictingTokensForRegistration - 충돌 row의 token만 비활성화")
    void deactivateConflictingTokensForRegistration_preservesRows() {
        User currentUser = user();
        User otherUser = user();
        UserDevice currentDevice = device(currentUser, "device-1", "token-current");
        UserDevice sameTokenDevice = device(otherUser, "device-2", "token-next");
        UserDevice samePhysicalDevice = device(otherUser, "device-1", "token-old");
        UserDevice unrelatedDevice = device(otherUser, "device-4", "token-unrelated");
        repo.flush(); em.clear();

        int deactivated = repo.deactivateConflictingTokensForRegistration(
                currentUser.getId(),
                "device-1",
                DevicePlatform.IOS,
                "token-next"
        );
        repo.flush(); em.clear();

        assertThat(deactivated).isEqualTo(2);
        assertThat(repo.findById(currentDevice.getId()).orElseThrow().getToken()).isEqualTo("token-current");
        assertThat(repo.findById(sameTokenDevice.getId()).orElseThrow().getToken()).isNull();
        assertThat(repo.findById(samePhysicalDevice.getId()).orElseThrow().getToken()).isNull();
        assertThat(repo.findById(unrelatedDevice.getId()).orElseThrow().getToken()).isEqualTo("token-unrelated");
    }

    @Test @DisplayName("deactivateConflictingTokensForRegistration - platform이 다른 같은 deviceId row는 유지")
    void deactivateConflictingTokensForRegistration_keepsDifferentPlatformDevice() {
        User currentUser = user();
        User otherUser = user();
        UserDevice androidDevice = UserDevice.create(otherUser, "device-1", "token-android", DevicePlatform.ANDROID);
        repo.save(androidDevice);
        repo.flush(); em.clear();

        int deactivated = repo.deactivateConflictingTokensForRegistration(
                currentUser.getId(),
                "device-1",
                DevicePlatform.IOS,
                "token-ios"
        );
        repo.flush(); em.clear();

        assertThat(deactivated).isZero();
        assertThat(repo.findByUserIdAndDeviceIdAndPlatform(otherUser.getId(), "device-1", DevicePlatform.ANDROID))
                .isPresent();
    }

    @Test @DisplayName("deleteByUserId - 회원 탈퇴 시에는 디바이스를 영구 삭제")
    void deleteByUserId_removesAllDevices() {
        User user1 = user();
        User user2 = user();
        device(user1, "device-1", "token-1");
        device(user1, "device-2", "token-2");
        device(user2, "device-3", "token-3");
        repo.flush(); em.clear();

        int deleted = repo.deleteByUserId(user1.getId());
        repo.flush(); em.clear();

        assertThat(deleted).isEqualTo(2);
        assertThat(repo.findAllActiveByUserId(user1.getId())).isEmpty();
        assertThat(repo.findAllActiveByUserId(user2.getId())).hasSize(1);
    }

    @Test @DisplayName("partial unique index - 활성 token은 하나의 row에만 귀속")
    void activeToken_mustBeUnique() {
        User user1 = user();
        User user2 = user();
        device(user1, "device-1", "duplicate-token");
        repo.flush();

        device(user2, "device-2", "duplicate-token");

        assertThatThrownBy(repo::flush).isInstanceOf(Exception.class);
    }

    @Test @DisplayName("findAllActiveByUserId - 비활성(token=null) 디바이스는 제외")
    void findAllActiveByUserId_excludesInactive() {
        User user = user();
        UserDevice active = device(user, "device-1", "token-1");
        UserDevice inactive = device(user, "device-2", "token-2");
        inactive.deactivateToken();
        repo.flush(); em.clear();

        List<UserDevice> devices = repo.findAllActiveByUserId(user.getId());

        assertThat(devices).extracting(UserDevice::getId)
                .containsExactly(active.getId());
    }
}
