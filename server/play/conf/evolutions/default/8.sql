# --- Database schema

# --- !Ups

ALTER TABLE entry ADD time_zone varchar(32);

# --- !Downs

ALTER TABLE entry DROP time_zone;

