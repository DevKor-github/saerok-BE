DELETE FROM notification_setting legacy
WHERE legacy.type = 'SYSTEM_CONTENT_DELETED'
  AND EXISTS (
      SELECT 1
      FROM notification_setting current
      WHERE current.user_device_id = legacy.user_device_id
        AND current.type = 'SYSTEM_ADMIN_MESSAGE'
  );

UPDATE notification_setting
SET type = 'SYSTEM_ADMIN_MESSAGE'
WHERE type = 'SYSTEM_CONTENT_DELETED';

DELETE FROM notification_setting
WHERE type NOT IN (
    'LIKED_ON_COLLECTION',
    'COMMENTED_ON_COLLECTION',
    'REPLIED_TO_COMMENT',
    'SUGGESTED_BIRD_ID_ON_COLLECTION',
    'COMMENTED_ON_FREE_BOARD_POST',
    'REPLIED_TO_FREE_BOARD_COMMENT',
    'SYSTEM_PUBLISHED_ANNOUNCEMENT',
    'SYSTEM_ADMIN_MESSAGE'
);
