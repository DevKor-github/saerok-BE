-- 1. users.gender
ALTER TABLE users ADD COLUMN gender_tmp VARCHAR(30);
UPDATE users SET gender_tmp = gender::text;
ALTER TABLE users DROP COLUMN gender;
ALTER TABLE users RENAME COLUMN gender_tmp TO gender;

-- 2. rarity_type.code
ALTER TABLE rarity_type ADD COLUMN code_tmp VARCHAR(30) NOT NULL;
UPDATE rarity_type SET code_tmp = code::text;
ALTER TABLE rarity_type DROP COLUMN code;
ALTER TABLE rarity_type RENAME COLUMN code_tmp TO code;

-- 3. residency_type.code
ALTER TABLE residency_type ADD COLUMN code_tmp VARCHAR(30) NOT NULL;
UPDATE residency_type SET code_tmp = code::text;
ALTER TABLE residency_type DROP COLUMN code;
ALTER TABLE residency_type RENAME COLUMN code_tmp TO code;

-- 4. bird_habitat.habitat_type
ALTER TABLE bird_habitat ADD COLUMN habitat_type_tmp VARCHAR(30);
UPDATE bird_habitat SET habitat_type_tmp = habitat_type::text;
ALTER TABLE bird_habitat DROP COLUMN habitat_type;
ALTER TABLE bird_habitat RENAME COLUMN habitat_type_tmp TO habitat_type;

-- 5. user_bird_collection.access_level
ALTER TABLE user_bird_collection ADD COLUMN access_level_tmp VARCHAR(30) NOT NULL;
UPDATE user_bird_collection SET access_level_tmp = access_level::text;
ALTER TABLE user_bird_collection DROP COLUMN access_level;
ALTER TABLE user_bird_collection RENAME COLUMN access_level_tmp TO access_level;

-- 6. ENUM TYPE DROP (모든 칼럼 변경 후)
DROP TYPE IF EXISTS gender_enum;
DROP TYPE IF EXISTS rarity_code_enum;
DROP TYPE IF EXISTS residency_code_enum;
DROP TYPE IF EXISTS access_level_enum;
DROP TYPE IF EXISTS habitat_type_enum;
