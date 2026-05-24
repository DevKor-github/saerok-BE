ALTER TABLE bird
    ADD COLUMN conservation_grade VARCHAR(16) NOT NULL DEFAULT 'NONE';

ALTER TABLE bird
    ADD CONSTRAINT chk_bird_conservation_grade
        CHECK (conservation_grade IN ('NONE', 'GRADE_I', 'GRADE_II'));

UPDATE bird
SET conservation_grade = 'GRADE_I'
WHERE id IN (
    58, 59, 86, 90, 100, 128, 161, 214, 215, 219,
    235, 257, 258, 275, 473, 518, 600
);

UPDATE bird
SET conservation_grade = 'GRADE_II'
WHERE id IN (
    27, 31, 33, 49, 50, 53, 57, 60, 73, 93,
    97, 98, 99, 102, 123, 135, 150, 162, 164, 179,
    201, 217, 218, 222, 224, 250, 253, 254, 255, 256,
    261, 264, 265, 266, 267, 268, 269, 271, 274, 287,
    395, 399, 403, 410, 411, 421, 425, 433, 444, 445,
    452, 459, 529, 530, 537, 538, 553, 577, 583, 592,
    593, 595, 602
);

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
                                'object_key',   object_key,
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
    b.conservation_grade,
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

CREATE UNIQUE INDEX idx_bird_profile_mv_id ON bird_profile_mv(id);

REFRESH MATERIALIZED VIEW bird_profile_mv;
