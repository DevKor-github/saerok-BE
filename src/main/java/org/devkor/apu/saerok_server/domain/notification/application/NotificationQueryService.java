package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetNotificationsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetUnreadCountResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationWebMapper notificationWebMapper;
    private final UserProfileImageUrlService userProfileImageUrlService;

    public GetNotificationsResponse getNotifications(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notificationWebMapper.toGetNotificationsResponse(notifications, userProfileImageUrlService);
    }

    public GetUnreadCountResponse getUnreadCount(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        return notificationWebMapper.toGetUnreadCountResponse(unreadCount);
    }
}
