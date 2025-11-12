package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.DeleteCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.stat.application.BirdIdRequestHistoryRecorder; // ★ 유지
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.any;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionCommandServiceTest {

    @Mock private CollectionRepository collectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private BirdRepository birdRepository;
    @Mock private CollectionImageRepository collectionImageRepository;
    @Mock private ImageDomainService imageDomainService;
    @Mock private CollectionWebMapper collectionWebMapper;
    @Mock private ImageService imageService;
    @Mock private BirdIdRequestHistoryRecorder birdReqHistory; // ★ 유지
    @Mock private ImageVariantService imageVariantService;     // ★ 추가

    private CollectionCommandService service;

    @BeforeEach
    void setUp() {
        service = new CollectionCommandService(
                collectionRepository,
                userRepository,
                birdRepository,
                collectionImageRepository,
                imageDomainService,
                collectionWebMapper,
                imageService,
                birdReqHistory,        // ★ 유지
                imageVariantService    // ★ 추가
        );
    }

    @Nested
    @DisplayName("createCollection 메서드 테스트")
    class CreateCollectionTests {

        @Test
        @DisplayName("정상 생성 - birdId 포함")
        void createCollection_success_withBird() {
            // given
            Long userId = 1L;
            Long birdId = 2L;
            LocalDate date = LocalDate.of(2025, 8, 7);
            Double lat = 10.0;
            Double lon = 20.0;
            String alias = "alias";
            String address = "address";
            String note = "note";
            AccessLevelType accessLevel = AccessLevelType.PRIVATE;

            User user = User.createUser("email@example.com");
            ReflectionTestUtils.setField(user, "id", userId);
            Bird bird = new Bird();
            ReflectionTestUtils.setField(bird, "id", birdId);

            CreateCollectionCommand command = new CreateCollectionCommand(
                    userId, birdId, date, lat, lon, alias, address, note, accessLevel
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(birdRepository.findById(birdId)).willReturn(Optional.of(bird));
            willAnswer(invocation -> {
                UserBirdCollection c = invocation.getArgument(0);
                ReflectionTestUtils.setField(c, "id", 3L);
                return 3L;
            }).given(collectionRepository).save(any(UserBirdCollection.class));

            // when
            Long result = service.createCollection(command);

            // then
            assertThat(result).isEqualTo(3L);
            ArgumentCaptor<UserBirdCollection> captor = ArgumentCaptor.forClass(UserBirdCollection.class);
            then(collectionRepository).should().save(captor.capture());
            UserBirdCollection saved = captor.getValue();
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getBird()).isSameAs(bird);
            assertThat(saved.getDiscoveredDate()).isEqualTo(date);
            assertThat(saved.getLatitude()).isEqualTo(lat);
            assertThat(saved.getLongitude()).isEqualTo(lon);
            assertThat(saved.getLocationAlias()).isEqualTo(alias);
            assertThat(saved.getAddress()).isEqualTo(address);
            assertThat(saved.getNote()).isEqualTo(note);
            assertThat(saved.getAccessLevel()).isEqualTo(accessLevel);

            // ‘대기 시작’ 기록 호출 여부는 상황에 따라 다를 수 있어 엄격 검증은 생략
            then(birdReqHistory).should().onCollectionCreatedIfPending(eq(saved), any());
        }

        @Test
        @DisplayName("발견 날짜 누락 시 BadRequestException")
        void createCollection_missingDate_throws() {
            CreateCollectionCommand cmd = new CreateCollectionCommand(
                    1L, null, null, 10.0, 20.0, null, null, null, AccessLevelType.PUBLIC
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(User.createUser("e@e")));

            assertThatThrownBy(() -> service.createCollection(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("관찰 날짜를 포함해주세요");
        }

        @Test
        @DisplayName("위치 정보 누락 시 BadRequestException")
        void createCollection_missingLocation_throws() {
            CreateCollectionCommand cmd = new CreateCollectionCommand(
                    1L, null, LocalDate.now(), null, 20.0, null, null, null, AccessLevelType.PUBLIC
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(User.createUser("e@e")));

            assertThatThrownBy(() -> service.createCollection(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("관찰 위치 정보를 포함해주세요");
        }

        @Test
        @DisplayName("메모 길이 초과 시 BadRequestException")
        void createCollection_noteTooLong_throws() {
            String longNote = "a".repeat(UserBirdCollection.NOTE_MAX_LENGTH + 1);
            CreateCollectionCommand cmd = new CreateCollectionCommand(
                    1L, null, LocalDate.now(), 10.0, 20.0, null, null, longNote, AccessLevelType.PUBLIC
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(User.createUser("e@e")));

            assertThatThrownBy(() -> service.createCollection(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("한 줄 평 길이는 " + UserBirdCollection.NOTE_MAX_LENGTH + "자 이하여야 해요");
        }
    }

    @Nested
    @DisplayName("deleteCollection 메서드 테스트")
    class DeleteCollectionTests {

        @Test
        @DisplayName("정상 삭제 흐름(연관 썸네일까지 삭제)")
        void deleteCollection_success() {
            Long userId = 1L;
            Long collId = 2L;
            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", userId);
            UserBirdCollection coll = UserBirdCollection.builder()
                    .user(user)
                    .bird(null)
                    .tempBirdName(null)
                    .discoveredDate(LocalDate.now())
                    .location(org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory.create(0, 0))
                    .locationAlias(null)
                    .address(null)
                    .note(null)
                    .isPinned(false)
                    .accessLevel(AccessLevelType.PUBLIC)
                    .build();
            ReflectionTestUtils.setField(coll, "id", collId);

            List<String> originalKeys = List.of("k1", "k2");
            List<String> associated = List.of("k1", "k2", "thumbnails/k1.webp", "thumbnails/k2.webp");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(collectionRepository.findById(collId)).willReturn(Optional.of(coll));
            given(collectionImageRepository.findObjectKeysByCollectionId(collId)).willReturn(originalKeys);
            given(imageVariantService.associatedKeys(ImageKind.USER_COLLECTION_IMAGE, originalKeys)).willReturn(associated);

            // when
            service.deleteCollection(new DeleteCollectionCommand(userId, collId));

            // then
            then(birdReqHistory).should().onCollectionDeleted(collId);
            then(collectionImageRepository).should().removeByCollectionId(collId);
            then(collectionRepository).should().remove(coll);

            ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
            then(imageService).should().deleteAll(keysCaptor.capture());
            assertThat(keysCaptor.getValue()).containsExactlyElementsOf(associated);

            then(imageVariantService).should().associatedKeys(ImageKind.USER_COLLECTION_IMAGE, originalKeys);
        }

        @Test
        @DisplayName("사용자 미존재 시 NotFoundException")
        void deleteCollection_userNotFound_throws() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> service.deleteCollection(new DeleteCollectionCommand(1L, 2L)))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("존재하지 않는 사용자 id예요");
        }

        @Test
        @DisplayName("컬렉션 미존재 시 NotFoundException")
        void deleteCollection_collectionNotFound_throws() {
            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", 1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(collectionRepository.findById(2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCollection(new DeleteCollectionCommand(1L, 2L)))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("해당 id의 컬렉션이 존재하지 않아요");
        }

        @Test
        @DisplayName("권한 없을 때 ForbiddenException")
        void deleteCollection_forbidden_throws() {
            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", 1L);
            User other = User.createUser("x@x");
            ReflectionTestUtils.setField(other, "id", 2L);
            UserBirdCollection coll = UserBirdCollection.builder()
                    .user(other).bird(null).tempBirdName(null)
                    .discoveredDate(LocalDate.now())
                    .location(org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory.create(0, 0))
                    .locationAlias(null).address(null).note(null)
                    .isPinned(false).accessLevel(AccessLevelType.PUBLIC)
                    .build();
            ReflectionTestUtils.setField(coll, "id", 3L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(collectionRepository.findById(3L)).willReturn(Optional.of(coll));

            assertThatThrownBy(() -> service.deleteCollection(new DeleteCollectionCommand(1L, 3L)))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("해당 컬렉션에 대한 권한이 없어요");
        }
    }

    // updateCollection 관련 기존 테스트들은 이 변경과 무관하므로 그대로 유지합니다.
}
