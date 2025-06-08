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
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.util.CloudFrontUrlService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionCommandService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final BirdRepository birdRepository;
    private final S3Client s3Client;
    private final CollectionImageRepository collectionImageRepository;
    private final CloudFrontUrlService cloudFrontUrlService;
    private final CollectionWebMapper collectionWebMapper;
    private final PointFactory pointFactory;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public Long createCollection(CreateCollectionCommand command) {
        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        Bird bird;

        if (command.birdId() != null) {
            bird = birdRepository.findById(command.birdId()).orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));
        } else {
            bird = null;
        }

        if (command.discoveredDate() == null) {
            throw new BadRequestException("관찰 날짜를 포함해주세요");
        }

        if (command.longitude() == null || command.latitude() == null) {
            throw new BadRequestException("관찰 위치 정보를 포함해주세요");
        }

        if (command.note() != null && command.note().length() > UserBirdCollection.NOTE_MAX_LENGTH) {
            throw new BadRequestException("한 줄 평 길이는 " + UserBirdCollection.NOTE_MAX_LENGTH + "자 이하여야 해요");
        }

        Point location = pointFactory.create(command.latitude(), command.longitude());

        UserBirdCollection collection = UserBirdCollection.builder()
                .user(user)
                .bird(bird)
                .tempBirdName(null) // 일단 tempBirdName 미사용하기로 하여 null로 설정
                .discoveredDate(command.discoveredDate())
                .location(location)
                .locationAlias(command.locationAlias())
                .address(command.address())
                .note(command.note())
                .build();

        return collectionRepository.save(collection);
    }

    public void deleteCollection(DeleteCollectionCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(command.collectionId());

        if (!objectKeys.isEmpty()) {
            List<ObjectIdentifier> objectsToDelete = objectKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();
            s3Client.deleteObjects(deleteRequest);

            // TODO: 추후 운영/고도화 단계에서 S3 이미지 삭제 실패 감지 및 후처리
        }

        collectionImageRepository.removeByCollectionId(command.collectionId());
        collectionRepository.remove(collection);
    }

    public UpdateCollectionResponse updateCollection(UpdateCollectionCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        System.out.println(command.isBirdIdUpdated());
        if (command.isBirdIdUpdated() != null && command.isBirdIdUpdated()) {
            if (command.birdId() != null) {
                Bird bird = birdRepository.findById(command.birdId()).orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));
                collection.setBird(bird);
            } else {
                collection.setBird(null);
            }
        }

        if (command.discoveredDate() != null) collection.setDiscoveredDate(command.discoveredDate());

        if ((command.latitude() == null) ^ (command.longitude() == null)) {
            throw new BadRequestException("위도와 경도 둘 중 하나만 수정할 수는 없어요");
        } else if (command.latitude() != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Coordinate coordinate = new Coordinate(command.longitude(), command.latitude());
            Point location = geometryFactory.createPoint(coordinate);
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

        List<String> urls = collectionImageRepository.findObjectKeysByCollectionId(command.collectionId()).stream()
                .map(cloudFrontUrlService::toImageUrl)
                .toList();

        return collectionWebMapper.toUpdateCollectionResponse(collection, urls);
    }
}
