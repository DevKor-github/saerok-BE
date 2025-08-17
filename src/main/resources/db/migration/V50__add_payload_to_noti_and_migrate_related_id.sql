-- 1) payload(jsonb) 컬럼 추가 (기본값은 빈 오브젝트)
ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS payload JSONB NOT NULL DEFAULT '{}'::jsonb;

-- 2) 기존 related_id 값을 payload.relatedId로 이관
UPDATE notification
SET payload = jsonb_build_object('relatedId', related_id)
WHERE related_id IS NOT NULL;