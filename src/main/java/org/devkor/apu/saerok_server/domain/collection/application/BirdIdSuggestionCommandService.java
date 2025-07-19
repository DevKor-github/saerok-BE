package org.devkor.apu.saerok_server.domain.collection.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BirdIdSuggestionCommandService {

    private final BirdIdSuggestionRepository suggestionRepo;
    private final CollectionRepository       collectionRepo;
    private final BirdRepository             birdRepo;
    private final UserRepository             userRepo;

    /* 동정 의견 제안/동의 */
    public SuggestOrAgreeResponse suggestOrAgree(Long userId, Long collectionId, Long birdId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (collection.getBird() != null)
            throw new BadRequestException("이미 bird_id가 확정된 컬렉션이에요");

        if (collection.getUser().getId().equals(userId))
            throw new BadRequestException("나 자신의 컬렉션에 동정 의견을 제안/동의할 수 없어요");

        Bird bird = birdRepo.findById(birdId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));

        if (suggestionRepo.existsByUserIdAndCollectionIdAndBirdId(userId, collectionId, birdId))
            throw new BadRequestException("이미 동의(또는 제안)한 항목이에요");

        BirdIdSuggestion suggestion = new BirdIdSuggestion(
                user,
                collection,
                bird
        );
        suggestionRepo.save(suggestion);

        return new SuggestOrAgreeResponse(suggestion.getId());
    }

    /* 동의 취소 */
    public void cancelAgree(Long userId, Long collectionId, Long birdId) {
        BirdIdSuggestion suggestion = suggestionRepo
                .findByUserIdAndCollectionIdAndBirdId(userId, collectionId, birdId)
                .orElseThrow(() -> new NotFoundException("동의/제안 기록이 없어요"));

        suggestionRepo.remove(suggestion);
    }

    /* 채택 */
    public AdoptSuggestionResponse adopt(Long userId, Long collectionId, Long birdId) {

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (!collection.getUser().getId().equals(userId))
            throw new ForbiddenException("컬렉션 작성자만 채택할 수 있어요");

        if (collection.getBird() != null)
            throw new BadRequestException("이미 bird_id가 확정된 컬렉션이에요");

        Bird bird = birdRepo.findById(birdId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));

        collection.setBird(bird);

        // NOTE: 컬렉션에 달린 동정 의견들은 삭제되지 않음

        return new AdoptSuggestionResponse(collectionId, birdId, bird.getName().getKoreanName());
    }

    /* 모든 의견 삭제 */
    public void deleteAll(Long userId, Long collectionId) {

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (!collection.getUser().getId().equals(userId))
            throw new ForbiddenException("컬렉션 작성자만 삭제할 수 있어요");

        suggestionRepo.deleteByCollectionId(collectionId);
    }
}
