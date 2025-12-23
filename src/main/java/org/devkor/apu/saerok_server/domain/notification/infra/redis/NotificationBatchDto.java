package org.devkor.apu.saerok_server.domain.notification.infra.redis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchActor;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchKey;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Redis 직렬화를 위한 NotificationBatch DTO.
 */
@Getter
public class NotificationBatchDto {

    // Getters for Jackson
    private final Long recipientId;
    private final String subject;
    private final String action;
    private final Long relatedId;
    private final List<ActorDto> actors;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final Map<String, Object> extras;

    @JsonCreator
    public NotificationBatchDto(
            @JsonProperty("recipientId") Long recipientId,
            @JsonProperty("subject") String subject,
            @JsonProperty("action") String action,
            @JsonProperty("relatedId") Long relatedId,
            @JsonProperty("actors") List<ActorDto> actors,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("extras") Map<String, Object> extras
    ) {
        this.recipientId = recipientId;
        this.subject = subject;
        this.action = action;
        this.relatedId = relatedId;
        this.actors = actors;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.extras = extras;
    }

    public static NotificationBatchDto fromBatch(NotificationBatch batch) {
        List<ActorDto> actorDtos = batch.getActors().stream()
                .map(actor -> new ActorDto(actor.id(), actor.name()))
                .toList();

        return new NotificationBatchDto(
                batch.getRecipientId(),
                batch.getSubject().name(),
                batch.getAction().name(),
                batch.getRelatedId(),
                actorDtos,
                batch.getCreatedAt(),
                batch.getExpiresAt(),
                batch.getExtras()
        );
    }

    public NotificationBatch toBatch() {
        BatchKey key = new BatchKey(
                recipientId,
                NotificationSubject.valueOf(subject),
                NotificationAction.valueOf(action),
                relatedId
        );

        List<BatchActor> batchActors = actors.stream()
                .map(dto -> BatchActor.of(dto.id, dto.name))
                .toList();

        return new NotificationBatch(
                key,
                batchActors,
                createdAt,
                expiresAt,
                extras == null ? Map.of() : extras
        );
    }

    public static class ActorDto {
        private final Long id;
        private final String name;

        @JsonCreator
        public ActorDto(
                @JsonProperty("id") Long id,
                @JsonProperty("name") String name
        ) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
