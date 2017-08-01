# --- Database schema

# --- !Ups

ALTER TABLE entry ADD license_plate varchar(64);

# --- !Downs

ALTER TABLE entry DROP license_plate;

