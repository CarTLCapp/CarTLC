# --- Database schema

# --- !Ups

ALTER TABLE technician ADD reload_code varchar(16);

# --- !Downs

ALTER TABLE technician DROP reload_code;

