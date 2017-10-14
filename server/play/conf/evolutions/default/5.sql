# --- Database schema

# --- !Ups

ALTER TABLE message ADD app_version varchar(64);

# --- !Downs

ALTER TABLE message DROP app_version;

