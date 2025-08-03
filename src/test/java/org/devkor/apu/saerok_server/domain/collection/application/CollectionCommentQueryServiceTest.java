package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentCountResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionCommentWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CollectionCommentQueryServiceTest {

    private static final long COLL_ID = 10L;

    CollectionCommentQueryService sut;

    @Mock CollectionCommentRepository commentRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock CollectionCommentLikeRepository commentLikeRepo;
    @Mock CollectionCommentWebMapper mapper;
    @Mock UserProfileImageRepository userProfileImageRepo;
    @Mock ImageDomainService imageDomainService;

    private static UserBirdCollection collection(long id, Long userId) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c, "id", id);

        User user = new User();
        setField(user, "id", userId);
        setField(c, "user", user);

        return c;
    }

    @BeforeEach
    void init() { 
        sut = new CollectionCommentQueryService(
            commentRepo, collectionRepo, commentLikeRepo, mapper, 
            userProfileImageRepo, imageDomainService); 
    }

    /* ------------------------------------------------------------------ */
    @Nested @DisplayName("댓글 목록 조회")
    class ReadList {

        @Test @DisplayName("성공 - 비회원")
        void success_guest() {
            UserBirdCollection coll = collection(COLL_ID, 999L);
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            List<UserBirdCollectionComment> comments = List.of();
            when(commentRepo.findByCollectionId(COLL_ID)).thenReturn(comments);

            Map<Long, Long> likeCounts = Map.of();
            when(commentLikeRepo.countLikesByCommentIds(List.of())).thenReturn(likeCounts);

            Map<Long, String> profileImageUrls = Map.of();
            when(userProfileImageRepo.findObjectKeysByUserIds(List.of())).thenReturn(Map.of());

            GetCollectionCommentsResponse expected = new GetCollectionCommentsResponse(List.of(), false);
            when(mapper.toGetCollectionCommentsResponse(comments, likeCounts, Map.of(), Map.of(), profileImageUrls, false))
                    .thenReturn(expected);

            var res = sut.getComments(COLL_ID, null); // userId = null (비회원)

            assertThat(res).isSameAs(expected);
            verify(mapper).toGetCollectionCommentsResponse(comments, likeCounts, Map.of(), Map.of(), profileImageUrls, false);
        }
        
        @Test @DisplayName("성공 - 회원 (내 컬렉션 아님)")
        void success_loggedInUser_notMyCollection() {
            Long userId = 123L;
            UserBirdCollection coll = collection(COLL_ID, 999L);
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            List<UserBirdCollectionComment> comments = List.of();
            when(commentRepo.findByCollectionId(COLL_ID)).thenReturn(comments);

            Map<Long, Long> likeCounts = Map.of();
            when(commentLikeRepo.countLikesByCommentIds(List.of())).thenReturn(likeCounts);
            
            Map<Long, Boolean> likeStatuses = Map.of();
            when(commentLikeRepo.findLikeStatusByUserIdAndCommentIds(userId, List.of())).thenReturn(likeStatuses);

            Map<Long, String> profileImageUrls = Map.of();
            when(userProfileImageRepo.findObjectKeysByUserIds(List.of())).thenReturn(Map.of());

            GetCollectionCommentsResponse expected = new GetCollectionCommentsResponse(List.of(), false);
            when(mapper.toGetCollectionCommentsResponse(comments, likeCounts, likeStatuses, Map.of(), profileImageUrls, false))
                    .thenReturn(expected);

            var res = sut.getComments(COLL_ID, userId);

            assertThat(res).isSameAs(expected);
            verify(mapper).toGetCollectionCommentsResponse(comments, likeCounts, likeStatuses, Map.of(), profileImageUrls, false);
        }
        
        @Test @DisplayName("성공 - 회원 (내 컬렉션)")
        void success_loggedInUser_myCollection() {
            Long userId = 123L;
            UserBirdCollection coll = collection(COLL_ID, userId); // 내 컬렉션
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            List<UserBirdCollectionComment> comments = List.of();
            when(commentRepo.findByCollectionId(COLL_ID)).thenReturn(comments);

            Map<Long, Long> likeCounts = Map.of();
            when(commentLikeRepo.countLikesByCommentIds(List.of())).thenReturn(likeCounts);
            
            Map<Long, Boolean> likeStatuses = Map.of();
            when(commentLikeRepo.findLikeStatusByUserIdAndCommentIds(userId, List.of())).thenReturn(likeStatuses);

            Map<Long, String> profileImageUrls = Map.of();
            when(userProfileImageRepo.findObjectKeysByUserIds(List.of())).thenReturn(Map.of());

            GetCollectionCommentsResponse expected = new GetCollectionCommentsResponse(List.of(), true);
            when(mapper.toGetCollectionCommentsResponse(comments, likeCounts, likeStatuses, Map.of(), profileImageUrls, true))
                    .thenReturn(expected);

            var res = sut.getComments(COLL_ID, userId);

            assertThat(res).isSameAs(expected);
            verify(mapper).toGetCollectionCommentsResponse(comments, likeCounts, likeStatuses, Map.of(), profileImageUrls, true);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void notFound() {
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.getComments(COLL_ID, null))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("댓글 수 조회 성공")
    void count_success() {
        when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(collection(COLL_ID, 999L)));
        when(commentRepo.countByCollectionId(COLL_ID)).thenReturn(7L);

        GetCollectionCommentCountResponse res = sut.getCommentCount(COLL_ID);

        assertThat(res.count()).isEqualTo(7L);
    }
}
