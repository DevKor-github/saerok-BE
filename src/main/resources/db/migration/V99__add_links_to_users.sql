ALTER TABLE users
    ADD COLUMN profile_links TEXT[] NOT NULL DEFAULT '{}';
