UPDATE webhooks
SET blob = :blob
AND subdomain = :subdomain
WHERE id = :id AND userid = :userid
