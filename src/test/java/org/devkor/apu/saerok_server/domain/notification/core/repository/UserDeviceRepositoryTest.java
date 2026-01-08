package org.devkor.apu.saerok_server.domain.notification.core.repository;

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
        UserDevice userDevice = UserDevice.create(user, deviceId, token);
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

    @Test @DisplayName("findByUserIdAndDeviceId")
    void findByUserIdAndDeviceId_returnsMatch() {
        User user = user();
        device(user, "device-1", "token-1");
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findByUserIdAndDeviceId(user.getId(), "device-1");
        Optional<UserDevice> missing = repo.findByUserIdAndDeviceId(user.getId(), "device-2");

        assertThat(found).isPresent();
        assertThat(missing).isEmpty();
    }

    @Test @DisplayName("deleteByUserIdAndDeviceId")
    void deleteByUserIdAndDeviceId_removesDevice() {
        User user = user();
        device(user, "device-1", "token-1");
        repo.flush(); em.clear();

        repo.deleteByUserIdAndDeviceId(user.getId(), "device-1");
        repo.flush(); em.clear();

        Optional<UserDevice> found = repo.findByUserIdAndDeviceId(user.getId(), "device-1");
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

    @Test @DisplayName("findTokensByUserDeviceIds")
    void findTokensByUserDeviceIds_returnsTokens() {
        User user = user();
        UserDevice first = device(user, "device-1", "token-1");
        UserDevice second = device(user, "device-2", "token-2");
        repo.flush(); em.clear();

        List<String> tokens = repo.findTokensByUserDeviceIds(List.of(first.getId(), second.getId()));
        List<String> emptyTokens = repo.findTokensByUserDeviceIds(List.of());

        assertThat(tokens).containsExactlyInAnyOrder("token-1", "token-2");
        assertThat(emptyTokens).isEmpty();
    }

    @Test @DisplayName("findTokensByUserId")
    void findTokensByUserId_returnsTokens() {
        User user = user();
        device(user, "device-1", "token-1");
        device(user, "device-2", "token-2");
        repo.flush(); em.clear();

        List<String> tokens = repo.findTokensByUserId(user.getId());

        assertThat(tokens).containsExactlyInAnyOrder("token-1", "token-2");
    }
}
