DELETE FROM popular_collection pc
WHERE (
    SELECT COUNT(*)
    FROM user_bird_collection_like ubcl
    WHERE ubcl.user_bird_collection_id = pc.user_bird_collection_id
) < 5;
