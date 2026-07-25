CREATE INDEX idx_user_collection_location_geography
    ON user_bird_collection USING GIST ((location::geography));
