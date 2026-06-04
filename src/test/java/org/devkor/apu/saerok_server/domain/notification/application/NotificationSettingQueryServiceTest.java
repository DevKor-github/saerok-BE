package org.devkor.apu.saerok_server.domain.notification.application;

import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationSettingBackfillService;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeSchema;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingWebMapper;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        NotificationSettingQueryService.class,
        NotificationSettingBackfillService.class,
        NotificationTypeSchema.class,
        NotificationSettingRepository.class,
        UserDeviceRepository.class,
        NotificationSettingWebMapper.class
})
@ActiveProfiles("test")
class NotificationSettingQueryServiceTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired NotificationSettingQueryService service;
    @Autowired UserDeviceRepository userDeviceRepository;
    @Autowired NotificationSettingRepository settingRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("설정 조회 시 누락된 자유게시판 알림 타입까지 기본 설정으로 백필한다")
    void getNotificationSettings_backfillsMissingTypes() {
        Long userId = new TransactionTemplate(transactionManager).execute(status -> {
            User user = new UserBuilder(em).build();
            UserDevice device = UserDevice.create(user, "device-1", "token-1", DevicePlatform.IOS);
            userDeviceRepository.save(device);
            userDeviceRepository.flush();

            settingRepository.save(NotificationSetting.of(device, NotificationType.LIKED_ON_COLLECTION, true));
            em.flush();
            em.clear();
            return user.getId();
        });

        try {
            NotificationSettingsResponse response =
                    service.getNotificationSettings(userId, "device-1", DevicePlatform.IOS);
            NotificationSettingsResponse secondResponse =
                    service.getNotificationSettings(userId, "device-1", DevicePlatform.IOS);

            assertThat(response.items()).hasSize(NotificationType.values().length);
            assertThat(secondResponse.items()).hasSize(NotificationType.values().length);
            assertThat(response.items())
                    .extracting(NotificationSettingsResponse.Item::type)
                    .containsAll(Arrays.asList(NotificationType.values()));
            assertThat(response.items())
                    .filteredOn(item -> item.type() == NotificationType.COMMENTED_ON_FREE_BOARD_POST
                            || item.type() == NotificationType.REPLIED_TO_FREE_BOARD_COMMENT)
                    .extracting(NotificationSettingsResponse.Item::enabled)
                    .containsOnly(true);
        } finally {
            new TransactionTemplate(transactionManager).execute(status -> {
                settingRepository.deleteByUserId(userId);
                userDeviceRepository.deleteByUserId(userId);

                User user = em.find(User.class, userId);
                if (user != null) {
                    em.remove(user);
                }
                em.flush();
                return null;
            });
        }
    }
}
