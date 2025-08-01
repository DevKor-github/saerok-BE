package org.devkor.apu.saerok_server.domain.collection.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.core.entity.*;
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

    public SuggestBirdIdResponse suggest(Long userId, Long collectionId, Long birdId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (collection.getBird() != null)
            throw new BadRequestException("이미 bird_id가 확정된 컬렉션이에요");

        if (collection.getUser().getId().equals(userId))
            throw new BadRequestException("나 자신의 컬렉션에 동정 의견을 제안할 수 없어요");

        Bird bird = birdRepo.findById(birdId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));

        if (suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(
                userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.SUGGEST)) {
            throw new BadRequestException("이미 내가 제안한 항목이에요");
        }

        if (suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(
                userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.AGREE)) {
            throw new BadRequestException("이미 동의한 항목이에요");
        }

        boolean birdAlreadySuggested = suggestionRepo.existsByCollectionIdAndBirdIdAndType(
                collectionId, birdId, BirdIdSuggestion.SuggestionType.SUGGEST);

        Long suggestionId;

        if (birdAlreadySuggested) {
            // 이미 제안된 경우 -> 동의만 추가
            // 기존 비동의가 있다면 제거
            suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(
                userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.DISAGREE)
                .ifPresent(suggestionRepo::remove);

            // 동의 추가
            BirdIdSuggestion agree = new BirdIdSuggestion(user, collection, bird, BirdIdSuggestion.SuggestionType.AGREE);
            suggestionRepo.save(agree);
            suggestionId = agree.getId();
        } else {
            // 첫 제안인 경우 -> 제안과 동의를 모두 추가
            // 1. 제안 추가
            BirdIdSuggestion suggestion = new BirdIdSuggestion(user, collection, bird, BirdIdSuggestion.SuggestionType.SUGGEST);
            suggestionRepo.save(suggestion);
            suggestionId = suggestion.getId();
            
            // 2. 동의도 자동으로 추가 (제안자는 자동으로 동의한 것으로 처리)
            BirdIdSuggestion agree = new BirdIdSuggestion(user, collection, bird, BirdIdSuggestion.SuggestionType.AGREE);
            suggestionRepo.save(agree);
        }

        return new SuggestBirdIdResponse(suggestionId);
    }

    public ToggleStatusResponse toggleAgree(Long userId, Long collectionId, Long birdId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (collection.getBird() != null)
            throw new BadRequestException("이미 bird_id가 확정된 컬렉션이에요");

        if (collection.getUser().getId().equals(userId))
            throw new BadRequestException("나 자신의 컬렉션에 동의할 수 없어요");

        Bird bird = birdRepo.findById(birdId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));
        
        if (!suggestionRepo.existsByCollectionIdAndBirdIdAndType(
                collectionId, birdId, BirdIdSuggestion.SuggestionType.SUGGEST)) {
            throw new BadRequestException("제안되지 않은 조류에는 동의할 수 없어요");
        }

        boolean agreeExists = suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(
                userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.AGREE);

        if (agreeExists) {
            // 동의가 이미 존재하면 제거 (동의 -1)
            suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(
                    userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.AGREE)
                .ifPresent(suggestionRepo::remove);
        } else {
            // 기존 비동의가 있다면 제거 (비동의 -1)
            suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(
                    userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.DISAGREE)
                .ifPresent(suggestionRepo::remove);

            // 동의 추가 (동의 +1)
            BirdIdSuggestion agree = new BirdIdSuggestion(user, collection, bird, BirdIdSuggestion.SuggestionType.AGREE);
            suggestionRepo.save(agree);
        }

        // 토글 완료 후 현재 상태 조회
        Object[] status = suggestionRepo.findToggleStatusByCollectionIdAndBirdId(collectionId, birdId, userId);
        return new ToggleStatusResponse(
                (Long) status[0],    // agreeCount
                (Long) status[1],    // disagreeCount
                (Boolean) status[2], // isAgreedByMe
                (Boolean) status[3]  // isDisagreedByMe
        );
    }

    public ToggleStatusResponse toggleDisagree(Long userId, Long collectionId, Long birdId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (collection.getBird() != null)
            throw new BadRequestException("이미 bird_id가 확정된 컬렉션이에요");

        if (collection.getUser().getId().equals(userId))
            throw new BadRequestException("나 자신의 컬렉션에 비동의할 수 없어요");

        Bird bird = birdRepo.findById(birdId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 조류 id예요"));
        
        if (!suggestionRepo.existsByCollectionIdAndBirdIdAndType(
                collectionId, birdId, BirdIdSuggestion.SuggestionType.SUGGEST)) {
            throw new BadRequestException("제안되지 않은 조류에는 비동의할 수 없어요");
        }

        boolean disagreeExists = suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(
                userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.DISAGREE);

        if (disagreeExists) {
            // 비동의가 이미 존재하면 제거 (비동의 -1)
            suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(
                    userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.DISAGREE)
                .ifPresent(suggestionRepo::remove);
        } else {
            // 기존 동의가 있다면 제거 (동의 -1)
            suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(
                    userId, collectionId, birdId, BirdIdSuggestion.SuggestionType.AGREE)
                .ifPresent(suggestionRepo::remove);

            // 비동의 추가 (비동의 +1)
            BirdIdSuggestion disagree = new BirdIdSuggestion(user, collection, bird, BirdIdSuggestion.SuggestionType.DISAGREE);
            suggestionRepo.save(disagree);
        }

        // 토글 완료 후 현재 상태 조회
        Object[] status = suggestionRepo.findToggleStatusByCollectionIdAndBirdId(collectionId, birdId, userId);
        return new ToggleStatusResponse(
                (Long) status[0],    // agreeCount
                (Long) status[1],    // disagreeCount
                (Boolean) status[2], // isAgreedByMe
                (Boolean) status[3]  // isDisagreedByMe
        );
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

        collection.changeBird(bird);

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
