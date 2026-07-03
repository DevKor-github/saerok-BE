CREATE UNIQUE INDEX IF NOT EXISTS uq_bird_scientific_name_active_ci
    ON bird (LOWER(BTRIM(scientific_name)))
    WHERE deleted_at IS NULL;
