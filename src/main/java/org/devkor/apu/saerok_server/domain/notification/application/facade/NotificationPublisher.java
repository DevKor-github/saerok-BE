package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationBatchService;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.store.InAppNotificationWriter;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushTarget;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchResult;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.BatchedNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationRenderer renderer;
    private final InAppNotificationWriter inAppWriter;
    private final NotificationRepository notificationRepository;
    private final PushGateway pushGateway;
    private final UserRepository userRepository;
    private final NotificationBatchService batchService;

    /**
     * <h2>모든 알림의 공통 파이프라인</h2>
     *
     * <ol>
     *   <li>렌더링</li>
     *   <li>인앱 저장</li>
     *   <li>배지 카운트 계산</li>
     *   <li>푸시 발송</li>
     * </ol>
     */
    @Transactional
    public void push(NotificationPayload payload) {
        // 시스템 알림은 즉시 전송
        if (payload instanceof SystemNotificationPayload) {
            sendNotification(payload);
            return;
        }

        if (payload instanceof ActionNotificationPayload actionPayload) {
            BatchResult result = batchService.addToBatch(actionPayload);

            if (result.shouldSendImmediately()) {
                sendNotification(payload);
            }
            // 배치에 추가되었으면 스케줄러가 나중에 전송
        }
    }

    /**
     * 배치 알림 전송 (스케줄러에서 호출).
     */
    @Transactional
    public void pushBatch(NotificationBatch batch) {
        try {
            BatchedNotificationPayload payload = BatchedNotificationPayload.fromBatch(batch);
            sendNotification(payload);

        } catch (Exception e) {
            log.error("Failed to send batch notification: key={}", batch.getKey(), e);
        } finally {
            // 성공/실패 여부와 관계없이 배치 삭제 (재시도 방지)
            batchService.deleteBatch(batch.getKey());
        }
    }

    private void sendNotification(NotificationPayload payload) {
        // recipient가 삭제/미존재면 조용히 무시
        if (userRepository.findById(payload.recipientId()).isEmpty()) {
            return;
        }

        RenderedMessage renderedMessage = renderer.render(payload);
        Long notificationId = inAppWriter.save(payload);

        int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                renderedMessage.pushTitle(),
                renderedMessage.pushBody(),
                payload.type().name(),
                payload.relatedId(),
                unread,
                notificationId
        );

        pushGateway.sendToUser(payload.recipientId(), payload.type(), cmd);
    }

    /**
     * 여러 사용자에게 알림을 발송하되, 푸시는 디바이스 기준으로 중복 제거합니다.
     */
    @Transactional
    public void pushDeduplicatedByDevice(Iterable<? extends NotificationPayload> payloads) {
        if (payloads == null) {
            return;
        }

        java.util.List<PushTarget> targets = new java.util.ArrayList<>();

        for (NotificationPayload payload : payloads) {
            if (payload == null) {
                continue;
            }

            if (userRepository.findById(payload.recipientId()).isEmpty()) {
                continue;
            }

            RenderedMessage renderedMessage = renderer.render(payload);
            Long notificationId = inAppWriter.save(payload);
            int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

            PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                    renderedMessage.pushTitle(),
                    renderedMessage.pushBody(),
                    payload.type().name(),
                    payload.relatedId(),
                    unread,
                    notificationId
            );

            targets.add(new PushTarget(payload.recipientId(), payload.type(), cmd));
        }

        if (targets.isEmpty()) {
            return;
        }

        pushGateway.sendToUsersDeduplicated(targets);
    }
}
