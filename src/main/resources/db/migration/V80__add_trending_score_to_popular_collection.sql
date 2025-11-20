DELETE FROM popular_collection;

ALTER TABLE popular_collection
    ADD COLUMN trending_score double precision,
    ADD COLUMN calculated_at timestamp with time zone;

UPDATE popular_collection
SET trending_score = 0,
    calculated_at = now();

ALTER TABLE popular_collection
    ALTER COLUMN trending_score SET NOT NULL,
    ALTER COLUMN calculated_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_popular_collection_trending_score
    ON popular_collection(trending_score DESC, calculated_at DESC);
