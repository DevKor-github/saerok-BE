package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.UpdateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.PushNotificationService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

    @Mock CollectionCommentRepository commentRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock UserRepository             userRepo;
    @Mock PushNotificationService    pushNotificationService;

    /* ---------- 엔티티 헬퍼 ---------- */
    private static User user(long id) {
        User u = new User();
        setField(u, "id", id);        // (Object) 캐스팅으로 Field-버전 선택 방지
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
    void init() { sut = new CollectionCommentCommandService(commentRepo, collectionRepo, userRepo, pushNotificationService); }

    /* ------------------------------------------------------------------ */
    @Nested @DisplayName("댓글 작성")
    class Create {

        @Test @DisplayName("성공")
        void success() {
            User commenter = user(OWNER_ID);
            User collectionOwner = user(OTHER_ID); // 다른 사용자의 컬렉션에 댓글 작성
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);

            // 닉네임 설정
            setField(commenter, "nickname", "commenterNick");

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(commenter));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            // save 호출 시 id 주입
            doAnswer(inv -> {
                setField((Object) inv.getArgument(0), "id", COMMENT_ID);
                return null;
            }).when(commentRepo).save(any());

            var res = sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("Nice"));

            assertThat(res.commentId()).isEqualTo(COMMENT_ID);
            verify(commentRepo).save(any());
            // 푸시 알림 호출 검증 (댓글 작성자와 컬렉션 소유자가 다른 경우)
            verify(pushNotificationService).sendCollectionCommentNotification(OTHER_ID, OWNER_ID, COLL_ID, "Nice");
        }

        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("x")))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("자신의 컬렉션에 댓글 작성 시 푸시 알림 없음")
        void ownCollectionComment_noPushNotification() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);

            when(userRepo.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            doAnswer(inv -> {
                setField((Object) inv.getArgument(0), "id", COMMENT_ID);
                return null;
            }).when(commentRepo).save(any());

            var res = sut.createComment(OWNER_ID, COLL_ID, new CreateCollectionCommentRequest("Self comment"));

            assertThat(res.commentId()).isEqualTo(COMMENT_ID);
            verify(commentRepo).save(any());
            // 자신의 컬렉션에 댓글 작성 시에는 푸시 알림 없음
            verifyNoInteractions(pushNotificationService);
        }
    }

    /* ------------------------------------------------------------------ */
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

    /* ------------------------------------------------------------------ */
    @Nested @DisplayName("댓글 삭제")
    class Delete {

        @Test @DisplayName("본인 댓글 삭제 성공")
        void successByCommentOwner() {
            User owner = user(OWNER_ID);
            UserBirdCollection coll = collection(COLL_ID, owner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, owner, coll, "bye");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            sut.deleteComment(OWNER_ID, COLL_ID, COMMENT_ID);

            verify(commentRepo).remove(cm);
        }
        
        @Test @DisplayName("컬렉션 소유자가 남의 댓글 삭제 성공")
        void successByCollectionOwner() {
            User collectionOwner = user(OWNER_ID);
            User commenter = user(OTHER_ID);
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, commenter, coll, "comment by other");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            sut.deleteComment(OWNER_ID, COLL_ID, COMMENT_ID);

            verify(commentRepo).remove(cm);
        }

        @Test @DisplayName("권한 없는 사용자가 댓글 삭제 → ForbiddenException")
        void forbidden() {
            User collectionOwner = user(OWNER_ID);
            User commenter = user(OTHER_ID);
            User unauthorizedUser = user(3L);  // 다른 사용자
            UserBirdCollection coll = collection(COLL_ID, collectionOwner);
            UserBirdCollectionComment cm = comment(COMMENT_ID, commenter, coll, "bye");

            when(commentRepo.findById(COMMENT_ID)).thenReturn(Optional.of(cm));

            assertThatThrownBy(() ->
                    sut.deleteComment(unauthorizedUser.getId(), COLL_ID, COMMENT_ID))
                    .isExactlyInstanceOf(ForbiddenException.class);
        }
    }
}
