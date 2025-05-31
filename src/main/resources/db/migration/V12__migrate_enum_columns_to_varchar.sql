/* ========= V12__migrate_enum_columns_to_varchar.sql ========= */

----------------------------------------------------------------
-- 0. 의존 뷰·인덱스 제거
----------------------------------------------------------------
DROP MATERIALIZED VIEW IF EXISTS bird_profile_mv;

----------------------------------------------------------------
-- 1. enum → varchar 타입 변환
----------------------------------------------------------------
-- users.gender
ALTER TABLE users
    ALTER COLUMN gender TYPE VARCHAR(30) USING gender::text;

-- rarity_type.code  (NOT NULL 유지)
ALTER TABLE rarity_type
    ALTER COLUMN code TYPE VARCHAR(30) USING code::text,
    ALTER COLUMN code SET NOT NULL;

-- residency_type.code  (NOT NULL 유지)
ALTER TABLE residency_type
    ALTER COLUMN code TYPE VARCHAR(30) USING code::text,
    ALTER COLUMN code SET NOT NULL;

-- bird_habitat.habitat_type
ALTER TABLE bird_habitat
    ALTER COLUMN habitat_type TYPE VARCHAR(30) USING habitat_type::text;

-- user_bird_collection.access_level  (NOT NULL 유지)
ALTER TABLE user_bird_collection
    ALTER COLUMN access_level TYPE VARCHAR(30) USING access_level::text,
    ALTER COLUMN access_level SET NOT NULL;

----------------------------------------------------------------
-- 2. 더 이상 쓰지 않는 ENUM 타입 제거
----------------------------------------------------------------
DROP TYPE IF EXISTS gender_enum;
DROP TYPE IF EXISTS rarity_code_enum;
DROP TYPE IF EXISTS residency_code_enum;
DROP TYPE IF EXISTS access_level_enum;
DROP TYPE IF EXISTS habitat_type_enum;

----------------------------------------------------------------
-- 3. bird_profile_mv 재생성 (V5__add_bird_deleted_at.sql에 정의된 bird_profile_mv 그대로 가져옴)
----------------------------------------------------------------
DROP MATERIALIZED VIEW IF EXISTS bird_profile_mv;

CREATE MATERIALIZED VIEW bird_profile_mv AS
WITH month_season AS (
    SELECT m AS month,
           CASE WHEN m IN (3,4,5)   THEN 'SPRING'
                WHEN m IN (6,7,8)   THEN 'SUMMER'
                WHEN m IN (9,10,11) THEN 'AUTUMN'
                ELSE                    'WINTER'
               END AS season
    FROM generate_series(1,12) AS g(m)
),
     bird_month_priority AS (
         SELECT br.bird_id,
                ms.month,
                MAX(rt.priority) AS priority
         FROM bird_residency br
                  JOIN rarity_type     rt  ON rt.id  = br.rarity_type_id
                  JOIN residency_type  rty ON rty.id = br.residency_type_id
                  JOIN month_season    ms  ON ((COALESCE(br.month_bitmask, rty.month_bitmask)
             >> (ms.month-1)) & 1) = 1
         GROUP BY br.bird_id, ms.month
     ),
     bird_season_priority AS (
         SELECT bmp.bird_id,
                ms.season,
                MAX(bmp.priority) AS priority
         FROM bird_month_priority bmp
                  JOIN month_season ms ON ms.month = bmp.month
         GROUP BY bmp.bird_id, ms.season
     ),
     bird_season_rarity AS (
         SELECT bsp.bird_id,
                bsp.season,
                bsp.priority,
                rt.code AS rarity_code
         FROM bird_season_priority bsp
                  JOIN rarity_type rt ON rt.priority = bsp.priority
     ),
     seasons_json AS (
         SELECT bird_id,
                jsonb_agg(
                        jsonb_build_object(
                                'season',   season,
                                'rarity',   rarity_code,
                                'priority', priority
                        )
                        ORDER BY array_position(
                                ARRAY['SPRING','SUMMER','AUTUMN','WINTER'], season
                                 )
                ) AS seasons_with_rarity
         FROM bird_season_rarity
         GROUP BY bird_id
     ),
     habitats_array AS (
         SELECT bird_id,
                array_agg(DISTINCT habitat_type) AS habitats
         FROM bird_habitat
         GROUP BY bird_id
     ),
     images_json AS (
         SELECT bird_id,
                jsonb_agg(
                        jsonb_build_object(
                                's3_url',       s3_url,
                                'original_url', original_url,
                                'order_index',  order_index,
                                'is_thumb',     is_thumb
                        )
                        ORDER BY order_index
                ) AS images
         FROM bird_image
         GROUP BY bird_id
     )
SELECT
    b.id,
    b.korean_name,
    b.scientific_name,
    b.scientific_year,
    b.description_is_ai_generated,
    b.class_eng,
    b.class_kor,
    b."order_eng",
    b."order_kor",
    b.family_eng,
    b.family_kor,
    b.genus_eng,
    b.genus_kor,
    b.species_eng,
    b.species_kor,
    b.scientific_author,
    b.phylum_eng,
    b.phylum_kor,
    b.nibr_url,
    b.description,
    b.description_source,
    ha.habitats,
    b.body_length_cm,
    COALESCE(sj.seasons_with_rarity, '[]'::jsonb) AS seasons_with_rarity,
    COALESCE(ij.images,              '[]'::jsonb) AS images,
    b.created_at,
    b.updated_at,
    b.deleted_at
FROM bird b
         LEFT JOIN habitats_array ha ON ha.bird_id = b.id
         LEFT JOIN seasons_json  sj ON sj.bird_id = b.id
         LEFT JOIN images_json   ij ON ij.bird_id = b.id
WHERE b.deleted_at IS NULL
WITH NO DATA;

-- MV에 고유 인덱스 생성 (Concurrent Refresh 요건)
CREATE UNIQUE INDEX idx_bird_profile_mv_id ON bird_profile_mv(id);

-- MV 초기 데이터 채우기 (Refresh)
REFRESH MATERIALIZED VIEW bird_profile_mv;