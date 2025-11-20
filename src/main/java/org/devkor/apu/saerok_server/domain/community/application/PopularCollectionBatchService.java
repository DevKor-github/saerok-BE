package org.devkor.apu.saerok_server.domain.community.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.community.core.entity.PopularCollection;
import org.devkor.apu.saerok_server.domain.community.core.repository.PopularCollectionRepository;
import org.devkor.apu.saerok_server.domain.community.core.repository.TrendingCollectionRepository;
import org.devkor.apu.saerok_server.domain.community.core.repository.dto.TrendingCollectionCandidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PopularCollectionBatchService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final double POPULARITY_LIKE_WEIGHT = 1.0;
    private static final double POPULARITY_COMMENT_WEIGHT = 1.2;
    private static final double FRESHNESS_LIKE_RATIO = 0.7;
    private static final double FRESHNESS_COMMENT_RATIO = 0.3;
    private static final double LAST_ACTIVITY_WINDOW_DAYS = 7.0;
    private static final double HALF_LIFE_DAYS = 3.0;
    private static final int CANDIDATE_DAYS = 120;
    private static final int POPULAR_LIMIT = 5;

    private final PopularCollectionRepository popularCollectionRepository;
    private final TrendingCollectionRepository trendingCollectionRepository;
    private final jakarta.persistence.EntityManager em;

    public void refreshPopularCollections() {
        OffsetDateTime now = OffsetDateTime.now(KST);
        log.info("[popular] refreshing popular collections at {}", now);

        List<TrendingCollectionCandidate> candidates = trendingCollectionRepository
                .findRecentPublicCollections(now.minusDays(CANDIDATE_DAYS));
        if (candidates.isEmpty()) {
            popularCollectionRepository.deleteAll();
            log.info("[popular] no candidates found; table truncated");
            return;
        }

        List<Long> candidateIds = candidates.stream()
                .map(TrendingCollectionCandidate::collectionId)
                .toList();

        Map<Long, List<OffsetDateTime>> likeCreatedAts = trendingCollectionRepository
                .findLikeCreatedAtByCollectionIds(candidateIds);
        Map<Long, Map<Long, OffsetDateTime>> lastCommentAtByUser = trendingCollectionRepository
                .findLastCommentAtByCollectionIds(candidateIds);

        List<PopularCandidateSnapshot> ranked = candidates.stream()
                .map(candidate -> buildSnapshot(candidate, now, likeCreatedAts, lastCommentAtByUser))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(PopularCandidateSnapshot::trendingScore).reversed()
                        .thenComparing(snapshot -> snapshot.collection().getId()))
                .limit(POPULAR_LIMIT)
                .toList();

        List<PopularCandidateSnapshot> shuffled = new ArrayList<>(ranked);
        Collections.shuffle(shuffled);

        List<PopularCollection> rankedAndShuffled = IntStream.range(0, shuffled.size())
                .mapToObj(idx -> toPopularCollection(shuffled.get(idx), now, idx))
                .toList();

        popularCollectionRepository.deleteAll();
        popularCollectionRepository.saveAll(rankedAndShuffled);

        log.info("[popular] refreshed {} rows", rankedAndShuffled.size());
    }

    private PopularCandidateSnapshot buildSnapshot(
            TrendingCollectionCandidate candidate,
            OffsetDateTime now,
            Map<Long, List<OffsetDateTime>> likeCreatedAts,
            Map<Long, Map<Long, OffsetDateTime>> lastCommentAtByUser
    ) {
        List<OffsetDateTime> likes = likeCreatedAts.getOrDefault(candidate.collectionId(), List.of());
        Map<Long, OffsetDateTime> commentsByUser = lastCommentAtByUser.getOrDefault(candidate.collectionId(), Map.of());

        long likeCount = likes.size();
        long commentUserCount = commentsByUser.size();
        if (likeCount == 0 && commentUserCount == 0) {
            return null;
        }

        double popularityScore = POPULARITY_LIKE_WEIGHT * Math.log(1 + likeCount)
                + POPULARITY_COMMENT_WEIGHT * Math.log(1 + commentUserCount);

        double decayedLikes = likes.stream()
                .mapToDouble(ts -> decay(ts, now))
                .sum();
        double decayedCommentUsers = commentsByUser.values().stream()
                .mapToDouble(ts -> decay(ts, now))
                .sum();

        double freshnessCore = FRESHNESS_LIKE_RATIO * decayedLikes
                + FRESHNESS_COMMENT_RATIO * decayedCommentUsers;

        OffsetDateTime lastCommentAt = commentsByUser.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
        double lastActivityBonus = 0.0;
        if (lastCommentAt != null) {
            double ageDays = daysBetween(lastCommentAt, now);
            lastActivityBonus = Math.max(0.0, 1 - ageDays / LAST_ACTIVITY_WINDOW_DAYS);
        }

        double freshnessScore = freshnessCore * (0.7 + 0.3 * lastActivityBonus);
        double trendingScore = popularityScore * freshnessScore;
        if (trendingScore <= 0) {
            return null;
        }

        UserBirdCollection ref = em.getReference(UserBirdCollection.class, candidate.collectionId());
        return new PopularCandidateSnapshot(ref, popularityScore, freshnessScore, trendingScore);
    }

    private double decay(OffsetDateTime eventTime, OffsetDateTime now) {
        double ageDays = daysBetween(eventTime, now);
        return Math.pow(0.5, ageDays / HALF_LIFE_DAYS);
    }

    private double daysBetween(OffsetDateTime earlier, OffsetDateTime later) {
        double seconds = Duration.between(earlier, later).getSeconds();
        double days = seconds / 86_400d;
        return Math.max(0d, days);
    }

    private PopularCollection toPopularCollection(PopularCandidateSnapshot snapshot, OffsetDateTime calculatedAt, int displayOrder) {
        return new PopularCollection(
                snapshot.collection(),
                snapshot.popularityScore(),
                snapshot.freshnessScore(),
                snapshot.trendingScore(),
                calculatedAt,
                displayOrder
        );
    }

    private record PopularCandidateSnapshot(
            UserBirdCollection collection,
            double popularityScore,
            double freshnessScore,
            double trendingScore
    ) {
    }
}
