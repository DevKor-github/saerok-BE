package org.devkor.apu.saerok_server.domain.collection.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.UpdateCollectionResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.DeleteCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.UpdateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.stat.application.BirdIdRequestHistoryRecorder;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionCommandService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final BirdRepository birdRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final ImageDomainService imageDomainService;
    private final CollectionWebMapper collectionWebMapper;
    private final ImageService imageService;
    private final BirdIdRequestHistoryRecorder birdReqHistory;

    public Long createCollection(CreateCollectionCommand command) {
        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Bird bird = (command.birdId() != null)
                ? birdRepository.findById(command.birdId()).orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"))
                : null;

        if (command.discoveredDate() == null) throw new BadRequestException("관찰 날짜를 포함해주세요");
        if (command.longitude() == null || command.latitude() == null) throw new BadRequestException("관찰 위치 정보를 포함해주세요");
        if (command.note() != null && command.note().length() > UserBirdCollection.NOTE_MAX_LENGTH)
            throw new BadRequestException("한 줄 평 길이는 " + UserBirdCollection.NOTE_MAX_LENGTH + "자 이하여야 해요");

        Point location = PointFactory.create(command.latitude(), command.longitude());

        UserBirdCollection collection = UserBirdCollection.builder()
                .user(user)
                .bird(bird)
                .tempBirdName(null) // 일단 tempBirdName 미사용하기로 하여 null로 설정
                .discoveredDate(command.discoveredDate())
                .location(location)
                .locationAlias(command.locationAlias())
                .address(command.address())
                .note(command.note())
                .accessLevel(command.accessLevel())
                .build();

        Long id = collectionRepository.save(collection);

        // 생성 직후 bird가 비어 있으면 '대기 시작' 기록
        birdReqHistory.onCollectionCreatedIfPending(collection, collection.getCreatedAt());

        return id;
    }

    public void deleteCollection(DeleteCollectionCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        // 1) 열린 동정 요청 히스토리 사전 정리
        birdReqHistory.onCollectionDeleted(collection.getId());

        // 2) 연관 이미지 정리 후 컬렉션 삭제
        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(command.collectionId());
        collectionImageRepository.removeByCollectionId(command.collectionId());
        collectionRepository.remove(collection);
        runAfterCommitOrNow(() -> imageService.deleteAll(objectKeys));
        // 3) 닫힌 히스토리는 FK가 SET NULL로 남는다 (DB가 처리)
    }

    public UpdateCollectionResponse updateCollection(UpdateCollectionCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        if (command.isBirdIdUpdated() != null && command.isBirdIdUpdated()) {
            Bird before = collection.getBird();
            Bird after;

            if (command.birdId() != null) {
                after = birdRepository.findById(command.birdId()).orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));
            } else {
                after = null;
            }

            // null->not null : '수정(EDIT)' 경로 → 열린 history 삭제(해결로 간주하지 않음)
            if (before == null && after != null) {
                birdReqHistory.onResolvedByEdit(collection);
            } else if (before != null && after == null) {
                // not null->null : 다시 대기 시작
                birdReqHistory.onBirdSetToUnknown(collection, OffsetDateTime.now());
            }

            collection.changeBird(after);
        }

        if (command.discoveredDate() != null) collection.setDiscoveredDate(command.discoveredDate());

        if ((command.latitude() == null) ^ (command.longitude() == null)) {
            throw new BadRequestException("위도와 경도 둘 중 하나만 수정할 수는 없어요");
        } else if (command.latitude() != null) {
            var location = PointFactory.create(command.latitude(), command.longitude());
            collection.setLocation(location);
        }

        if (command.locationAlias() != null) collection.setLocationAlias(command.locationAlias());
        if (command.address() != null) collection.setAddress(command.address());

        if (command.note() != null) {
            if (command.note().length() > UserBirdCollection.NOTE_MAX_LENGTH) {
                throw new BadRequestException("한 줄 평 길이는 " + UserBirdCollection.NOTE_MAX_LENGTH + "자 이하여야 해요");
            }
            collection.setNote(command.note());
        }

        if (command.accessLevel() != null) {
            collection.setAccessLevel(command.accessLevel());
        }

        String imageUrl = collectionImageRepository.findObjectKeysByCollectionId(command.collectionId()).stream()
                .map(imageDomainService::toUploadImageUrl)
                .findFirst()
                .orElse(null);

        return collectionWebMapper.toUpdateCollectionResponse(collection, imageUrl);
    }
}
