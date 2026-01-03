package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.UpdateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentCountResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.service.CommentContentResolver;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionCommentWebMapper;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CollectionCommentCommandServiceTest {

    private static final long OWNER_ID   = 1L;
    private static final long OTHER_ID   = 2L;
    private static final long COLL_ID    = 10L;
    private static final long COMMENT_ID = 100L;

    CollectionCommentCommandService sut;
    CollectionCommentQueryService   querySut;

    @Mock CollectionCommentRepository       commentRepo;
    @Mock CollectionRepository              collectionRepo;
    @Mock UserRepository                    userRepo;
    @Mock NotificationPublisher             publisher;

    @Mock CollectionCommentLikeRepository   commentLikeRepo;
    @Mock CollectionCommentWebMapper        collectionCommentWebMapper;
    @Mock UserProfileImageUrlService        userProfileImageUrlService;
    @Mock CommentContentResolver            commentContentResolver;

    private static User user(long id) {
        User u = new User();
        setField(u, "id", id);
        return u;
    }

    private static UserBirdCollection collection(long id, User owner) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c, "id", id);
        setField(c, "user", owner);
        return c;
    }

    private static UserBirdCollectionComment comment(long id, User u, UserBirdCollection c, String msg) {
        UserBirdCollectionComment cm = UserBirdCollectionComment.of(u, c, msg);
        setField(cm, "id", id);
        return cm;
    }

    @BeforeEach
    void init() {
        NotifyActionDsl notifyActionDsl = new NotifyActionDsl(
                publisher,
                (target, base) -> {
                    Map<String,Object> extras = base == null ? new HashMap<>() : new HashMap<>(base);
                    if (target.type() == TargetType.COLLECTION) {
                        extras.put("collectionId", target.id());
                        extras.put("collectionImageUrl", "dummy");
                    } else if (target.type() == TargetType.COMMENT) {
                        extras.put("commentId", target.id());
                        extras.put("collectionId", 999L); // dummy collection id
                        extras.put("collectionImageUrl", "dummy");
                    }
                    return extras;
                }
        );
        sut = new CollectionCommentCommandService(commentRepo, collectionRepo, userRepo, notifyActionDsl);

        querySut = new CollectionCommentQueryService(
                commentRepo, collectionRepo, commentLikeRepo, collectionCommentWebMapper, userProfileImageUrlService, commentContentResolver
        );
    }

    @Nested @DisplayName("댓글 작성")
    class Create {

        @Test @DisplayName("성공 (남의 컬렉션에 댓글 → 알림 발송)")
        void success() {
            User commenter = user(OWNER_ID);
            User collectionOwner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);

            setField(commenter, "nickname", "commenterNick");

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            doAnswer(inv -> { setField((Object) inv.getArgument(0), "id", COMMENT_ID); return null; })
                    .when(commentRepo).save(any());

            var res = sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("Nice", null));

            assertThat(res.commentId()).isEqualTo(COMMENT_ID);
            verify(commentRepo).save(any());

            ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(publisher).push(payloadCap.capture());

            ActionNotificationPayload p = (ActionNotificationPayload) payloadCap.getValue();
            assertThat(p.subject()).isEqualTo(NotificationSubject.COLLECTION);
            assertThat(p.action()).isEqualTo(NotificationAction.COMMENT);
            assertThat(p.recipientId()).isEqualTo(OTHER_ID);
            assertThat(p.actorId()).isEqualTo(OWNER_ID);
            Map<String, Object> extras = p.extras();
            assertThat(extras.get("collectionId")).isEqualTo(COLL_ID);
            assertThat(extras.get("comment")).isEqualTo("Nice");
            assertThat(extras).containsKey("collectionImageUrl");
        }

        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() ->
                    sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("x", null)))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("자신의 컬렉션에 댓글 작성 시 알림 없음")
        void ownCollectionComment_noPush() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            doAnswer(inv -> { setField((Object) inv.getArgument(0), "id", COMMENT_ID); return null; })
                    .when(commentRepo).save(any());

            var res = sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("Self comment", null));

            assertThat(res.commentId()).isEqualTo(COMMENT_ID);
            verify(commentRepo).save(any());
            verifyNoInteractions(publisher);
        }

        @Test @DisplayName("대댓글 작성 성공 - 원댓글 작성자와 컬렉션 소유자 모두 다른 경우 (2개 알림)")
        void createReply_success_twoNotifications() {
            long commenterId = 1L;
            long parentCommentOwnerId = 2L;
            long collectionOwnerId = 3L;
            long parentCommentId = 200L;
            long replyId = 300L;

            User commenter = user(commenterId);
            User parentCommentOwner = user(parentCommentOwnerId);
            User collectionOwner = user(collectionOwnerId);
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);
            UserBirdCollectionComment parentComment = comment(parentCommentId, parentCommentOwner, coll, "parent");

            setField(commenter, "nickname", "replier");

            when(userRepo.findById(commenterId)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));
            when(commentRepo.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

            doAnswer(inv -> { setField((Object) inv.getArgument(0), "id", replyId); return null; })
                    .when(commentRepo).save(any());

            var res = sut.createComment(commenterId, COLL_ID, new CreateCollectionCommentRequest("reply content", parentCommentId));

            assertThat(res.commentId()).isEqualTo(replyId);
            verify(commentRepo).save(any());

            ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(publisher, times(2)).push(payloadCap.capture());

            var notifications = payloadCap.getAllValues();

            // 첫 번째 알림: 원댓글 작성자에게 REPLY 알림
            ActionNotificationPayload replyNotif = (ActionNotificationPayload) notifications.get(0);
            assertThat(replyNotif.subject()).isEqualTo(NotificationSubject.COMMENT);
            assertThat(replyNotif.action()).isEqualTo(NotificationAction.REPLY);
            assertThat(replyNotif.recipientId()).isEqualTo(parentCommentOwnerId);
            assertThat(replyNotif.actorId()).isEqualTo(commenterId);

            // 두 번째 알림: 컬렉션 소유자에게 COMMENT 알림
            ActionNotificationPayload commentNotif = (ActionNotificationPayload) notifications.get(1);
            assertThat(commentNotif.subject()).isEqualTo(NotificationSubject.COLLECTION);
            assertThat(commentNotif.action()).isEqualTo(NotificationAction.COMMENT);
            assertThat(commentNotif.recipientId()).isEqualTo(collectionOwnerId);
            assertThat(commentNotif.actorId()).isEqualTo(commenterId);
        }

        @Test @DisplayName("대댓글 작성 성공 - 원댓글 작성자 = 컬렉션 소유자인 경우 (1개 알림)")
        void createReply_success_oneNotification() {
            long commenterId = 1L;
            long parentAndCollectionOwnerId = 2L;
            long parentCommentId = 200L;
            long replyId = 300L;

            User commenter = user(commenterId);
            User owner = user(parentAndCollectionOwnerId);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment parentComment = comment(parentCommentId, owner, coll, "parent");

            setField(commenter, "nickname", "replier");

            when(userRepo.findById(commenterId)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));
            when(commentRepo.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

            doAnswer(inv -> { setField((Object) inv.getArgument(0), "id", replyId); return null; })
                    .when(commentRepo).save(any());

            var res = sut.createComment(commenterId, COLL_ID, new CreateCollectionCommentRequest("reply", parentCommentId));

            assertThat(res.commentId()).isEqualTo(replyId);

            ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(publisher, times(1)).push(payloadCap.capture());

            // 원댓글 작성자에게만 REPLY 알림 (컬렉션 소유자와 동일인이므로 중복 제거됨)
            ActionNotificationPayload notif = (ActionNotificationPayload) payloadCap.getValue();
            assertThat(notif.subject()).isEqualTo(NotificationSubject.COMMENT);
            assertThat(notif.action()).isEqualTo(NotificationAction.REPLY);
            assertThat(notif.recipientId()).isEqualTo(parentAndCollectionOwnerId);
        }

        @Test @DisplayName("삭제된 댓글에 대댓글 작성 → ForbiddenException")
        void createReply_deletedParent_forbidden() {
            long parentCommentId = 200L;
            User commenter = user(OWNER_ID);
            User owner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment parentComment = comment(parentCommentId, owner, coll, "deleted");
            parentComment.softDelete();

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));
            when(commentRepo.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

            assertThatThrownBy(() ->
                    sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("reply", parentCommentId)))
                    .isExactlyInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("삭제된 댓글");
        }

        @Test @DisplayName("대댓글에 대댓글 작성 (depth 제한) → ForbiddenException")
        void createReply_depthLimit_forbidden() {
            long rootCommentId = 100L;
            long replyCommentId = 200L;

            User commenter = user(OWNER_ID);
            User owner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);

            UserBirdCollectionComment rootComment = comment(rootCommentId, owner, coll, "root");
            UserBirdCollectionComment replyComment = UserBirdCollectionComment.of(owner, coll, "reply", rootComment);
            setField(replyComment, "id", replyCommentId);

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));
            when(commentRepo.findById(replyCommentId)).thenReturn(Optional.of(replyComment));

            assertThatThrownBy(() ->
                    sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("nested reply", replyCommentId)))
                    .isExactlyInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("대댓글");
        }

        @Test @DisplayName("다른 컬렉션의 댓글에 대댓글 작성 → NotFoundException")
        void createReply_differentCollection_notFound() {
            long parentCommentId = 200L;
            long otherCollectionId = 999L;

            User commenter = user(OWNER_ID);
            User owner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollection otherColl = collection(otherCollectionId, owner);
            UserBirdCollectionComment parentComment = comment(parentCommentId, owner, otherColl, "parent");

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));
            when(commentRepo.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

            assertThatThrownBy(() ->
                    sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("reply", parentCommentId)))
                    .isExactlyInstanceOf(NotFoundException.class)
                    .hasMessageContaining("컬렉션");
        }
    }

    @Nested @DisplayName("댓글 수정")
    class Update {
        @Test @DisplayName("본인 댓글 수정 성공")
        void success() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, owner, coll, "old");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            var res = sut.updateComment(OWNER_ID, COLL_ID, COMMENT_ID,
                    new UpdateCollectionCommentRequest("new"));

            assertThat(res.content()).isEqualTo("new");
        }

        @Test @DisplayName("남의 댓글 수정 → ForbiddenException")
        void forbidden() {
            User other = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, other);
            UserBirdCollectionComment cm = comment(COMMENT_ID, other, coll, "hack");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            assertThatThrownBy(() ->
                    sut.updateComment(OWNER_ID, COLL_ID, COMMENT_ID,
                            new UpdateCollectionCommentRequest("x")))
                    .isExactlyInstanceOf(ForbiddenException.class);
        }
    }

    @Nested @DisplayName("댓글 삭제")
    class Delete {
        @Test @DisplayName("대댓글 없는 댓글 삭제 → hard delete")
        void delete_noReplies_hardDelete() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, owner, coll, "bye");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));
            when(commentRepo.hasReplies(COMMENT_ID)).thenReturn(false);

            sut.deleteComment(OWNER_ID, COLL_ID, COMMENT_ID);

            verify(commentRepo).remove(cm);
            assertThat(cm.getStatus()).isNotEqualTo(org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus.DELETED);
        }

        @Test @DisplayName("대댓글 있는 댓글 삭제 → soft delete")
        void delete_hasReplies_softDelete() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, owner, coll, "parent with replies");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));
            when(commentRepo.hasReplies(COMMENT_ID)).thenReturn(true);

            sut.deleteComment(OWNER_ID, COLL_ID, COMMENT_ID);

            verify(commentRepo, never()).remove(any());
            assertThat(cm.getStatus()).isEqualTo(org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus.DELETED);
        }

        @Test @DisplayName("컬렉션 소유자가 남의 댓글 삭제 성공")
        void delete_byCollectionOwner() {
            User collectionOwner = user(OWNER_ID);
            User commentOwner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, commentOwner, coll, "others comment");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));
            when(commentRepo.hasReplies(COMMENT_ID)).thenReturn(false);

            sut.deleteComment(OWNER_ID, COLL_ID, COMMENT_ID);

            verify(commentRepo).remove(cm);
        }

        @Test @DisplayName("댓글 작성자도 컬렉션 소유자도 아닌 경우 → ForbiddenException")
        void delete_forbidden() {
            long thirdPartyId = 999L;
            User collectionOwner = user(OWNER_ID);
            User commentOwner = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, commentOwner, coll, "not mine");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            assertThatThrownBy(() ->
                    sut.deleteComment(thirdPartyId, COLL_ID, COMMENT_ID))
                    .isExactlyInstanceOf(ForbiddenException.class);
        }
    }

    @Nested @DisplayName("댓글 조회")
    class Query {
        @Test @DisplayName("댓글 수 조회 성공")
        void count_success() {
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(collection(COLL_ID, user(999L))));
            when(commentRepo.countByCollectionId(COLL_ID)).thenReturn(7L);

            GetCollectionCommentCountResponse res = querySut.getCommentCount(COLL_ID);
            assertThat(res.count()).isEqualTo(7L);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void notFound() {
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> querySut.getComments(COLL_ID, null))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }
}
