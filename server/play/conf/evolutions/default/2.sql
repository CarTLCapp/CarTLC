# --- Database schema

# --- !Ups

ALTER TABLE equipment ADD is_local bit default 0;
ALTER TABLE note ADD is_local bit default 0;

# --- !Downs
ALTER TABLE equipment DROP is_local;
ALTER TABLE note DROP is_local;