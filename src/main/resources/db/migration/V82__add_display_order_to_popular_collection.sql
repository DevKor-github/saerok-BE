ALTER TABLE popular_collection
    ADD COLUMN display_order INT NOT NULL DEFAULT 0;

CREATE INDEX idx_popular_collection_display_order ON popular_collection(display_order);
