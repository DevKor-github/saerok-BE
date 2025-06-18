ALTER TABLE users ADD COLUMN joined_at TIMESTAMPTZ;
UPDATE users SET joined_at = created_at;
ALTER TABLE users ALTER COLUMN joined_at SET NOT NULL;

/*
초기에는 created_at을 가입 시각으로 겸할 수 있다고 판단했는데, 사용자 탈퇴 시 soft delete하는 것을 고려한다면, 가입 시각이라는 의미를 나타내는 필드를 별도로 두는 게 필요하다고 판단하여 추가.
created_at을 재가입 시각으로 업데이트하는 것도 고려했으나 그것은 created_at의 의미와 맞지 않다고 판단.
joined_at 필드는 최초 가입 또는 재가입 등 가장 최근의 가입 시각을 반영하는 필드.
 */