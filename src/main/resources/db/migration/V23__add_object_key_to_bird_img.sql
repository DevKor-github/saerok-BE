ALTER TABLE bird_image ADD COLUMN object_key TEXT;

UPDATE bird_image
    SET object_key = replace(s3_url, 'https://d30ecbxpvvxmvh.cloudfront.net/', '')
    WHERE s3_url LIKE 'https://d30ecbxpvvxmvh.cloudfront.net/%';

ALTER TABLE bird_image ALTER COLUMN object_key SET NOT NULL;