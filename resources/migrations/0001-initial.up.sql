CREATE TABLE webhooks (
  id text not null,
  active bool not null,
  deleted bool not null,
  userid text not null,
  name text not null,
  description text,
  subdomain text not null UNIQUE,
  CONSTRAINT webhooks_pk PRIMARY KEY (id)
);

CREATE TABLE whitelist (
  id text not null,
  webhookid text not null,
  ip text not null,
  CONSTRAINT whitelist_pk PRIMARY KEY (id)
);

CREATE TABLE users (
  id text not null,
  provider text not null,
  uid text not null,
  email text not null,
  CONSTRAINT user_pk PRIMARY KEY (id)
);
