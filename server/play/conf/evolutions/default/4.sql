# --- Database schema

# --- !Ups

ALTER TABLE technician ADD app_version varchar(64);

# --- !Downs

ALTER TABLE technician DROP app_version;

