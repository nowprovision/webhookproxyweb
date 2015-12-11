CREATE OR REPLACE FUNCTION notify_app_added() RETURNS trigger AS $$
DECLARE
BEGIN
    PERFORM pg_notify('added', NEW.id);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_app_updated() RETURNS trigger AS $$
DECLARE
BEGIN
    PERFORM pg_notify('updated', NEW.id);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION notify_app_removed() RETURNS trigger AS $$
DECLARE
BEGIN
    PERFORM pg_notify('removed', OLD.id);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER webhook_added_notify AFTER INSERT ON webhooks FOR EACH ROW EXECUTE PROCEDURE notify_app_added();

CREATE TRIGGER webhook_updated_notify AFTER UPDATE ON webhooks FOR EACH ROW EXECUTE PROCEDURE notify_app_updated();

CREATE TRIGGER webhook_removed_notify AFTER DELETE ON webhooks FOR EACH ROW EXECUTE PROCEDURE notify_app_removed();
