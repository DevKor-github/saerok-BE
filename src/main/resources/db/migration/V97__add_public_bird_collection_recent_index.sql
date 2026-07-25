CREATE INDEX idx_user_bird_collection_public_bird_created_at
    ON user_bird_collection (bird_id, created_at DESC, id DESC)
    WHERE access_level = 'PUBLIC';
