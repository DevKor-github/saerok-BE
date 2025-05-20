package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.MyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.MyCollectionDto;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionQueryService {
    
    private final CollectionRepository collectionRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final CollectionWebMapper collectionWebMapper;
    
    public List<MyCollectionsResponse> getMyCollectionsResponse(Long userId) {
        List<UserBirdCollection> collections = collectionRepository.findByUserId(userId);
        List<MyCollectionDto> result = new ArrayList<>();
        
        for (UserBirdCollection collection : collections) {
            List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collection.getId());
            String imageUrl = objectKeys.isEmpty() ? null : objectKeys.getFirst();
            String birdName = (collection.getBird() != null) ? collection.getBird().getName().getKoreanName() : "어디선가 본 새";

            result.add(MyCollectionDto.builder()
                    .collectionId(collection.getId())
                    .imageUrl(imageUrl)
                    .birdName(birdName)
                    .build());
        }
        
        return collectionWebMapper.toMyCollectionsResponse(result);
    }
}
