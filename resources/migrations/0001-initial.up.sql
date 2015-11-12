CREATE TABLE webhooks (
  id text not null CHECK (id <> ''::text),
  active bool not null,
  deleted bool not null,
  userid text not null,
  name text not null CHECK (name <> ''::text),
  description text,
  secret text not null CHECK (name <> ''::text),
  subdomain text not null UNIQUE CHECK (subdomain <> ''::text),
  CONSTRAINT webhooks_pk PRIMARY KEY (id),
  UNIQUE (id, userid)
);

CREATE TABLE whitelist (
  id text not null CHECK (id <> ''::text),
  webhookid text not null CHECK (id <> ''::text),
  userid text not null CHECK (userid <> ''::text), -- why repeat? see below
  description text not null CHECK (description <> ''::text),
  ip text not null CHECK (ip <> ''::text),
  CONSTRAINT whitelist_pk PRIMARY KEY (id),
  -- db hack to prevent adding whitelists to others' accounts
  FOREIGN KEY (userid, webhookid) REFERENCES webhooks (userid,id)
);

CREATE TABLE users (
  id text not null,
  provider text not null,
  uid text not null,
  email text not null,
  CONSTRAINT user_pk PRIMARY KEY (id)
);
