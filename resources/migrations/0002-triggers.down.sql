DROP TRIGGER webhook_added_notify ON webhooks;

DROP TRIGGER webhook_updated_notify ON webhooks;

DROP TRIGGER webhook_removed_notify ON webhooks;


DROP FUNCTION notify_app_added();

DROP FUNCTION notify_app_updated();

DROP FUNCTION notify_app_removed();
