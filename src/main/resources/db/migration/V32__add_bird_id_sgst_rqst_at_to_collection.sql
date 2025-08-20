ALTER TABLE user_bird_collection
    ADD COLUMN bird_id_suggestion_requested_at TIMESTAMPTZ;

UPDATE user_bird_collection
SET bird_id_suggestion_requested_at =
        CASE
            WHEN bird_id IS NULL THEN updated_at
            ELSE NULL
            END;
