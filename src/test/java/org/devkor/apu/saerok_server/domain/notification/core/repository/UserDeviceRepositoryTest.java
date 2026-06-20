package org.devkor.apu.saerok_server.domain.notification.core.repository;

import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
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

    @Test @DisplayName("deleteByUserIdAndDeviceIdAndPlatform")
    void deleteByUserIdAndDeviceIdAndPlatform_removesDevice() {
        User user = user();
        device(user, "device-1", "token-1");
        repo.flush(); em.clear();

        repo.deleteByUserIdAndDeviceIdAndPlatform(user.getId(), "device-1", DevicePlatform.IOS);
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findByUserIdAndDeviceIdAndPlatform(user.getId(), "device-1", DevicePlatform.IOS);
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("deleteByToken")
    void deleteByToken_removesDevice() {
        User user = user();
        device(user, "device-1", "token-1");
        device(user, "device-2", "token-2");
        repo.flush(); em.clear();

        repo.deleteByToken("token-1");
        repo.flush(); em.clear();

        List<UserDevice> devices = repo.findAllByUserId(user.getId());
        assertThat(devices).hasSize(1);
        assertThat(devices.getFirst().getToken()).isEqualTo("token-2");
    }

    @Test @DisplayName("deleteConflictingDevicesForRegistration - 등록 대상 외 같은 token 또는 같은 device/platform row 삭제")
    void deleteConflictingDevicesForRegistration_removesStaleRows() {
        User currentUser = user();
        User otherUser = user();
        device(currentUser, "device-1", "token-1");
        device(currentUser, "device-3", "token-1");
        device(otherUser, "device-2", "token-1");
        device(otherUser, "device-1", "token-2");
        device(otherUser, "device-4", "token-4");
        repo.flush(); em.clear();

        int deleted = repo.deleteConflictingDevicesForRegistration(
                currentUser.getId(),
                "device-1",
                DevicePlatform.IOS,
                "token-1"
        );
        repo.flush(); em.clear();

        assertThat(deleted).isEqualTo(3);
        assertThat(repo.findAllByUserId(currentUser.getId()))
                .extracting(UserDevice::getDeviceId)
                .containsExactly("device-1");
        assertThat(repo.findAllByUserId(otherUser.getId()))
                .extracting(UserDevice::getDeviceId)
                .containsExactly("device-4");
    }

    @Test @DisplayName("deleteConflictingDevicesForRegistration - platform이 다른 같은 deviceId row는 token이 다르면 유지")
    void deleteConflictingDevicesForRegistration_keepsDifferentPlatformDevice() {
        User currentUser = user();
        User otherUser = user();
        UserDevice androidDevice = UserDevice.create(otherUser, "device-1", "token-android", DevicePlatform.ANDROID);
        repo.save(androidDevice);
        repo.flush(); em.clear();

        int deleted = repo.deleteConflictingDevicesForRegistration(
                currentUser.getId(),
                "device-1",
                DevicePlatform.IOS,
                "token-ios"
        );
        repo.flush(); em.clear();

        assertThat(deleted).isZero();
        assertThat(repo.findByUserIdAndDeviceIdAndPlatform(otherUser.getId(), "device-1", DevicePlatform.ANDROID))
                .isPresent();
    }

    @Test @DisplayName("deleteByUserId")
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
        assertThat(repo.findAllByUserId(user1.getId())).isEmpty();
        assertThat(repo.findAllByUserId(user2.getId())).hasSize(1);
    }

    @Test @DisplayName("findAllByUserId")
    void findAllByUserId_returnsDevices() {
        User user = user();
        UserDevice first = device(user, "device-1", "token-1");
        UserDevice second = device(user, "device-2", "token-2");
        repo.flush(); em.clear();

        List<UserDevice> devices = repo.findAllByUserId(user.getId());

        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(UserDevice::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }
}
