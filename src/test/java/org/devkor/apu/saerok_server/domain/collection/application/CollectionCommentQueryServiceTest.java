package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentCountResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionCommentWebMapper;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CollectionCommentQueryServiceTest {

    private static final long COLL_ID = 10L;

    CollectionCommentQueryService sut;

    @Mock CollectionCommentRepository commentRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock CollectionCommentWebMapper mapper;

    private static UserBirdCollection collection(long id) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c, "id", id);
        return c;
    }

    @BeforeEach
    void init() { sut = new CollectionCommentQueryService(commentRepo, collectionRepo, mapper); }

    /* ------------------------------------------------------------------ */
    @Nested @DisplayName("댓글 목록 조회")
    class ReadList {

        @Test @DisplayName("성공")
        void success() {
            UserBirdCollection coll = collection(COLL_ID);
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(coll));

            List<UserBirdCollectionComment> empty = List.of();
            when(commentRepo.findByCollectionId(COLL_ID)).thenReturn(empty);

            GetCollectionCommentsResponse expected = new GetCollectionCommentsResponse(List.of());
            when(mapper.toGetCollectionCommentsResponse(empty)).thenReturn(expected);

            var res = sut.getComments(COLL_ID);

            assertThat(res).isSameAs(expected);
            verify(mapper).toGetCollectionCommentsResponse(empty);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void notFound() {
            when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.getComments(COLL_ID))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("댓글 수 조회 성공")
    void count_success() {
        when(collectionRepo.findById(COLL_ID)).thenReturn(Optional.of(collection(COLL_ID)));
        when(commentRepo.countByCollectionId(COLL_ID)).thenReturn(7L);

        GetCollectionCommentCountResponse res = sut.getCommentCount(COLL_ID);

        assertThat(res.count()).isEqualTo(7L);
    }
}
