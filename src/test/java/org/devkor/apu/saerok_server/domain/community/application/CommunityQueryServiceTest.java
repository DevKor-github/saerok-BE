package org.devkor.apu.saerok_server.domain.community.application;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityFreeBoardPostInfo;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityCollectionsResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityMainResponse;
import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.domain.community.core.repository.CommunityRepository;
import org.devkor.apu.saerok_server.domain.community.mapper.CommunityWebMapper;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostPreview;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostQueryService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    CommunityQueryService communityQueryService;

    @Mock CommunityRepository communityRepository;
    @Mock CommunityDataAssembler dataAssembler;
    @Mock CommunityWebMapper communityWebMapper;
    @Mock FreeBoardPostQueryService freeBoardPostQueryService;

    private static User user(Long id, String nickname) {
        User u = new User();
        setField(u, "id", id);
        u.setNickname(nickname);
        return u;
    }

    private static Bird bird(Long id, String koreanName) {
        Bird b = new Bird();
        setField(b, "id", id);
        BirdName name = new BirdName();
        name.setKoreanName(koreanName);
        setField(b, "name", name);
        return b;
    }

    private static UserBirdCollection collection(Long id, User user, Bird bird, String note) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c, "id", id);
        setField(c, "user", user);
        setField(c, "bird", bird);
        c.setNote(note);
        c.setAccessLevel(AccessLevelType.PUBLIC);
        return c;
    }

    private static CommunityCollectionInfo collectionInfo(
            Long collectionId,
            String imageUrl,
            String thumbnailImageUrl,
            String note,
            Long likeCount,
            Long commentCount,
            Boolean isPopular,
            Long suggestionUserCount,
            CommunityCollectionInfo.BirdInfo birdInfo,
            CommunityCollectionInfo.UserInfo userInfo
    ) {
        return new CommunityCollectionInfo(
                collectionId,
                imageUrl,
                thumbnailImageUrl,
                LocalDate.of(2024, 3, 15),
                LocalDateTime.of(2024, 3, 15, 10, 30, 0),
                37.5665,
                126.9780,
                null,
                null,
                note,
                likeCount,
                commentCount,
                false,
                isPopular,
                suggestionUserCount,
                birdInfo,
                userInfo
        );
    }

    @BeforeEach
    void setUp() {
        communityQueryService = new CommunityQueryService(
                communityRepository,
                dataAssembler,
                communityWebMapper,
                freeBoardPostQueryService
        );
    }

    @Test
    @DisplayName("페이지네이션에서 page나 size 중 하나만 null인 경우 유효하지 않음")
    void invalidPagination_isNotValid() {
        // Given
        CommunityQueryCommand pageOnlyCommand = new CommunityQueryCommand(1, null, "새");
        CommunityQueryCommand sizeOnlyCommand = new CommunityQueryCommand(null, 10, "새");
        
        // When & Then
        assertFalse(pageOnlyCommand.hasValidPagination());
        assertFalse(sizeOnlyCommand.hasValidPagination());
    }

    @Test
    @DisplayName("Repository에서 RuntimeException 발생 시 예외가 전파됨")
    void repositoryException_isPropagated() {
        // Given
        Long userId = 1L;
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        
        given(communityRepository.findRecentPublicCollections(org.mockito.ArgumentMatchers.any()))
                .willThrow(repositoryException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> communityQueryService.getCommunityMain(userId));
        
        assertEquals("Database connection failed", exception.getMessage());
        verifyNoInteractions(dataAssembler);
    }

    @Test
    @DisplayName("DataAssembler에서 예외 발생 시 예외가 전파됨")
    void dataAssemblerException_isPropagated() {
        // Given
        Long userId = 1L;
        
        given(communityRepository.findRecentPublicCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(communityRepository.findPopularCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(communityRepository.findPendingBirdIdCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        
        RuntimeException assemblerException = new RuntimeException("Data assembly failed");
        given(dataAssembler.toCollectionInfos(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .willThrow(assemblerException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> communityQueryService.getCommunityMain(userId));
        
        assertEquals("Data assembly failed", exception.getMessage());
    }

    @Test
    @DisplayName("최근 컬렉션 조회 시 동정 요청 컬렉션은 참여자 수가 있고, 일반 컬렉션은 null이다")
    void getRecentCollections_withMixedCollections_correctlySetsParticipantCount() {
        // Given
        Long userId = 1L;
        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, null);
        
        User u = user(1L, "테스트유저");
        Bird b = bird(100L, "까치");
        
        UserBirdCollection pendingCollection = collection(1L, u, null, "이게 무슨 새일까요?");
        UserBirdCollection normalCollection = collection(2L, u, b, "까치를 발견했어요!");
        
        List<UserBirdCollection> collections = List.of(pendingCollection, normalCollection);
        
        given(communityRepository.findRecentPublicCollections(command))
                .willReturn(collections);
        
        CommunityCollectionInfo.UserInfo userInfo = new CommunityCollectionInfo.UserInfo(
                1L, "테스트유저", "https://example.com/profile.jpg", "https://example.com/thumbnails/profile.webp"
        );
        
        CommunityCollectionInfo pendingInfo = collectionInfo(
                1L, "https://example.com/image1.jpg", "https://example.com/thumbnails/1", "이게 무슨 새일까요?",
                10L, 5L, false, 3L, null, userInfo
        );
        
        CommunityCollectionInfo.BirdInfo birdInfo = new CommunityCollectionInfo.BirdInfo(100L, "까치");
        CommunityCollectionInfo normalInfo = collectionInfo(
                2L, "https://example.com/image2.jpg", "https://example.com/thumbnails/2", "까치를 발견했어요!",
                15L, 7L, false, null, birdInfo, userInfo
        );
        
        given(dataAssembler.toCollectionInfos(collections, userId))
                .willReturn(List.of(pendingInfo, normalInfo));
        
        // When
        GetCommunityCollectionsResponse response = communityQueryService.getRecentCollections(userId, command);
        
        // Then
        assertThat(response.items()).hasSize(2);
        
        CommunityCollectionInfo firstItem = response.items().get(0);
        assertThat(firstItem.collectionId()).isEqualTo(1L);
        assertThat(firstItem.suggestionUserCount()).isEqualTo(3L);
        assertThat(firstItem.bird()).isNull();
        assertThat(firstItem.note()).isEqualTo("이게 무슨 새일까요?");
        
        CommunityCollectionInfo secondItem = response.items().get(1);
        assertThat(secondItem.collectionId()).isEqualTo(2L);
        assertThat(secondItem.suggestionUserCount()).isNull();
        assertThat(secondItem.bird()).isNotNull();
        assertThat(secondItem.bird().koreanName()).isEqualTo("까치");
        assertThat(secondItem.note()).isEqualTo("까치를 발견했어요!");
    }

    @Test
    @DisplayName("커뮤니티 메인 조회 시 자유게시판 최신 글 5건이 포함된다")
    void getCommunityMain_returnsRecentFreeBoardPosts() {
        // Given
        Long userId = 1L;

        given(communityRepository.findRecentPublicCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(communityRepository.findPopularCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(communityRepository.findPendingBirdIdCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(dataAssembler.toCollectionInfos(List.of(), userId))
                .willReturn(List.of());

        List<FreeBoardPostPreview> freeBoardPosts = List.of(
                new FreeBoardPostPreview(1L, 10L, "유저A", "https://img/a.jpg", "https://img/thumb/a.webp",
                        "오늘 한강에서 백로를 봤어요!", LocalDateTime.of(2025, 7, 5, 15, 0), LocalDateTime.of(2025, 7, 5, 15, 0)),
                new FreeBoardPostPreview(2L, 11L, "유저B", "https://img/b.jpg", "https://img/thumb/b.webp",
                        "참새 귀엽다", LocalDateTime.of(2025, 7, 5, 14, 30), LocalDateTime.of(2025, 7, 5, 14, 30)),
                new FreeBoardPostPreview(3L, 12L, "유저C", "https://img/c.jpg", "https://img/thumb/c.webp",
                        "까치 발견!", LocalDateTime.of(2025, 7, 5, 14, 0), LocalDateTime.of(2025, 7, 5, 14, 0)),
                new FreeBoardPostPreview(4L, 13L, "유저D", "https://img/d.jpg", "https://img/thumb/d.webp",
                        "비둘기가 많네요", LocalDateTime.of(2025, 7, 5, 13, 30), LocalDateTime.of(2025, 7, 5, 13, 30)),
                new FreeBoardPostPreview(5L, 14L, "유저E", "https://img/e.jpg", "https://img/thumb/e.webp",
                        "딱따구리 소리가 들려요", LocalDateTime.of(2025, 7, 5, 13, 0), LocalDateTime.of(2025, 7, 5, 13, 0))
        );
        given(freeBoardPostQueryService.getRecentPostsForMain(5))
                .willReturn(freeBoardPosts);

        for (FreeBoardPostPreview post : freeBoardPosts) {
            given(communityWebMapper.toCommunityFreeBoardPostInfo(post))
                    .willReturn(new CommunityFreeBoardPostInfo(
                            post.postId(), post.userId(), post.nickname(),
                            post.profileImageUrl(), post.thumbnailProfileImageUrl(),
                            post.content(), post.createdAt(), post.updatedAt()
                    ));
        }

        // When
        GetCommunityMainResponse response = communityQueryService.getCommunityMain(userId);

        // Then
        assertThat(response.recentFreeBoardPosts()).hasSize(5);
        assertThat(response.recentFreeBoardPosts().get(0).postId()).isEqualTo(1L);
        assertThat(response.recentFreeBoardPosts().get(4).postId()).isEqualTo(5L);
        assertThat(response.recentFreeBoardPosts().get(0)).isInstanceOf(CommunityFreeBoardPostInfo.class);
        then(freeBoardPostQueryService).should().getRecentPostsForMain(5);
    }
}
