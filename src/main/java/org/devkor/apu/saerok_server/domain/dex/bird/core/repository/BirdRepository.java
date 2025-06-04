package org.devkor.apu.saerok_server.domain.dex.bird.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.BirdSearchDto;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class BirdRepository {

    private final EntityManager em;

    /**
     * Bird 엔티티를 조회합니다. soft delete된 항목은 조회하지 않습니다.
     * @param id
     * @return
     */
    public Optional<Bird> findById(Long id) {
        return em.createQuery("SELECT b FROM Bird b WHERE b.id = :id AND b.deletedAt IS NULL", Bird.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    /**
     * 검색–PostgreSQL 네이티브 쿼리
     */
    public List<Bird> search(BirdSearchDto dto) {

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT b.*
              FROM bird b
              LEFT JOIN bird_habitat bh ON bh.bird_id = b.id
              LEFT JOIN bird_profile_mv bp ON bp.id = b.id
             WHERE 1=1
            """);

        Map<String, Object> params = new HashMap<>();

        /* 키워드 */
        if (dto.q() != null && !dto.q().isBlank()) {
            sql.append(" AND lower(b.korean_name) LIKE :q ");
            params.put("q", "%" + dto.q().toLowerCase() + "%");
        }

        /* 서식지 */
        if (dto.habitats() != null && !dto.habitats().isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < dto.habitats().size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("bh.habitat_type = :habitat")
                        .append(i);
                // 파라미터는 문자열( enum.name() )로 주고, SQL에서 enum 타입으로 캐스팅
                params.put("habitat" + i, dto.habitats().get(i).name());
            }
            sql.append(") ");
        }

        /* 크기 범위(여러 구간 OR 조건) */
        if (dto.cmRanges() != null && !dto.cmRanges().isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < dto.cmRanges().size(); i++) {
                var r = dto.cmRanges().get(i);
                String minKey = "min" + i;
                String maxKey = "max" + i;

                if (r.minCm() != null) {
                    sql.append(" b.body_length_cm > :").append(minKey);
                    params.put(minKey, r.minCm());
                }
                if (r.maxCm() != null) {
                    if (r.minCm() != null) sql.append(" AND");
                    sql.append(" b.body_length_cm <= :").append(maxKey);
                    params.put(maxKey, r.maxCm());
                }
                if (i < dto.cmRanges().size() - 1) sql.append(" OR");
            }
            sql.append(" ) ");
        }

        /* 계절(JSONB) */
        if (dto.seasons() != null && !dto.seasons().isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < dto.seasons().size(); i++) {
                String key = "season" + i;
                sql.append(" bp.seasons_with_rarity @> CAST(:")
                        .append(key)
                        .append(" AS jsonb)");
                if (i < dto.seasons().size() - 1) sql.append(" OR ");
                params.put(key,
                        "[{\"season\":\"" + dto.seasons().get(i).name() + "\"}]");
            }
            sql.append(" ) ");
        }

        /* 정렬 */
        sql.append(" ORDER BY b.")
            .append(dto.sortBy().getColumn())
            .append(" ")
            .append(dto.sortDir().name());

        Query query = em.createNativeQuery(sql.toString(), Bird.class);

        /* 페이징 */
        if (dto.page() != null && dto.size() != null) {
            int offset = (dto.page() - 1) * dto.size();
            query.setFirstResult(offset);
            query.setMaxResults(dto.size());
        }

        /* 파라미터 바인딩 */
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
