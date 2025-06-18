ALTER TABLE user_bird_collection_image
    ADD COLUMN content_type VARCHAR(50) NOT NULL default 'application/octet-stream';