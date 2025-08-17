package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetNotificationsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetUnreadCountResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring",
        imports = OffsetDateTimeLocalizer.class)
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
    @Mapping(target = "createdAt", expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDateTime(notification.getCreatedAt()))")
    @Mapping(target = "payload", expression = "java(notification.getPayload())")
    GetNotificationsResponse.Item toItem(Notification notification);

}
