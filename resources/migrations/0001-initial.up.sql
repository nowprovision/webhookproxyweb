CREATE TABLE users (
  id text not null CHECK (id <> ''::text),
  provider text not null CHECK (provider <> ''::text),
  uid text not null CHECK (uid <> ''::text),
  email text not null CHECK (email <> ''::text),
  UNIQUE (uid, provider),
  CONSTRAINT user_pk PRIMARY KEY (id)
);

CREATE TABLE webhooks (
  id text not null CHECK (id <> ''::text),
  userid text not null REFERENCES users(id),
  blob jsonb not null,
  subdomain text not null CHECK (subdomain <> ''::text),
  UNIQUE (subdomain),
  CONSTRAINT webhooks_pk PRIMARY KEY (id)
);

