package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.UpdateCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.DeleteCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.UpdateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.any;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionCommandServiceTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BirdRepository birdRepository;
    @Mock
    private CollectionImageRepository collectionImageRepository;
    @Mock
    private ImageDomainService imageDomainService;
    @Mock
    private CollectionWebMapper collectionWebMapper;
    @Mock
    private ImageService imageService;

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
                imageService
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
        @DisplayName("정상 삭제 흐름")
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

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(collectionRepository.findById(collId)).willReturn(Optional.of(coll));
            given(collectionImageRepository.findObjectKeysByCollectionId(collId)).willReturn(List.of("k1", "k2"));

            // when
            service.deleteCollection(new DeleteCollectionCommand(userId, collId));

            // then
            then(imageService).should().deleteAll(List.of("k1", "k2"));
            then(collectionImageRepository).should().removeByCollectionId(collId);
            then(collectionRepository).should().remove(coll);
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

    @Nested
    @DisplayName("updateCollection 메서드 테스트")
    class UpdateCollectionTests {

        @Test
        @DisplayName("모든 필드 정상 수정")
        void updateCollection_success_all() {
            Long userId = 1L;
            Long collId = 2L;
            Long newBirdId = 5L;
            LocalDate newDate = LocalDate.of(2025, 8, 6);
            Double newLat = 12.3;
            Double newLon = 34.5;
            String newAlias = "newAlias";
            String newAddr = "newAddr";
            String newNote = "newNote";
            AccessLevelType newAccess = AccessLevelType.PRIVATE;

            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", userId);
            Bird oldBird = new Bird();
            ReflectionTestUtils.setField(oldBird, "id", 3L);
            UserBirdCollection coll = UserBirdCollection.builder()
                    .user(user).bird(oldBird).tempBirdName(null)
                    .discoveredDate(LocalDate.of(2025, 1, 1))
                    .location(org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory.create(0, 0))
                    .locationAlias("old").address("old").note("old")
                    .isPinned(false).accessLevel(AccessLevelType.PUBLIC)
                    .build();
            ReflectionTestUtils.setField(coll, "id", collId);

            UpdateCollectionCommand cmd = new UpdateCollectionCommand(
                    userId, collId, true, newBirdId,
                    newDate, newLat, newLon,
                    newAlias, newAddr, newNote, newAccess
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(collectionRepository.findById(collId)).willReturn(Optional.of(coll));
            Bird newBird = new Bird();
            ReflectionTestUtils.setField(newBird, "id", newBirdId);
            given(birdRepository.findById(newBirdId)).willReturn(Optional.of(newBird));
            given(collectionImageRepository.findObjectKeysByCollectionId(collId))
                    .willReturn(List.of("key1"));
            given(imageDomainService.toUploadImageUrl("key1")).willReturn("url1");
            UpdateCollectionResponse expected = new UpdateCollectionResponse(
                    collId, newBirdId, newDate,
                    newLon, newLat,
                    newAddr, newAlias,
                    newNote, "url1", newAccess
            );
            given(collectionWebMapper.toUpdateCollectionResponse(coll, "url1"))
                    .willReturn(expected);

            UpdateCollectionResponse response = service.updateCollection(cmd);

            assertThat(response).isSameAs(expected);
        }

        @Test
        @DisplayName("위도/경도 단독 수정 시 BadRequestException")
        void updateCollection_latLonXor_throws() {
            Long userId = 1L, collId = 2L;
            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", userId);
            UserBirdCollection coll = UserBirdCollection.builder()
                    .user(user).bird(null).tempBirdName(null)
                    .discoveredDate(LocalDate.now())
                    .location(org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory.create(0, 0))
                    .locationAlias(null).address(null).note(null)
                    .isPinned(false).accessLevel(AccessLevelType.PUBLIC)
                    .build();
            ReflectionTestUtils.setField(coll, "id", collId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(collectionRepository.findById(collId)).willReturn(Optional.of(coll));

            UpdateCollectionCommand cmd = new UpdateCollectionCommand(
                    userId, collId, false, null,
                    null, 10.0, null,
                    null, null, null, null
            );

            assertThatThrownBy(() -> service.updateCollection(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("위도와 경도 둘 중 하나만 수정할 수는 없어요");
        }

        @Test
        @DisplayName("메모 길이 초과 시 BadRequestException")
        void updateCollection_noteTooLong_throws() {
            Long userId = 1L, collId = 2L;
            User user = User.createUser("e@e");
            ReflectionTestUtils.setField(user, "id", userId);
            UserBirdCollection coll = UserBirdCollection.builder()
                    .user(user).bird(null).tempBirdName(null)
                    .discoveredDate(LocalDate.now())
                    .location(org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory.create(0, 0))
                    .locationAlias(null).address(null).note(null)
                    .isPinned(false).accessLevel(AccessLevelType.PUBLIC)
                    .build();
            ReflectionTestUtils.setField(coll, "id", collId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(collectionRepository.findById(collId)).willReturn(Optional.of(coll));

            String longNote = "b".repeat(UserBirdCollection.NOTE_MAX_LENGTH + 1);
            UpdateCollectionCommand cmd = new UpdateCollectionCommand(
                    userId, collId, false, null,
                    null, null, null,
                    null, null, longNote, null
            );

            assertThatThrownBy(() -> service.updateCollection(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("한 줄 평 길이는 " + UserBirdCollection.NOTE_MAX_LENGTH + "자 이하여야 해요");
        }
    }
}