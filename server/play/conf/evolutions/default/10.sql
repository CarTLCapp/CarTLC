# --- Database schema

# --- !Ups

CREATE TABLE secondary_technician (
    id                  int auto_increment primary key,
    entry_id            int default 0,
    secondary_tech_id   int default 0,
);

# --- !Downs

DROP TABLE if exists secondary_technician;