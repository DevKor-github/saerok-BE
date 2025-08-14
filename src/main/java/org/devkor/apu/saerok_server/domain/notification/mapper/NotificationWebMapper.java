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

    default GetNotificationsResponse toGetNotificationsResponse(List<Notification> notifications) {
        List<GetNotificationsResponse.Item> items = notifications.stream()
                .map(this::toItem)
                .toList();
        return new GetNotificationsResponse(items);
    }

    GetUnreadCountResponse toGetUnreadCountResponse(Long unreadCount);

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "actorNickname", source = "actor.nickname")
    GetNotificationsResponse.Item toItem(Notification notification);
}
