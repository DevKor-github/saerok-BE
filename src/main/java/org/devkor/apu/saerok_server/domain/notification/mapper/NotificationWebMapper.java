package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetNotificationsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetUnreadCountResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface NotificationWebMapper {

    GetNotificationsResponse toGetNotificationsResponse(List<Notification> notifications);

    GetUnreadCountResponse toGetUnreadCountResponse(Long unreadCount);

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderNickname", source = "sender.nickname")
    GetNotificationsResponse.NotificationDto toNotificationDto(Notification notification);
}
