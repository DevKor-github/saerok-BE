-- 기존에는 새의 특정 계절 희귀도를 결정할 때 UNSPECIFIED를 RARE보다 우선시했는데, RARE를 우선시하도록 변경함

UPDATE rarity_type
SET priority = CASE code
                   WHEN 'UNSPECIFIED' THEN -10
                   WHEN 'RARE' THEN 0
                   ELSE priority
    END
WHERE code IN ('UNSPECIFIED', 'RARE');
