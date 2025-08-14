-- V44: notification.type -> notification.subject/action 전환 (SYSTEM 데이터는 없음)
-- 1) 새 컬럼 추가 (초기에는 NULL 허용)
ALTER TABLE notification
    ADD COLUMN subject VARCHAR(50),
    ADD COLUMN action  VARCHAR(50);

-- 2) 기존 type 값을 subject/action으로 백필
--    LIKE/COMMENT/BIRD_ID_SUGGESTION -> subject=COLLECTION, action=LIKE|COMMENT|SUGGEST_BIRD_ID
UPDATE notification
SET subject = CASE type
                  WHEN 'LIKE'               THEN 'COLLECTION'
                  WHEN 'COMMENT'            THEN 'COLLECTION'
                  WHEN 'BIRD_ID_SUGGESTION' THEN 'COLLECTION'
                  ELSE subject
    END,
    action  = CASE type
                  WHEN 'LIKE'               THEN 'LIKE'
                  WHEN 'COMMENT'            THEN 'COMMENT'
                  WHEN 'BIRD_ID_SUGGESTION' THEN 'SUGGEST_BIRD_ID'
                  ELSE action
        END;

-- 3) NOT NULL 제약 (SYSTEM이 없으므로 둘 다 NOT NULL 가능)
ALTER TABLE notification
    ALTER COLUMN subject SET NOT NULL;

ALTER TABLE notification
    ALTER COLUMN action SET NOT NULL;

-- 4) 더 이상 사용하지 않는 type 컬럼 삭제
ALTER TABLE notification
    DROP COLUMN type;
