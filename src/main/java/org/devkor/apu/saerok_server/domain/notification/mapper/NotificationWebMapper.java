package org.devkor.apu.saerok_server.domain.notification.mapper;

import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetNotificationsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetUnreadCountResponse;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = OffsetDateTimeLocalizer.class)
public interface NotificationWebMapper {

    default GetNotificationsResponse toGetNotificationsResponse(
            List<Notification> notifications,
            @Context UserProfileImageUrlService userProfileImageUrlService
    ) {
        List<GetNotificationsResponse.Item> items = notifications.stream()
                .map(n -> toItem(n, userProfileImageUrlService))
                .toList();
        return new GetNotificationsResponse(items);
    }

    GetUnreadCountResponse toGetUnreadCountResponse(Long unreadCount);

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "actorNickname", source = "actor.nickname")
    @Mapping(target = "actorProfileImageUrl",
            expression = "java(notification.getActor() == null ? null : userProfileImageUrlService.getProfileImageUrlFor(notification.getActor()))")
    @Mapping(target = "createdAt",
            expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDateTime(notification.getCreatedAt()))")
    @Mapping(target = "payload", expression = "java(notification.getPayload())")
    GetNotificationsResponse.Item toItem(
            Notification notification,
            @Context UserProfileImageUrlService userProfileImageUrlService
    );
}
