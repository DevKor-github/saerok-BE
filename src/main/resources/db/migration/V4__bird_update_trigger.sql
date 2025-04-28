CREATE OR REPLACE FUNCTION touch_bird_updated_at()
RETURNS trigger AS $$
BEGIN
UPDATE bird
SET updated_at = NOW()
WHERE id = COALESCE(NEW.bird_id, OLD.bird_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- bird_habitat → bird.updated_at
DROP TRIGGER IF EXISTS trg_bird_habitat_touch ON bird_habitat;
CREATE TRIGGER trg_bird_habitat_touch
    AFTER INSERT OR UPDATE OR DELETE ON bird_habitat
    FOR EACH ROW EXECUTE FUNCTION touch_bird_updated_at();

-- bird_image → bird.updated_at
DROP TRIGGER IF EXISTS trg_bird_image_touch ON bird_image;
CREATE TRIGGER trg_bird_image_touch
    AFTER INSERT OR UPDATE OR DELETE ON bird_image
    FOR EACH ROW EXECUTE FUNCTION touch_bird_updated_at();

-- bird_residency → bird.updated_at
DROP TRIGGER IF EXISTS trg_bird_residency_touch ON bird_residency;
CREATE TRIGGER trg_bird_residency_touch
    AFTER INSERT OR UPDATE OR DELETE ON bird_residency
    FOR EACH ROW EXECUTE FUNCTION touch_bird_updated_at();
