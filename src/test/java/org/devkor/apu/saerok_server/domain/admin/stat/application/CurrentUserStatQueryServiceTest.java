package org.devkor.apu.saerok_server.domain.admin.stat.application;

import org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response.CurrentUserStatResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CurrentUserStatQueryService.class)
@ActiveProfiles("test")
class CurrentUserStatQueryServiceTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired CurrentUserStatQueryService sut;

    @Test
    @DisplayName("현재 가입 완료 사용자, 가입 경로, 활성 푸시 플랫폼을 함께 집계한다")
    void getCurrentUserStats_countsOnlyCurrentCompletedUsersAndActivePushDevices() {
        User iosOnly = completedUser(SignupSourceType.INSTAGRAM);
        activeDevice(iosOnly, "ios-device-1", DevicePlatform.IOS);

        User bothPlatforms = completedUser(SignupSourceType.FRIEND);
        activeDevice(bothPlatforms, "ios-device-2", DevicePlatform.IOS);
        activeDevice(bothPlatforms, "android-device-1", DevicePlatform.ANDROID);

        completedUser(null);

        User inactiveDeviceUser = completedUser(SignupSourceType.INSTAGRAM);
        UserDevice inactiveDevice = activeDevice(inactiveDeviceUser, "android-device-2", DevicePlatform.ANDROID);
        inactiveDevice.deactivateToken();

        User incompleteUser = new UserBuilder(em).build();
        activeDevice(incompleteUser, "ios-device-3", DevicePlatform.IOS);

        User deletedUser = completedUser(SignupSourceType.COMMUNITY);
        activeDevice(deletedUser, "android-device-3", DevicePlatform.ANDROID);
        deletedUser.softDelete();

        em.flush();
        em.clear();

        CurrentUserStatResponse response = sut.getCurrentUserStats();

        assertThat(response.completedUserCount()).isEqualTo(4L);
        assertThat(response.signupSourceCounts())
                .containsEntry("INSTAGRAM", 2L)
                .containsEntry("FRIEND", 1L)
                .containsEntry("UNKNOWN", 1L)
                .containsEntry("COMMUNITY", 0L);
        assertThat(response.activePushUserCountsByPlatform())
                .containsEntry("IOS", 2L)
                .containsEntry("ANDROID", 1L);
    }

    private User completedUser(SignupSourceType signupSource) {
        User user = new UserBuilder(em).build();
        user.setSignupSource(signupSource);
        user.setSignupStatus(SignupStatusType.COMPLETED);
        return user;
    }

    private UserDevice activeDevice(User user, String deviceId, DevicePlatform platform) {
        UserDevice device = UserDevice.create(user, deviceId, "token-" + deviceId, platform);
        em.persist(device);
        return device;
    }
}
