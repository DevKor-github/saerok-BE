package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushGateway pushGateway;

    public void readNotification(Long userId, Long notificationId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없어요"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 알림에 대한 권한이 없어요");
        }

        notification.markAsRead();

        int unread = notificationRepository.countUnreadByUserId(userId).intValue();
        pushGateway.sendToUser(userId, NotificationSubject.APP, NotificationAction.BADGE_REFRESH, PushMessageCommand.forBadge(unread));
    }

    public void readAllNotifications(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        notificationRepository.markAllAsReadByUserId(userId);

        int unread = notificationRepository.countUnreadByUserId(userId).intValue();
        pushGateway.sendToUser(userId, NotificationSubject.APP, NotificationAction.BADGE_REFRESH, PushMessageCommand.forBadge(unread));
    }

    public void deleteNotification(Long userId, Long notificationId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없어요"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 알림에 대한 권한이 없어요");
        }

        notificationRepository.remove(notification);
    }

    public void deleteAllNotifications(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        notificationRepository.deleteByUserId(userId);
    }
}
