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
@Import(NotificationSettingRepository.class)
@ActiveProfiles("test")
class NotificationSettingRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired NotificationSettingRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserDevice device(User user, String deviceId) {
        UserDevice userDevice = UserDevice.create(user, deviceId, "token-" + deviceId, DevicePlatform.IOS);
        em.persist(userDevice);
        return userDevice;
    }

    private void setting(UserDevice device, NotificationType type, boolean enabled) {
        NotificationSetting setting = NotificationSetting.of(device, type, enabled);
        repo.save(setting);
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("findByUserDeviceId")
    void findByUserDeviceId_returnsSettings() {
        User user = user();
        UserDevice device = device(user, "device-1");
        UserDevice otherDevice = device(user, "device-2");
        setting(device, NotificationType.COMMENTED_ON_COLLECTION, true);
        setting(device, NotificationType.LIKED_ON_COLLECTION, false);
        setting(otherDevice, NotificationType.COMMENTED_ON_COLLECTION, true);
        em.flush(); em.clear();

        List<NotificationSetting> result = repo.findByUserDeviceId(device.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationSetting::getType)
                .containsExactlyInAnyOrder(
                        NotificationType.COMMENTED_ON_COLLECTION,
                        NotificationType.LIKED_ON_COLLECTION
                );
    }

    @Test @DisplayName("findByUserDeviceIdAndType - 일치하는 설정만 반환")
    void findByUserDeviceIdAndType_returnsMatch() {
        User user = user();
        UserDevice device = device(user, "device-1");
        setting(device, NotificationType.LIKED_ON_COLLECTION, false);
        em.flush(); em.clear();

        Optional<NotificationSetting> found =
                repo.findByUserDeviceIdAndType(device.getId(), NotificationType.LIKED_ON_COLLECTION);
        Optional<NotificationSetting> missing =
                repo.findByUserDeviceIdAndType(device.getId(), NotificationType.REPLIED_TO_COMMENT);

        assertThat(found).isPresent();
        assertThat(found.get().getEnabled()).isFalse();
        assertThat(missing).isEmpty();
    }

    @Test @DisplayName("findEnabledDeviceIdsByUserAndType - enabled devices만 반환")
    void findEnabledDeviceIdsByUserAndType_returnsEnabled() {
        User user = user();
        User otherUser = user();
        UserDevice enabledDevice = device(user, "device-enabled");
        UserDevice disabledDevice = device(user, "device-disabled");
        UserDevice otherDevice = device(otherUser, "device-other");
        setting(enabledDevice, NotificationType.COMMENTED_ON_COLLECTION, true);
        setting(disabledDevice, NotificationType.COMMENTED_ON_COLLECTION, false);
        setting(otherDevice, NotificationType.COMMENTED_ON_COLLECTION, true);
        em.flush(); em.clear();

        List<Long> deviceIds =
                repo.findEnabledDeviceIdsByUserAndType(user.getId(), NotificationType.COMMENTED_ON_COLLECTION);

        assertThat(deviceIds).containsExactly(enabledDevice.getId());
    }

    @Test @DisplayName("deleteByUserId")
    void deleteByUserId_removesSettings() {
        User user = user();
        UserDevice device1 = device(user, "device-1");
        UserDevice device2 = device(user, "device-2");
        setting(device1, NotificationType.LIKED_ON_COLLECTION, true);
        setting(device2, NotificationType.COMMENTED_ON_COLLECTION, true);

        User otherUser = user();
        UserDevice otherDevice = device(otherUser, "device-3");
        setting(otherDevice, NotificationType.LIKED_ON_COLLECTION, true);
        em.flush(); em.clear();

        int deleted = repo.deleteByUserId(user.getId());
        em.flush(); em.clear();

        assertThat(deleted).isEqualTo(2);
        assertThat(repo.findByUserDeviceId(device1.getId())).isEmpty();
        assertThat(repo.findByUserDeviceId(device2.getId())).isEmpty();
        assertThat(repo.findByUserDeviceId(otherDevice.getId())).hasSize(1);
    }
}
