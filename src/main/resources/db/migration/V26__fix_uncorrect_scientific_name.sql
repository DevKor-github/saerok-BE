-- Fix scientific name for bird ID 499 (회색머리노랑솔새)

-- Update bird table
UPDATE bird 
SET scientific_name = 'Phylloscopus tephrocephalus',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 499;

-- Refresh materialized view to reflect the changes
REFRESH MATERIALIZED VIEW bird_profile_mv;