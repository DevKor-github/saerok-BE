ALTER TABLE popular_collection
    ADD COLUMN popularity_score double precision,
    ADD COLUMN freshness_score double precision;

UPDATE popular_collection
SET popularity_score = 0,
    freshness_score = 0;

ALTER TABLE popular_collection
    ALTER COLUMN popularity_score SET NOT NULL,
    ALTER COLUMN freshness_score SET NOT NULL;
