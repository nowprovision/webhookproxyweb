UPDATE webhooks
SET blob = :blob, subdomain = :subdomain
WHERE id = :id AND userid = :userid
